package com.byteshaft.ghostrecorder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

public class BinarySmsReceiver extends BroadcastReceiver {

    private String currentPassword;

    @Override
    public void onReceive(Context context, Intent intent) {
        Helpers helpers = new Helpers();
        SharedPreferences preferences = helpers.getPreferenceManager(context);
        boolean serviceState = preferences.getBoolean("service_state", false);
        if (!serviceState) {
            Toast.makeText(context, "Service is disabled", Toast.LENGTH_SHORT).show();
            return;
        }

        String incomingCommand = helpers.decodeIncomingSmsText(intent);
        String[] smsCommand = incomingCommand.split("_");

        if (smsCommand.length != 3) {
            Log.e(AppGlobals.LOG_TAG, "Incomplete command.");
            return;
        }

        Intent smsServiceIntent = new Intent(
                context.getApplicationContext(), AudioRecorderService.class);

        String password = smsCommand[0];
        String action = smsCommand[1];
        String time = smsCommand[2];
//        String battery_level = smsCommand[3];
//        String response = smsCommand[4];

        currentPassword = preferences.getString("service_password", null);
        if (!password.equals(currentPassword)) {
            Log.e(AppGlobals.LOG_TAG, "Wrong password.");
//            if (response.equalsIgnoreCase("yes")) {
//                // FIXME: Send sms to the sender for wrong password
//            }
            return;
        }

        if (action.equalsIgnoreCase("start")) {
            smsServiceIntent.putExtra("ACTION", "start");
        } else if (action.equalsIgnoreCase("stop")) {
            AudioRecorderService.instance.mRecorderHelpers.stopRecording();
            return;
        } else {
            Log.e(AppGlobals.LOG_TAG, "Invalid command.");
//            if (response.equalsIgnoreCase("yes")) {
//                // FIXME: Send sms to the sender for wrong command
//            }
            return;
        }

//        String[] realTime = time.split(":");
//        String recordingLength = realTime[0];
//        String recordingTime = realTime[1];

        // TODO: implement code or listener for battery level change.

        smsServiceIntent.putExtra("PASSWORD", password);
        smsServiceIntent.putExtra("RECORD_TIME", time);
//        smsServiceIntent.putExtra("BATTERY_LEVEL", battery_level);
//        smsServiceIntent.putExtra("RESPONSE", response);
        context.startService(smsServiceIntent);
    }
}
