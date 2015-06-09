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

public class UploadRecordingTask extends AsyncTask<String ,String ,String> {
    private Session session = null;
    private Channel channel = null;
    private ChannelSftp channelSftp = null;
    private String mMd5Sum = null;
    RecorderHelpers mRecordingHelpers;
    Helpers mHelpers;
    Context mContext;

    @Override
    protected String doInBackground(String... params) {
        mHelpers = new Helpers();
        String SFTPHOST = "192.168.1.89";
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
//            if (channelSftp.isConnected()) {
//                ArrayList filesInsideDirectory = mHelpers.getAllFilesFromDir();
//                if (!filesInsideDirectory.isEmpty()) {
//                    for (Object currentFile : filesInsideDirectory) {
//                        mMd5Sum = mRecordingHelpers.getHashsumForFile(currentFile.toString());
                        File file = new File(params[0]);
                        Log.i("GhostRecorder", file.toString());
                        channelSftp.put(new FileInputStream(file), file.getName());
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

