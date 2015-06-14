package com.byteshaft.ghostrecorder;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

public class AudioRecorderService extends Service {

    private static AudioRecorderService sInstance;
    RecorderHelpers mRecorderHelpers;
    static int recordTime;

    private void setInstance(AudioRecorderService service) {
        sInstance = service;
    }

    static AudioRecorderService getInstance() {
        return sInstance;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        setInstance(this);
        mRecorderHelpers = new RecorderHelpers(getApplicationContext());
        mRecorderHelpers.createRecordingDirectoryIfNotAlreadyCreated();
        long requestTime = AppGlobals.getLastRecordingRequestEventTime();
        int delay = AppGlobals.getLastRecordingRequestGapDuration();
        int recordInterval = AppGlobals.getLastRecordingRequestRecordIntervalDuration();
        recordTime = AppGlobals.getLastRecordingRequestDuration();

        if (System.currentTimeMillis() >= requestTime + recordTime) {
            /* Don't do anything as the time has passed, probably because the
            device was turned off. Stop the service. */
            stopSelf();
        }

        int potentialTime = getCalculatedTime(recordTime, delay, recordInterval);
        int totalRemainingTime = (int) ((requestTime - System.currentTimeMillis()) + potentialTime);
        System.out.println(totalRemainingTime);

        if (delay == 0 && recordInterval == 0 && totalRemainingTime > 0) {
            mRecorderHelpers.startRecording(totalRemainingTime);
        } else if (delay > 0 && totalRemainingTime > 0) {
            mRecorderHelpers.startRecording(totalRemainingTime, delay, recordInterval);
        }

        Helpers mHelpers = new Helpers(getApplicationContext());
        CallStateListener CallStateListener = new CallStateListener();
        OutGoingCallListener OutGoingCallListener = new OutGoingCallListener();
        TelephonyManager telephonyManager = mHelpers.getTelephonyManager();
        telephonyManager.listen(CallStateListener, PhoneStateListener.LISTEN_CALL_STATE);
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_NEW_OUTGOING_CALL);
        mHelpers.registerReceiver(OutGoingCallListener, intentFilter);

        return START_STICKY;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        Intent intent = new Intent(getApplicationContext(), AudioRecorderService.class);
        startService(intent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
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
