package com.byteshaft.ghostrecorder;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class AudioRecorderService extends Service {

    static AudioRecorderService instance;
    RecorderHelpers mRecorderHelpers;
    private PendingIntent pendingIntent;
    private AlarmManager alarmManager;
    static int recordTime;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        instance = this;
        mRecorderHelpers = new RecorderHelpers(getApplicationContext());
        mRecorderHelpers.createRecordingDirectoryIfNotAlreadyCreated();
        Bundle bundle = intent.getExtras();
        String action = bundle.getString("ACTION");
        recordTime = Integer.valueOf(bundle.getString("RECORD_TIME"));
        if (action.equalsIgnoreCase("start")) {
            mRecorderHelpers.startAlarm(getApplicationContext());
            System.out.println("Alarm Started for 10 seconds...");
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
