package com.cse424.project;

public interface AudioCallListener  {
    public void onCallStarted();

    public void onCallEnded();

    public void onCallFailed();

    public void onIncomingCall();

    public void updateStatus(String text);
}
