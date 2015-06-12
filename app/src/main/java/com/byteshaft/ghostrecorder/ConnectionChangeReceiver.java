package com.byteshaft.ghostrecorder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;

public class ConnectionChangeReceiver extends BroadcastReceiver {
    private UploadRecordingTaskHelpers mUploadHelpers;
    private UploadRecordingTask uploadRecordingTask;
    private Context mContext;
    private RecordingDatabaseHelper recordingDatabaseHelper;
    private String LOG_TAG = AppGlobals.getLogTag(getClass());
    static boolean sUploadingPrevious = false;

    @Override
    public void onReceive(Context context, Intent intent)
    {
        mContext = context;
        recordingDatabaseHelper = new RecordingDatabaseHelper(mContext);
        mUploadHelpers = new UploadRecordingTaskHelpers(context);
        if (mUploadHelpers.isNetworkAvailable()) {
            int network = mUploadHelpers.networkAvailable();
            if (network == 0) {
                Log.i(LOG_TAG, "Ping success");
                deletePreviousUploadFailFileOnServer();
            }
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

    class Task extends AsyncTask<ArrayList<String>, String, String>{

        @Override
        protected String doInBackground(ArrayList<String>... params) {
            uploadRecordingTask = new UploadRecordingTask(mContext);
            uploadRecordingTask.deletePreviousUploadFailedRecordings(params[0]);
            return null;
        }
    }
}

