package com.tencent.iot.video.link;

public class XP2P {

    static {
        System.loadLibrary("native-lib");
    }

    public static native void startServiceWithXp2pInfo(String peername);
    public static native void stopService();
    public static native String delegateHttpFlv();
    public static native void setDeviceInfo(String id, String name);
    public static native void setQcloudApiCred(String id, String key);
    public static native void runSendService();
    public static native void stopSendService(byte[] data);
    public static native void dataSend(byte[] data, int len);
    public static native void setXp2pInfoAttributes(String attributes);


}
