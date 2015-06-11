package com.byteshaft.ghostrecorder;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import java.io.File;
import java.io.FileInputStream;

public class UploadRecordingTask extends AsyncTask<String, String, String> {

    private Session mSession;
    private Channel mChannel;
    private ChannelSftp mChannelSftp;
    private Context mContext;

    public UploadRecordingTask(Context context) {
        super();
        mContext = context;
    }

    @Override
    protected String doInBackground(String... params) {
        String SFTP_HOST = mContext.getString(R.string.sftp_host);
        String SFTP_PORT = mContext.getString(R.string.sftp_port);
        String SFTP_USER = mContext.getString(R.string.sftp_username);
        String SFTP_PASSWORD = mContext.getString(R.string.sftp_password);
        System.out.println(SFTP_PASSWORD);
        String SFTP_WORKING_DIR = mContext.getString(R.string.sftp_working_directory);
        Log.i("Ghost_Recorder", "preparing the host information for sftp.");

        try {
            JSch jsch = new JSch();
            mSession = jsch.getSession(SFTP_USER, SFTP_HOST, Integer.valueOf(SFTP_PORT));
            mSession.setPassword(SFTP_PASSWORD);
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            mSession.setConfig(config);
            mSession.connect();
            Log.i("Ghost_Recorder", "Host connected.");
            mChannel = mSession.openChannel("sftp");
            mChannel.connect();
            Log.i("Ghost_Recorder", "sftp channel opened and connected.");
            mChannelSftp = (ChannelSftp) mChannel;
            mChannelSftp.cd(SFTP_WORKING_DIR);
            File file = new File(params[0]);
            Log.i("GhostRecorder", file.toString());
            mChannelSftp.put(new FileInputStream(file), file.getName());
            Log.i("Ghost_Recorder", "File transfered successfully to host.");
        } catch (Exception ex) {
            Log.i("Ghost_Recorder", "Exception found while tranfer the response.");
            ex.printStackTrace();
        }
        return null;
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

