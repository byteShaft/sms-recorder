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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public class Helpers extends ContextWrapper {

    String path = Environment.getExternalStorageDirectory().toString() + "/Recordings";

    public Helpers(Context base) {
        super(base);
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

    ArrayList<String> getAllFilesFromDir() {
        Log.d("Files", "Path: " + path);
        File f = new File(path);
        File file[] = f.listFiles();
        ArrayList<String> arrayList = new ArrayList<String>();
        Log.d("Files", "Size: "+ file.length);
        for (int i=0; i < file.length; i++)
        {
            arrayList.add(file[i].getName());
            Log.d("Files", "FileName:" + file[i].getName());
        }
        return arrayList;
    }

    TelephonyManager getTelephonyManager() {
        return (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
    }
}
