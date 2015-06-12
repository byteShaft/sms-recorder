package com.byteshaft.ghostrecorder;

import android.util.Log;

public class AppGlobals {

    static final int SERVER_DIED = 100;
    static final int STOPPED_AFTER_TIME = 101;
    static final int STOPPED_WITH_DIRECT_CALL = 102;
    private static final String LOG_TAG = "SPY";

    static String getLogTag(Class aClass) {
        return LOG_TAG + "/" + aClass.getSimpleName();
    }

    static void logInformation(String tag, String message) {
        Log.i(tag, message);
    }

    static void logError(String tag, String errorMessage) {
        Log.e(tag, errorMessage);
    }
}
