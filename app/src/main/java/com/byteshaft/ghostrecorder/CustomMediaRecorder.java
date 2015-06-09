package com.byteshaft.ghostrecorder;

import android.media.MediaRecorder;
import android.os.Handler;
import android.util.Log;

import java.util.ArrayList;

public class CustomMediaRecorder extends MediaRecorder {

    private static boolean sIsRecording;
    private int mDuration;
    private Handler mHandler;
    private static boolean mIsUsable = true;
    private static CustomMediaRecorder mCustomMediaRecorder;
    private ArrayList<OnNewFileWrittenListener> mListeners = new ArrayList<>();
    private String mFilePath;

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

    void setOnNewFileWrittenListener(OnNewFileWrittenListener listener) {
        mListeners.add(listener);
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
    public void stop(){
        super.stop();
        mHandler.removeCallbacks(null);
        setIsRecording(false);
        for (OnNewFileWrittenListener listener : mListeners) {
            listener.onNewRecordingCompleted(mFilePath);
        }
        Log.i(AppGlobals.LOG_TAG, "Stopped recording");
    }

    @Override
    public void setOutputFile(String path) throws IllegalStateException {
        super.setOutputFile(path);
        mFilePath = path;
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

    public interface OnNewFileWrittenListener {
        void onNewRecordingCompleted(String path);
    }
}
