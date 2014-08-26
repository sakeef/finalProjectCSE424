package com.cse424.project;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;

public class AudioActivity extends ActionBarActivity {
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.activity_audio);

        final AudioSender sender = new AudioSender("192.168.0.101", 9998);
        final AudioReceiver receiver = new AudioReceiver(9998);

        findViewById(R.id.recordButton).setOnClickListener(new View.OnClickListener()   {
            @Override
            public void onClick(View v) {
                if(sender.isRunning())  {
                    sender.free();
                } else  {
                    sender.start();
                }
            }
        });

        findViewById(R.id.listenButton).setOnClickListener(new View.OnClickListener()   {
            @Override
            public void onClick(View v) {
                if(receiver.isRunning())    {
                    receiver.free();
                } else  {
                    receiver.start();
                }
            }
        });
    }
}
