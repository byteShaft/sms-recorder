package com.byteshaft.ghostrecorder;


import android.content.Context;
import android.content.ContextWrapper;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class UploadRecordingTaskHelpers extends ContextWrapper {

    private final String LOGTAG = AppGlobals.LOG_TAG + "/" + getClass().getName();

    boolean currentNetworkState = false;


    public UploadRecordingTaskHelpers(Context base) {
        super(base);
    }

    boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    class checkNetwworkState extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {

            return null;
        }
    }
}

