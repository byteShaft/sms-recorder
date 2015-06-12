package com.byteshaft.ghostrecorder;

import android.content.Context;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

public class CallStateListener extends PhoneStateListener {

    RecorderHelpers mRecordHelpers;
    Context context;

    @Override
    public void onCallStateChanged(int state, String incomingNumber) {

        super.onCallStateChanged(state, incomingNumber);
        switch (state) {
            case TelephonyManager.CALL_STATE_RINGING:
                if (CustomMediaRecorder.isRecording()) {
                    mRecordHelpers.stopRecording();
                }
                break;
        }
    }
}
