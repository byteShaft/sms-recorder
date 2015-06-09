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
}