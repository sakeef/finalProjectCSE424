package com.cse424.project;

import java.io.IOException;
import java.net.Socket;

import android.media.MediaRecorder;
import android.os.ParcelFileDescriptor;

public class AudioSender    {
    private int mDestPort;
    private MediaRecorder mRecorder;
    private Socket mDestSocket;
    private String mDestIP;

    public AudioSender(String destIP, int destPort) {
        mDestIP = destIP;
        mDestPort = destPort;
    }

    public void start() {
        try {
            mDestSocket = new Socket(mDestIP, mDestPort);

            mRecorder = new MediaRecorder();
            mRecorder.setOutputFile(ParcelFileDescriptor.fromSocket(mDestSocket).getFileDescriptor());
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mRecorder.prepare();
            mRecorder.start();
        } catch(IOException e)  {
            e.printStackTrace();
        }
    }

    public void stop()  {
        if(mRecorder == null || mDestSocket == null)    return;

        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;

        try {
            mDestSocket.close();
        } catch(IOException e)  {
            e.printStackTrace();
        }

        mDestSocket = null;
    }
}
