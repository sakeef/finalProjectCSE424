package com.cse424.project;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class AudioCall  {
    private static final int INCOMING_CALL_LISTENER_PORT = 8888;
    private static AudioCall mSharedInstance;
    private AudioCallListener mListener;
    private CallStatus mStatus;

    public static enum CallStatus   {
        IN_CALL, LISTENING, FREE, INCOMING, REJECTED, CALLING, ENDED
    }

    public static AudioCall getInstance(AudioCallListener listener) {
        if(mSharedInstance == null) mSharedInstance = new AudioCall(listener);

        return mSharedInstance;
    }

    private AudioCall(AudioCallListener listener)  {
        mListener = listener;

        setCallStatus(CallStatus.FREE);
    }

    private CallStatus getCallStatus()  {
        return mStatus;
    }

    private void setCallStatus(CallStatus status)   {
        mStatus = status;

        updateStatus("status changed to: " + status.name());
    }

    private boolean canMakeNewCall()    {
        return !(getCallStatus().equals(CallStatus.CALLING) || getCallStatus().equals(CallStatus.IN_CALL) || getCallStatus().equals(CallStatus.INCOMING));
    }

    private boolean canEndCall()    {
        return getCallStatus().equals(CallStatus.CALLING) || getCallStatus().equals(CallStatus.IN_CALL);
    }

    private boolean canListenForIncomingCall()  {
        return getCallStatus().equals(CallStatus.FREE) || getCallStatus().equals(CallStatus.REJECTED) || getCallStatus().equals(CallStatus.ENDED);
    }

    public void makeNewCall(String destIP)  {
        if(canMakeNewCall())    {
            stopListeningForIncomingCall();
            setCallStatus(CallStatus.CALLING);
            new MakeCallThread(destIP, this).start();
        }
    }

    public void endCall()   {
        if(canEndCall())    {
            setCallStatus(CallStatus.ENDED);
            startListeningForIncomingCall();
        }
    }

    public void rejectCall()    {
        if(getCallStatus().equals(CallStatus.INCOMING)) {
            setCallStatus(CallStatus.REJECTED);
        }
    }

    public void receiveCall()   {
        if(getCallStatus().equals(CallStatus.INCOMING)) {
            stopListeningForIncomingCall();
            setCallStatus(CallStatus.IN_CALL);
        }
    }

    public void startListeningForIncomingCall() {
        if(canListenForIncomingCall())  {
            setCallStatus(CallStatus.LISTENING);
            new IncomingCallListenerThread(this).start();
        }
    }

    public void stopListeningForIncomingCall()  {
        if(getCallStatus().equals(CallStatus.LISTENING))    setCallStatus(CallStatus.FREE);
    }

    public void onIncomingCall()    {
        if(getCallStatus().equals(CallStatus.LISTENING))    {
            setCallStatus(CallStatus.INCOMING);

            if(mListener != null)   mListener.onIncomingCall();
        }
    }

    public void updateStatus(String status) {
        if(mListener != null)   mListener.updateStatus(status);
    }

    private static class IncomingCallListenerThread extends Thread  {
        private AudioCall mAudioCall;
        private AudioSender serverAudioSender;
        private AudioReceiver serverAudioReceiver;

        public IncomingCallListenerThread(AudioCall audioCall)  {
            mAudioCall = audioCall;
        }

        @Override
        public void run()   {
            ServerSocket mServerSocket = null;
            Socket mSocket = null;

            try {
                mServerSocket = new ServerSocket(INCOMING_CALL_LISTENER_PORT);
            } catch(IOException e)  {
                e.printStackTrace();
            }

            if(mServerSocket != null)   {
                if(mAudioCall.getCallStatus().equals(CallStatus.LISTENING)) {
                    mAudioCall.updateStatus("listening for incoming call...");

                    try {
                        mSocket = mServerSocket.accept();

                        if(mSocket != null) mAudioCall.onIncomingCall();
                    } catch(IOException e)  {
                        e.printStackTrace();
                    }
                }

                if(mSocket != null) {
                    int count = 0;
                    ObjectOutputStream outStream;

                    try {
                        outStream = new ObjectOutputStream(mSocket.getOutputStream());

                        while(mAudioCall.getCallStatus().equals(CallStatus.INCOMING) && count < 15) {
                            mAudioCall.updateStatus("call from " + mSocket.getInetAddress().toString() + "...");
                            outStream.writeObject(mAudioCall.getCallStatus().name());
                            outStream.flush();

                            try {
                                Thread.sleep(1000);
                            } catch(InterruptedException e) {
                                e.printStackTrace();
                            }

                            count++;
                        }

                        if(mAudioCall.getCallStatus().equals(CallStatus.INCOMING))  {
                            mAudioCall.setCallStatus(CallStatus.ENDED);
                            outStream.writeObject(mAudioCall.getCallStatus().name());
                            outStream.flush();
                        } else if(mAudioCall.getCallStatus().equals(CallStatus.REJECTED))   {
                            outStream.writeObject(mAudioCall.getCallStatus().name());
                            outStream.flush();
                        } else if(mAudioCall.getCallStatus().equals(CallStatus.IN_CALL))    {
                            outStream.writeObject(mAudioCall.getCallStatus().name());
                            outStream.flush();
                        }

                        if(mAudioCall.getCallStatus().equals(CallStatus.IN_CALL))   {
                            serverAudioSender = new AudioSender(mSocket);
                            serverAudioReceiver = new AudioReceiver(mSocket);

                            Thread t = new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    while(mAudioCall.getCallStatus().equals(CallStatus.IN_CALL))    {
                                        try {
                                            Thread.sleep(1000);
                                        } catch(InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                    }

                                    serverAudioSender.setRunning(false);
                                    serverAudioReceiver.setRunning(false);
                                }
                            });

                            t.start();
                            serverAudioSender.start();
                            serverAudioReceiver.start();
                        }


                        if(!mAudioCall.getCallStatus().equals(CallStatus.IN_CALL))  {
                            outStream.close();
                            mSocket.close();
                        }
                    } catch(IOException e)  {
                        e.printStackTrace();
                    }
                }

                if(!mAudioCall.getCallStatus().equals(CallStatus.IN_CALL))  {
                    try {
                        mServerSocket.close();
                    } catch(IOException e)  {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private static class MakeCallThread extends Thread  {
        private String mDestIP;
        private AudioCall mAudioCall;
        private AudioSender clientAudioSender;
        private AudioReceiver clientAudioReceiver;

        public MakeCallThread(String destIP, AudioCall audioCall)   {
            mDestIP = destIP;
            mAudioCall = audioCall;
        }

        @Override
        public void run()   {
            Socket mSocket = null;

            try {
                mSocket = new Socket(mDestIP, AudioCall.INCOMING_CALL_LISTENER_PORT);
            } catch(IOException e)  {
                e.printStackTrace();
            }

            if(mSocket != null) {
                int count = 0;
                ObjectInputStream inStream = null;

                while(count < 15)   {
                    try {
                        if(inStream == null)    inStream = new ObjectInputStream(mSocket.getInputStream());

                        String status = (String) inStream.readObject();

                        if(status != null)  {
                            if(status.equalsIgnoreCase(CallStatus.REJECTED.name())) {
                                mAudioCall.setCallStatus(CallStatus.REJECTED);
                                break;
                            } else if(status.equalsIgnoreCase(CallStatus.IN_CALL.name()))   {
                                mAudioCall.setCallStatus(CallStatus.IN_CALL);
                                break;
                            } else if(status.equalsIgnoreCase(CallStatus.ENDED.name())) {
                                mAudioCall.setCallStatus(CallStatus.ENDED);
                            }
                        }
                    } catch(IOException e)  {
                        e.printStackTrace();
                    } catch(ClassNotFoundException e)   {
                        e.printStackTrace();
                    }

                    mAudioCall.updateStatus("Calling...");

                    try {
                        Thread.sleep(1000);
                    } catch(InterruptedException e) {
                        e.printStackTrace();
                    }

                    count++;
                }

                if(mAudioCall.getCallStatus().equals(CallStatus.CALLING)) {
                    mAudioCall.updateStatus("Call ended, receiver not picking up");
                    mAudioCall.setCallStatus(CallStatus.FREE);
                }

                if(mAudioCall.getCallStatus().equals(CallStatus.IN_CALL))   {
                    clientAudioSender = new AudioSender(mSocket);
                    clientAudioReceiver = new AudioReceiver(mSocket);

                    Thread t = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            while(mAudioCall.getCallStatus().equals(CallStatus.IN_CALL))    {
                                try {
                                    Thread.sleep(1000);
                                } catch(InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }

                            clientAudioSender.setRunning(false);
                            clientAudioReceiver.setRunning(false);
                        }
                    });

                    t.start();
                    clientAudioSender.start();
                    clientAudioReceiver.start();
                }

                if(!mAudioCall.getCallStatus().equals(CallStatus.IN_CALL))  {
                    try {
                        if(inStream != null)    inStream.close();

                        mSocket.close();
                    } catch(IOException e)  {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
