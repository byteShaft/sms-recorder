package com.byteshaft.ghostrecorder;

import android.media.MediaRecorder;
import android.os.Handler;
import android.util.Log;

public class CustomMediaRecorder extends MediaRecorder {

    private static boolean sIsRecording;
    private int mDuration;
    private Handler mHandler;
    private static boolean mIsUsable = true;
    private static CustomMediaRecorder mCustomMediaRecorder;

    static CustomMediaRecorder getInstance() {
        if (mCustomMediaRecorder == null) {
            mCustomMediaRecorder = new CustomMediaRecorder();
            return mCustomMediaRecorder;
        } else if (!isUsable()) {
            mCustomMediaRecorder = new CustomMediaRecorder();
            return mCustomMediaRecorder;
        } else {
            return mCustomMediaRecorder;
        }
    }

    private CustomMediaRecorder() {
        mHandler = new Handler();
    }

    static boolean isRecording() {
        return sIsRecording;
    }

    private void setIsRecording(boolean state) {
        sIsRecording = state;
    }

    void setDuration(int time) {
        mDuration = time;
    }

    static boolean isUsable() {
        return mIsUsable;
    }

    private void setIsUsable(boolean usable) {
        mIsUsable = usable;
    }

    @Override
    public void start() throws IllegalStateException {
        super.start();
        if (mDuration > 0) {
            mHandler.postDelayed(stopper, mDuration);
        }
        setIsRecording(true);
    }

    @Override
    public void stop() throws IllegalStateException {
        super.stop();
        mHandler.removeCallbacks(null);
        setIsRecording(false);
        Log.i(AppGlobals.LOG_TAG, "Stopped recording");
    }

    @Override
    public void reset() {
        super.reset();
        setIsUsable(false);
    }

    private Runnable stopper = new Runnable() {
        @Override
        public void run() {
            if (isRecording()) {
                stop();
            }
        }
    };
}
