package com.byteshaft.ghostrecorder;


import android.content.Context;
import android.content.ContextWrapper;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class UploadRecordingTaskHelpers extends ContextWrapper {

    private final String LOG_TAG = AppGlobals.getLogTag(getClass());

    public UploadRecordingTaskHelpers(Context base) {
        super(base);
    }

    boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    int networkAvailable() {
        int returnVal = 0;
        if (isNetworkAvailable()) {
            Process p1;
            try {
                p1 = Runtime.getRuntime().exec("ping -c 1 www.google.com");
                returnVal = p1.waitFor();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            }
        return returnVal;

        }

    void deleteFile(File f) throws IOException {
        if (f.isDirectory()) {
            for (File c : f.listFiles()) {
                if (c.exists()) {
                    c.delete();
                }
            }
        }
    }
}

