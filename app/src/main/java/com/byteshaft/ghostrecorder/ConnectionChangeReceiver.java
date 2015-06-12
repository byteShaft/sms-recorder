package com.byteshaft.ghostrecorder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;

public class ConnectionChangeReceiver extends BroadcastReceiver {
    UploadRecordingTaskHelpers mUploadHelpers;
    UploadRecordingTask uploadRecordingTask;
    Context mContext;
    RecordingDatabaseHelper recordingDatabaseHelper;
    private String LOG_TAG = AppGlobals.getLogTag(getClass());

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
        System.out.println("array is Up now");
        if (listToBeDelete.size() >= 0) {
            new Task().execute(listToBeDelete);
            System.out.println("after data delete");
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

