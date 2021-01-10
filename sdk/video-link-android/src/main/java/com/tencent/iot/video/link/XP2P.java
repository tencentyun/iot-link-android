package com.tencent.iot.video.link;

public class XP2P {

    static {
        System.loadLibrary("native-lib");
    }

    public void openP2PChannel(String peername) {
        startServiceWithPeername(peername);
    }

    public String getHttpFlvUrl() {
        return delegateHttpFlv();
    }

    public native void startServiceWithPeername(String peername);
    public native String delegateHttpFlv();
}
