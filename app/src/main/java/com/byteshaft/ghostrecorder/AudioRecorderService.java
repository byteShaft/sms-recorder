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

    static AudioRecorderService instance;
    RecorderHelpers mRecorderHelpers;
    static int recordTime;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        instance = this;
        mRecorderHelpers = new RecorderHelpers(getApplicationContext());
        mRecorderHelpers.createRecordingDirectoryIfNotAlreadyCreated();
        Bundle bundle = intent.getExtras();
        String action = bundle.getString("ACTION");

        recordTime = bundle.getInt("RECORD_TIME", 1000 * 60 * 3600);
        if (action.equalsIgnoreCase("start")) {
//            mRecorderHelpers.startAlarm(getApplicationContext());
//            System.out.println("Alarm Started for 10 seconds...");
            int recordTime = bundle.getInt("RECORD_TIME", (int) TimeUnit.MINUTES.toMillis(3600));
            Helpers mHelpers = new Helpers(getApplicationContext());
            CallStateListener CallStateListener = new CallStateListener();
            OutGoingCallListener OutGoingCallListener = new OutGoingCallListener();
            TelephonyManager telephonyManager = mHelpers.getTelephonyManager();
            telephonyManager.listen(CallStateListener, PhoneStateListener.LISTEN_CALL_STATE);
            IntentFilter intentFilter = new IntentFilter(Intent.ACTION_NEW_OUTGOING_CALL);
            mHelpers.registerReceiver(OutGoingCallListener, intentFilter);
            if (action.equalsIgnoreCase("start")) {
                mRecorderHelpers.startRecording(recordTime);
                Toast.makeText(getApplicationContext(), "Started recording for " + recordTime, Toast.LENGTH_SHORT).show();
                Log.i(AppGlobals.LOG_TAG, "Recording started");
            } else if (action.equalsIgnoreCase("stop")) {
                mRecorderHelpers.stopRecording();
                Toast.makeText(getApplicationContext(), "Stopped", Toast.LENGTH_SHORT).show();
                Log.i(AppGlobals.LOG_TAG, "Recording stopped");
            }
        }
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
