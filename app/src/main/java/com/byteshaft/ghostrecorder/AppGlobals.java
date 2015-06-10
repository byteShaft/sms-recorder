package com.byteshaft.ghostrecorder;


public class AppGlobals {

    static final String LOG_TAG = "SPY";

    private static boolean isRecording;

    static boolean isRecording() {
        return isRecording;
    }

    static void setIsRecording(boolean state) {
        isRecording = state;
    }

    static final int SERVER_DIED = 100;
    static final int STOPPED_AFTER_TIME = 101;
    static final int STOPPED_WITH_DIRECT_CALL = 102;
}