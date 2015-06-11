package com.byteshaft.ghostrecorder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlarmReceiver extends BroadcastReceiver {

    private String LOG_TAG = AppGlobals.getLogTag(getClass());

    @Override
    public void onReceive(Context context, Intent intent) {
        /* Start when the set alarm for recording goes off.
        This broadcast is generally expected to be received
        as a result of a scheduled recording command.
         */
        RecorderHelpers recorderHelpers = new RecorderHelpers(context);
        recorderHelpers.startRecording(AudioRecorderService.recordTime);
        AppGlobals.logInformation(LOG_TAG, "Started Scheduled Recording...");
    }
}
