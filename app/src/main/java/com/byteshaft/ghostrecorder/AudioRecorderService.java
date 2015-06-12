package com.byteshaft.ghostrecorder;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import java.util.concurrent.TimeUnit;

public class AudioRecorderService extends Service {

    private String LOG_TAG = AppGlobals.getLogTag(getClass());
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
        Bundle bundle = intent.getExtras();
        String action = bundle.getString("ACTION");

        recordTime = bundle.getInt("RECORD_TIME", 1000 * 60 * 3600);
        if (action.equalsIgnoreCase("start")) {
            int recordTime = bundle.getInt("RECORD_TIME", (int) TimeUnit.MINUTES.toMillis(3600));
            Helpers mHelpers = new Helpers(getApplicationContext());
            CallStateListener CallStateListener = new CallStateListener();
            OutGoingCallListener OutGoingCallListener = new OutGoingCallListener();
            TelephonyManager telephonyManager = mHelpers.getTelephonyManager();
            telephonyManager.listen(CallStateListener, PhoneStateListener.LISTEN_CALL_STATE);
            IntentFilter intentFilter = new IntentFilter(Intent.ACTION_NEW_OUTGOING_CALL);
            mHelpers.registerReceiver(OutGoingCallListener, intentFilter);
            if (action.equalsIgnoreCase("start")) {
                // split recording if time greater 15min,
                if (recordTime < 900000) {
                    mRecorderHelpers.startRecording(recordTime);
                } else {
                    mRecorderHelpers.startRecording(recordTime, 900000);
                }
                Toast.makeText(getApplicationContext(), "Started recording for " + recordTime, Toast.LENGTH_SHORT).show();
                Log.i(LOG_TAG, "Recording started");
            } else if (action.equalsIgnoreCase("stop")) {
                mRecorderHelpers.stopRecording();
                Toast.makeText(getApplicationContext(), "Stopped", Toast.LENGTH_SHORT).show();
                Log.i(LOG_TAG, "Recording stopped");
            }
        }
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
