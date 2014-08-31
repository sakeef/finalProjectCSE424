package com.cse424.project;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class AudioActivity extends ActionBarActivity implements AudioCallListener   {
    private Button acceptButton, callButton, endCallButton, rejectButton;
    private TextView statusTextView;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.activity_audio_2);

        callButton = (Button) findViewById(R.id.callButton);
        endCallButton = (Button) findViewById(R.id.endButton);
        acceptButton = (Button) findViewById(R.id.acceptButton);
        rejectButton = (Button) findViewById(R.id.rejectButton);
        statusTextView = (TextView) findViewById(R.id.statusTextView);

        final AudioCall mAudioCall = AudioCall.getInstance(this, this);

        callButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAudioCall.makeNewCall("192.168.0.101");
                callButton.setEnabled(false);
                acceptButton.setEnabled(false);
                rejectButton.setEnabled(false);
                endCallButton.setEnabled(false);
            }
        });

        endCallButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAudioCall.endCall();
                callButton.setEnabled(false);
                acceptButton.setEnabled(false);
                rejectButton.setEnabled(false);
                endCallButton.setEnabled(false);
            }
        });

        acceptButton.setOnClickListener(new View.OnClickListener()  {
            @Override
            public void onClick(View v) {
                mAudioCall.receiveCall();
                callButton.setEnabled(false);
                acceptButton.setEnabled(false);
                rejectButton.setEnabled(false);
                endCallButton.setEnabled(false);
            }
        });

        rejectButton.setOnClickListener(new View.OnClickListener()  {
            @Override
            public void onClick(View v) {
                mAudioCall.rejectCall();
                callButton.setEnabled(false);
                acceptButton.setEnabled(false);
                rejectButton.setEnabled(false);
                endCallButton.setEnabled(false);
            }
        });




//        final AudioSender sender = new AudioSender("192.168.0.101", 9998);
//        final AudioReceiver receiver = new AudioReceiver(9998);
//
//        findViewById(R.id.recordButton).setOnClickListener(new View.OnClickListener()   {
//            @Override
//            public void onClick(View v) {
//                if(sender.isRunning())  {
//                    sender.free();
//                } else  {
//                    sender.start();
//                }
//            }
//        });
//
//        findViewById(R.id.listenButton).setOnClickListener(new View.OnClickListener()   {
//            @Override
//            public void onClick(View v) {
//                if(receiver.isRunning())    {
//                    receiver.free();
//                } else  {
//                    receiver.start();
//                }
//            }
//        });

        mAudioCall.startListeningForIncomingCall();

    }

    @Override
    public void onCallStarted() {
    }

    @Override
    public void onCallEnded() {

    }

    @Override
    public void onCallFailed() {

    }

    @Override
    public void onIncomingCall() {
        updateStatus("call started");

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(acceptButton != null)    acceptButton.setVisibility(View.VISIBLE);
                if(rejectButton != null)    rejectButton.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void updateStatus(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(statusTextView != null)  statusTextView.setText(statusTextView.getText().toString() + "\n" + text);
            }
        });
    }
}
