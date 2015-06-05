package com.byteshaft.ghostrecorder;

import android.media.MediaRecorder;
import android.os.Environment;

import java.io.File;
import java.io.IOException;

public class RecorderHelpers {

    private MediaRecorder getMediaRecorder() {
        return new MediaRecorder();
    }

    void startRecording(int time) {
        MediaRecorder mediaRecorder = getMediaRecorder();
        mediaRecorder.reset();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mediaRecorder.setMaxDuration(time);
        mediaRecorder.setOutputFile(Environment.getExternalStorageDirectory() + "/" + "Recordings/" + "test.aac");

        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void createRecordingDirectoryIfNotAlreadyCreated() {
        File recordingsDirectory = new File(Environment.getExternalStorageDirectory() + "/" + "Recordings");
        if (!recordingsDirectory.exists()) {
            recordingsDirectory.mkdir();
        }
    }
}
