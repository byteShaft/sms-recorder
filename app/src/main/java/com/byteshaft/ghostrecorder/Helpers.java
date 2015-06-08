package com.byteshaft.ghostrecorder;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;

public class Helpers {

    void sendDataSms(String phoneNumber, String port, String smsCommand) {
        SmsManager smsManager = getSmsManager();
        Log.i(AppGlobals.LOG_TAG, getSmsFeedbackFormattedMessage(phoneNumber, port, smsCommand));
        smsManager.sendDataMessage(
                phoneNumber, null, Short.valueOf(port), smsCommand.getBytes(), null, null
        );
    }

    private SmsManager getSmsManager() {
        return SmsManager.getDefault();
    }

    private String getSmsFeedbackFormattedMessage(String number, String port, String command) {
        return String.format(
                "Sending data SMS \"%s\" to %s on port number: %s",
                command, number, String.valueOf(port)
        );
    }

    String decodeIncomingSmsText(Intent intent) {
        Bundle bundle = intent.getExtras();
        SmsMessage[] messages;
        String str = "";

        if (bundle != null) {
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
        }

        return str;
    }

    SharedPreferences getPreferenceManager(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }
}
