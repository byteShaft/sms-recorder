package com.byteshaft.ghostrecorder;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

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
            mRecorderHelpers.startRecording(totalRemainingTime, null, 0);
        } else if (delay > 0 && totalRemainingTime > 0) {
            mRecorderHelpers.startRecording(totalRemainingTime, delay, recordInterval);
        }

        Helpers mHelpers = new Helpers(getApplicationContext());
        TelephonyManager telephonyManager = mHelpers.getTelephonyManager();
        telephonyManager.listen(mCallStateListener, PhoneStateListener.LISTEN_CALL_STATE);
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_NEW_OUTGOING_CALL);
        mHelpers.registerReceiver(mOutgoingCallListener, intentFilter);

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

    private PhoneStateListener mCallStateListener = new PhoneStateListener() {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);
            switch (state) {
                case TelephonyManager.CALL_STATE_RINGING:
                    if (CustomMediaRecorder.isRecording()) {
                        mRecorderHelpers.stopRecording();
                    }
                    break;
            }
        }
    };

    private BroadcastReceiver mOutgoingCallListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (CustomMediaRecorder.isRecording()) {
                mRecorderHelpers.stopRecording();
            }
        }
    };
}
