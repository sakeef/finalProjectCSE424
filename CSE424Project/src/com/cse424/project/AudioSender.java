package com.cse424.project;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.LinkedList;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

public class AudioSender extends Thread {
    private boolean mKeepRunning;
    private Socket mSocket;

    public AudioSender(Socket socket)   {
        mSocket = socket;
    }

    public boolean isRunning()  {
        return mKeepRunning;
    }

    public void free()  {
        mKeepRunning = false;
    }

    @Override
    public void run()   {
        int mInBufferSize = AudioRecord.getMinBufferSize(44100, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT);
        AudioRecord mInRec = new AudioRecord(MediaRecorder.AudioSource.MIC, 44100, AudioFormat.CHANNEL_CONFIGURATION_MONO,
                AudioFormat.ENCODING_PCM_16BIT, mInBufferSize);

        byte[] mInBytes = new byte[mInBufferSize];
        mKeepRunning = true;
        LinkedList<byte[]> mInQueue = new LinkedList<byte[]>();
        DataOutputStream outStream = null;

        try {
            outStream = new DataOutputStream(mSocket.getOutputStream());
        } catch(UnknownHostException e) {
            e.printStackTrace();
        } catch(IOException e)  {
            e.printStackTrace();
        }

        try {
            byte[] bytes_pkg;
            mInRec.startRecording();

            while(mKeepRunning) {
                mInRec.read(mInBytes, 0, mInBufferSize);
                bytes_pkg = mInBytes.clone();

                if(mInQueue.size() >= 2 && outStream != null)   outStream.write(mInQueue.removeFirst(), 0, mInQueue.removeFirst().length);

                mInQueue.add(bytes_pkg);
            }

            mInRec.stop();

            if(outStream != null)   outStream.close();
        } catch(IOException e)  {
            e.printStackTrace();
        }
    }
}