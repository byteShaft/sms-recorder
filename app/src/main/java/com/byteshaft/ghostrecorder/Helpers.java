package com.byteshaft.ghostrecorder;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;

import java.io.File;

public class Helpers {

    void sendDataSms(String phoneNumber, String port, String smsCommand) {
        SmsManager smsManager = getSmsManager();
        smsManager.sendDataMessage(
                phoneNumber, null, Short.valueOf(port), smsCommand.getBytes(), null, null);
    }

    private SmsManager getSmsManager() {
        return SmsManager.getDefault();
    }

    static String decodeIncomingSmsText(Intent intent) {
        Bundle bundle = intent.getExtras();
        SmsMessage[] messages;
        String messageText = "";

        if (bundle != null) {
            Object[] pdus = (Object[]) bundle.get("pdus");
            messages = new SmsMessage[pdus.length];

            for (int i = 0; i < messages.length; i++) {
                messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
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

    void getAllFilesFromDir() {
        String path = Environment.getExternalStorageDirectory().toString()+"/Recordings";
        Log.d("Files", "Path: " + path);
        File f = new File(path);
        File file[] = f.listFiles();
        Log.d("Files", "Size: "+ file.length);
        for (int i=0; i < file.length; i++)
        {
            Log.d("Files", "FileName:" + file[i].getName());
        }
    }
}
