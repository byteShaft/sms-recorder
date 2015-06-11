package com.byteshaft.ghostrecorder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlarmReciever extends BroadcastReceiver {

    RecorderHelpers mRecordHelpers;

    @Override
    public void onReceive(Context context, Intent intent) {
        mRecordHelpers = new RecorderHelpers(context);
        mRecordHelpers.startRecording(AudioRecorderService.recordTime);
        System.out.println("Started Scheduled Recording...");
    }
}
