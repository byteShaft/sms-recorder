package com.byteshaft.ghostrecorder;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class AudioRecorderService extends Service {

    static AudioRecorderService instance;
    RecorderHelpers mRecorderHelpers;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        instance = this;
        mRecorderHelpers = new RecorderHelpers(getApplicationContext());
        mRecorderHelpers.createRecordingDirectoryIfNotAlreadyCreated();
        Bundle bundle = intent.getExtras();
        String action = bundle.getString("ACTION");
        int recordTime = Integer.valueOf(bundle.getString("RECORD_TIME"));
        if (action.equalsIgnoreCase("start")) {
            mRecorderHelpers.startRecording(recordTime);
            Toast.makeText(getApplicationContext(), "Started", Toast.LENGTH_SHORT).show();
            Log.i(AppGlobals.LOG_TAG, "Recording started");
        } else if (action.equalsIgnoreCase("stop")) {
            mRecorderHelpers.stopRecording();
            Toast.makeText(getApplicationContext(), "Stopped", Toast.LENGTH_SHORT).show();
            Log.i(AppGlobals.LOG_TAG, "Recording stopped");
        }

        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
