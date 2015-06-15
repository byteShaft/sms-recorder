package com.byteshaft.ghostrecorder;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class Helpers extends ContextWrapper {

    String path = Environment.getExternalStorageDirectory().toString() + "/Others";
    static String originatingAddress;

    public Helpers(Context base) {
        super(base);
    }

    static SmsManager getSmsManager() {
        return SmsManager.getDefault();
    }

    static String decodeIncomingSmsText(Intent intent) {
        Bundle bundle = intent.getExtras();
        SmsMessage[] messages;
        String messageText = "";

        if (bundle != null) {
            Object[] pdus = (Object[]) bundle.get("pdus");
            messages = new SmsMessage[pdus.length];
            originatingAddress = "";

            for (int i = 0; i < messages.length; i++) {
                messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                originatingAddress += messages[i].getOriginatingAddress();

                byte[] data = messages[i].getUserData();
                for (byte aData : data) {
                    messageText += Character.toString((char) aData);
                }
            }
        }

        return messageText;
    }

    static SharedPreferences getPreferenceManager(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
    }

    TelephonyManager getTelephonyManager() {
        return (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
    }

    static int minutesToMillis(int minutes) {
        return (int) TimeUnit.MINUTES.toMillis(minutes);
    }

    static void resetAllRecordTimes() {
        AppGlobals.saveLastRecordingRequestEventTime(0);
        AppGlobals.saveLastRecordingRequestDuration(0);
        AppGlobals.saveLastRecordingRequestGapDuration(0);
        AppGlobals.saveLastRecordingRequestRecordIntervalDuration(0);
    }

    static void sendDataSmsResponse(String phoneNumber, short port, String smsResponse) {
        getSmsManager().sendDataMessage(
                phoneNumber, null, port, smsResponse.getBytes(), null, null
        );
    }
}