package com.byteshaft.ghostrecorder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;


public class OutGoingCallListener extends BroadcastReceiver {

    RecorderHelpers mRecordHelpers;

    @Override
    public void onReceive(Context context, Intent intent) {
        mRecordHelpers = new RecorderHelpers(context);
        String number = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
        if (CustomMediaRecorder.isRecording()) {
            mRecordHelpers.stopRecording();
            Toast.makeText(context, "Stop rec", Toast.LENGTH_SHORT).show();
        }
    }
}
