package com.cse424.project;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

public class AudioReceiver extends Thread {
    private Socket mSocket;
    private boolean mKeepRunning;

    public AudioReceiver(Socket socket)   {
        mSocket = socket;
        mKeepRunning = true;
    }

    public void setRunning(boolean running) {
        mKeepRunning = running;
    }

    public boolean isRunning()  {
        return mKeepRunning;
    }

    @Override
    public void run()   {
        byte[] mOutBytes = null;
        AudioTrack mOutTrack = null;
        DataInputStream inStream = null;
        int maxBuffer = 400;

        try {
            inStream = new DataInputStream(mSocket.getInputStream());
            int mOutBufferSize = AudioTrack.getMinBufferSize(8000, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
            int mInBufferSize = AudioTrack.getMinBufferSize(8000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
            maxBuffer = Math.max(mInBufferSize, mOutBufferSize);
            mOutTrack = new AudioTrack(AudioManager.STREAM_VOICE_CALL, 8000, AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT, maxBuffer, AudioTrack.MODE_STREAM);
            mOutBytes = new byte[maxBuffer];
        } catch(IOException e)  {
            e.printStackTrace();
        }

        byte[] bytes_pkg;

        if(mOutTrack != null)   mOutTrack.play();

        while(mKeepRunning && mOutBytes != null)    {
            try {
                inStream.read(mOutBytes);

                bytes_pkg = mOutBytes.clone();

                mOutTrack.write(bytes_pkg, 0, maxBuffer);
            } catch(IOException e)  {
                e.printStackTrace ();
            }
        }

        if(mOutTrack != null)   mOutTrack.stop();

        try {
            if(inStream != null)    inStream.close();
        } catch(IOException e)  {
            e.printStackTrace ();
        }
    }
}
