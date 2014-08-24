package com.cse424.project;

import java.io.IOException;
import java.net.Socket;

import android.media.MediaRecorder;
import android.os.ParcelFileDescriptor;

public class AudioSender    {
    private int mDestPort;
    private MediaRecorder mRecorder;
    private String mDestIP;

    public AudioSender(String destIP, int destPort) {
        mDestIP = destIP;
        mDestPort = destPort;
    }

    public void start() {
        try {
            ParcelFileDescriptor pfd = ParcelFileDescriptor.fromSocket(new Socket(mDestIP, mDestPort));

            mRecorder = new MediaRecorder();
            mRecorder.setOutputFile(pfd.getFileDescriptor());
            mRecorder.prepare();
            mRecorder.start();
        } catch(IOException e)  {
            e.printStackTrace();
        }
    }

    public void stop()  {
        if(mRecorder == null)   return;

        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;
    }
}
