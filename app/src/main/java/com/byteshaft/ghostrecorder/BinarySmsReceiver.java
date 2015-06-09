package com.byteshaft.ghostrecorder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

public class BinarySmsReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences preferences = Helpers.getPreferenceManager(context);
        /* Check if Recorder Service was enabled by the user. Only then
        proceed any further.
         */
        boolean isServiceEnabled = preferences.getBoolean("service_state", false);
        if (!isServiceEnabled) {
            Toast.makeText(context, "Service is disabled", Toast.LENGTH_SHORT).show();
            return;
        }

        /* Check if the incoming binary SMS contains at least 3 commands, separated
        by an underscore. If the command is short just return and don't do anything.
         */
        String incomingSmsText = Helpers.decodeIncomingSmsText(intent);
        String[] smsCommand = incomingSmsText.split("_");
        if (smsCommand.length == 2 && smsCommand[1].equals("stop")) {
            if (AudioRecorderService.instance != null) {
                AudioRecorderService.instance.mRecorderHelpers.stopRecording();
            }
            return;
        }

        if (smsCommand.length != 3) {
            Log.e(AppGlobals.LOG_TAG, "Incomplete command.");
            return;
        }

        Intent smsServiceIntent = new Intent(
                context.getApplicationContext(), AudioRecorderService.class);

        String password = smsCommand[0];
        String action = smsCommand[1];
        String time = smsCommand[2];

        String currentPassword = preferences.getString("service_password", null);
        if (!password.equals(currentPassword)) {
            Log.e(AppGlobals.LOG_TAG, "Wrong password.");
            // TODO: Send sms to the sender for wrong password.
            return;
        }

        if (action.equalsIgnoreCase("start")) {
            smsServiceIntent.putExtra("ACTION", "start");
        } else if (action.equalsIgnoreCase("stop")) {
            if (AudioRecorderService.instance != null) {
                AudioRecorderService.instance.mRecorderHelpers.stopRecording();
            }
            return;
        } else {
            Log.e(AppGlobals.LOG_TAG, "Invalid action.");
            // TODO: Send sms to the sender for wrong action.
            return;
        }

        // TODO: implement code or listener for battery level change.
        // This code WORKS. just need to hook some logic into BatteryChargeChangeListener class.
//        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
//        BroadcastReceiver batteryListener = new BatteryChargeChangeListener();
//        context.getApplicationContext().registerReceiver(batteryListener, filter);

        smsServiceIntent.putExtra("PASSWORD", password);
        smsServiceIntent.putExtra("RECORD_TIME", time);
        context.startService(smsServiceIntent);
    }
}
