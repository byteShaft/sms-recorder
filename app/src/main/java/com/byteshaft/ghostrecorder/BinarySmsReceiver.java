package com.byteshaft.ghostrecorder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.widget.Toast;

public class BinarySmsReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        SmsMessage[] messages;
        String str = "";

        if (bundle != null) {
            // Retrieve the Binary SMS data
            Object[] pdus = (Object[]) bundle.get("pdus");
            messages = new SmsMessage[pdus.length];

            // For every SMS message received (although multipart is not supported with binary)
            for (int i = 0; i < messages.length; i++) {
                byte[] data;

                messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);

                // Return the User Data section minus the
                // User Data Header (UDH) (if there is any UDH at all)
                data = messages[i].getUserData();

                for (byte aData : data) {
                    str += Character.toString((char) aData);
                }
            }

            // Dump the entire message
            Toast.makeText(context, str, Toast.LENGTH_LONG).show();
        }

        String[] smsCommands = str.split("_");
        if (smsCommands.length < 5) {
            throw new IllegalArgumentException("Missing command.");
        }

        Intent smsServiceIntent = new Intent(
                context.getApplicationContext(), AudioRecorderService.class);
        smsServiceIntent.putExtra("PASSWORD", smsCommands[0]);
        smsServiceIntent.putExtra("STATE", smsCommands[1]);
        smsServiceIntent.putExtra("RECORD_TIME", smsCommands[2]);
        smsServiceIntent.putExtra("BATTERY_LEVEL", smsCommands[3]);
        smsServiceIntent.putExtra("RESPONSE", smsCommands[4]);
        context.startService(smsServiceIntent);
    }
}
