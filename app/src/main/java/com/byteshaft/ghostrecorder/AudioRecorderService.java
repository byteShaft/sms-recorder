package com.byteshaft.ghostrecorder;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

public class AudioRecorderService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        RecorderHelpers recorderHelpers = new RecorderHelpers();
        recorderHelpers.createRecordingDirectoryIfNotAlreadyCreated();
        Bundle bundle = intent.getExtras();
        String password = bundle.getString("PASSWORD");
        if (!isPasswordCorrect(password)) {
            Log.i("REC", "Invalid password, stopping the service");
            stopSelf();
        }
        String action = bundle.getString("STATE");
        if (!isActionValid(action)) {
            Log.i("REC", "Invalid action, stopping the service");
            stopSelf();
        }
        int recordTime = Integer.valueOf(bundle.getString("RECORD_TIME"));
        int batteryLimit = Integer.valueOf(bundle.getString("BATTERY_LEVEL"));
        if (!isBatteryLimitSane(batteryLimit)) {
            Log.i("REC", "Battery should be between 0 and 100, stopping the service");
            stopSelf();
        }
        String sendResponse = bundle.getString("RESPONSE").toLowerCase();
        if (sendResponse.equals("start")) {
            Log.i("REC", "Starting recording");
            recorderHelpers.startRecording(recordTime);
        } else if (sendResponse.equals("stop")) {
            // Stub need to write a stop method.
        }

        return super.onStartCommand(intent, flags, startId);
    }

    private boolean isPasswordCorrect(String password) {
        return password.equals("password");
    }

    private boolean isActionValid(String command) {
        return command.toLowerCase().equals("start") || command.toLowerCase().equals("stop");
    }

    private boolean isBatteryLimitSane(int limit) {
        return limit > 0 || limit < 100;
    }
}
