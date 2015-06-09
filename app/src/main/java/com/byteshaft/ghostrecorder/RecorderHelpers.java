package com.byteshaft.ghostrecorder;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class RecorderHelpers extends ContextWrapper {

    private CustomMediaRecorder mRecorder;
    private PendingIntent pendingIntent;
    private AlarmManager alarmManager;

    public RecorderHelpers(Context base) {
        super(base);
        mRecorder = CustomMediaRecorder.getInstance();
    }

    void startRecording(int time) {
        if (mRecorder.isRecording()) {
            Log.i("SPY", "Recording already in progress");
            return;
        }
        mRecorder.reset();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mRecorder.setAudioEncodingBitRate(128);
        mRecorder.setMaxDuration(100000);
        mRecorder.setDuration(time);
        mRecorder.setOutputFile(Environment.getExternalStorageDirectory() + "/" + "Recordings/" + getTimeStamp() + ".aac");

        try {
            mRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mRecorder.start();
    }

    void stopRecording() {
        if (mRecorder.isRecording()) {
            mRecorder.stop();
            mRecorder.reset();
            mRecorder.release();
            cancelAlarm();
            mRecorder = null;
        }
    }

    void createRecordingDirectoryIfNotAlreadyCreated() {
        File recordingsDirectory = new File(Environment.getExternalStorageDirectory() + "/" + "Recordings");
        if (!recordingsDirectory.exists()) {
            recordingsDirectory.mkdir();
        }
    }
    public void startAlarm(Context context) {
        Intent intent = new Intent("com.byteshaft.startAlarm");
        pendingIntent = PendingIntent.getBroadcast(context, 0 , intent, 0);
        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        int interval = 5000;
        alarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + 5000, pendingIntent);
        Toast.makeText(this, "Alarm Set!", Toast.LENGTH_SHORT).show();
        System.out.println(alarmManager == null);
    }

    public void cancelAlarm() {
        if(alarmManager != null) {
            alarmManager.cancel(pendingIntent);
            Toast.makeText(this, "Alarm canceled!", Toast.LENGTH_SHORT).show();
        }
    }

    private String getTimeStamp() {
        return new SimpleDateFormat("yyyyMMddhhmmss", Locale.US).format(new Date());
    }

    String getHashsumForFile(String path) {
        MessageDigest messageDigest = null;
        try {
            messageDigest = MessageDigest.getInstance("SHA1");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        FileInputStream fileInput = null;
        try {
            fileInput = new FileInputStream(path);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        byte[] dataBytes = new byte[1024];

        int bytesRead = 0;

        try {
            while ((bytesRead = fileInput.read(dataBytes)) != -1) {
                messageDigest.update(dataBytes, 0, bytesRead);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        byte[] digestBytes = messageDigest.digest();

        StringBuffer sb = new StringBuffer("");

        for (int i = 0; i < digestBytes.length; i++) {
            sb.append(Integer.toString((digestBytes[i] & 0xff) + 0x100, 16).substring(1));
        }

        System.out.println("Checksum for the File: " + sb.toString());

        try {
            fileInput.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return sb.toString();
    }
}
