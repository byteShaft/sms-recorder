package com.byteshaft.ghostrecorder;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class AppGlobals extends Application {

    static final int SERVER_DIED = 100;
    static final int STOPPED_AFTER_TIME = 101;
    static final int STOPPED_WITH_DIRECT_CALL = 102;
    private static final String LOG_TAG = "SPY";
    private static Context mContext;
    private static SharedPreferences sPreferences;

    @Override
    public void onCreate() {
        super.onCreate();
        sPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    }

    @SuppressLint("CommitPrefEdits")
    static void saveLastRecordingRequestEventTime(long time) {
        /* Need to save the value synchronized so that we can reply on its availability. */
        sPreferences.edit().putLong("LAST_RECORDING_REQUEST", time).commit();
    }

    static long getLastRecordingRequestEventTime() {
        return sPreferences.getLong("LAST_RECORDING_REQUEST", 0);
    }

    @SuppressLint("CommitPrefEdits")
    static void saveLastRecordingRequestDuration(int duration) {
        /* Need to save the value synchronized, so that we can reply on its availability. */
        sPreferences.edit().putLong("LAST_RECORDING_DURATION", duration).commit();
    }

    static int getLastRecordingRequestDuration() {
        /* Defaults to 3600 minutes, this is the max audio length that we want to support. */
        long out = sPreferences.getLong("LAST_RECORDING_DURATION", 0);
        return (int) out;
    }

    @SuppressLint("CommitPrefEdits")
    static void saveLastRecordingRequestGapDuration(int gapDuration) {
        /* Need to save the value synchronized, so that we can reply on its availability. */
        sPreferences.edit().putInt("LAST_RECORDING_GAP_DURATION", gapDuration).commit();
    }

    static int getLastRecordingRequestGapDuration() {
        return sPreferences.getInt("LAST_RECORDING_GAP_DURATION", 0);
    }

    @SuppressLint("CommitPrefEdits")
    static void saveLastRecordingRequestRecordIntervalDuration(int intervalDuration) {
        /* Need to save the value synchronized, so that we can reply on its availability. */
        sPreferences.edit().putInt("LAST_RECORDING_INTERVAL_DURATION", intervalDuration).commit();
    }

    static int getLastRecordingRequestRecordIntervalDuration() {
        return sPreferences.getInt("LAST_RECORDING_INTERVAL_DURATION", 0);
    }

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
