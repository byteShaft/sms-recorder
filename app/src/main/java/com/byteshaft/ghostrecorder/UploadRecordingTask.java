package com.byteshaft.ghostrecorder;


import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import java.io.File;
import java.io.FileInputStream;

public class UploadRecordingTask extends AsyncTask<String ,String ,String> {
    private Session session = null;
    private Channel channel = null;
    private ChannelSftp channelSftp = null;
    private String mMd5Sum = null;
    RecorderHelpers mRecordingHelpers;

    @Override
    protected String doInBackground(String... params) {
        String SFTPHOST = "192.168.1.2";
        int SFTPPORT = 22;
        String SFTPUSER = "abu";
        String SFTPPASS = "abu";
        String SFTPWORKINGDIR = "/home/abu/www/";
        Log.i("Ghost_Recorder", "preparing the host information for sftp.");
        try {
            JSch jsch = new JSch();
            session = jsch.getSession(SFTPUSER, SFTPHOST, SFTPPORT);
            session.setPassword(SFTPPASS);
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            session.connect();
            Log.i("Ghost_Recorder", "Host connected.");
            channel = session.openChannel("sftp");
            channel.connect();
            Log.i("Ghost_Recorder", "sftp channel opened and connected.");
            channelSftp = (ChannelSftp) channel;
            channelSftp.cd(SFTPWORKINGDIR);
            String sdCard = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Recordings/ok.aac";
            mMd5Sum = mRecordingHelpers.getHashsumForFile(sdCard);

            File f = new File(sdCard);
            channelSftp.put(new FileInputStream(f), f.getName());
            Log.i("Ghost_Recorder", "File transfered successfully to host.");
        } catch (Exception ex) {
            Log.i("Ghost_Recorder", "Exception found while tranfer the response.");
            ex.printStackTrace();
        }
        return null;
    }

        private void stopUploadingWhenFinished() {
            channelSftp.exit();
            Log.i("Server", "sftp Channel exited.");
            channel.disconnect();
            Log.i("Server", "Channel disconnected.");
            session.disconnect();
            Log.i("Server", "Host Session disconnected.");
    }
}

