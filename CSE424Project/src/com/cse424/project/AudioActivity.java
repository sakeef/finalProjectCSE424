package com.cse424.project;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;

public class AudioActivity extends ActionBarActivity {
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.activity_audio);

        Button startButton = (Button) findViewById(R.id.recordButton);
        final AudioSender sender = new AudioSender("127.0.0.1", 9998);
        final AudioReceiver receiver = new AudioReceiver(9998);

        startButton.setOnClickListener(new View.OnClickListener()   {
            @Override
            public void onClick(View v) {
                if(sender.isRecording())    {
                    sender.stopRecording();
                    receiver.stop();
                } else  {
                    receiver.start();
                    sender.startRecording();
                }
            }
        });

    }
}
