package com.cse424.project;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.LinkedList;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;

public class AudioSender extends Thread {
    private boolean mKeepRunning;
    private Socket mSocket;

    public AudioSender(Socket socket)   {
        mSocket = socket;
        mKeepRunning = true;
    }

    public boolean isRunning()  {
        return mKeepRunning;
    }

    public void setRunning(boolean running)  {
        mKeepRunning = running;
    }

    @Override
    public void run()   {
        int mOutBufferSize = AudioTrack.getMinBufferSize(8000, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
        int mInBufferSize = AudioTrack.getMinBufferSize(8000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        int maxBuffer = Math.max(mInBufferSize, mOutBufferSize);
        AudioRecord mInRec = new AudioRecord(MediaRecorder.AudioSource.MIC, 8000, AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT, maxBuffer);

        byte[] mInBytes = new byte[maxBuffer];
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
                mInRec.read(mInBytes, 0, maxBuffer);
                bytes_pkg = mInBytes.clone();

                if(mInQueue.size() >= 2 && outStream != null)   outStream.write(mInQueue.removeFirst(), 0, maxBuffer);

                mInQueue.add(bytes_pkg);
            }

            mInRec.stop();

            if(outStream != null)   outStream.close();
        } catch(IOException e)  {
            e.printStackTrace();
        }
    }
}