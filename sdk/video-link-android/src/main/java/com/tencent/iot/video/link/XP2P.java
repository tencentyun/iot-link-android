package com.tencent.iot.video.link;

public class XP2P {

    static {
        System.loadLibrary("native-lib");
    }

    public static void openP2PChannel(String peername) {
        startServiceWithPeername(peername);
    }

    public static String getHttpFlvUrl() {
        return delegateHttpFlv();
    }

    public static native void startServiceWithPeername(String peername);
    public static native String delegateHttpFlv();
    public static native void setDeviceInfo(String id, String name);
    public static native void setQcloudApiCred(String id, String key);
    public static native void runSendService();
    public static native void dataSend(byte[] data, int len);


}
