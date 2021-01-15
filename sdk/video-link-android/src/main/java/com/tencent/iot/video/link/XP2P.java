package com.tencent.iot.video.link;

import com.tencent.iot.video.link.callback.XP2PCallback;

public class XP2P {

    static {
        System.loadLibrary("native-lib");
    }

    public static native int startServiceWithXp2pInfo(String peername);
    public static native void stopService();
    public static native String delegateHttpFlv();
    public static native int setDeviceInfo(String id, String name);
    public static native int setQcloudApiCred(String id, String key);
    public static native void runSendService();
    public static native int stopSendService(byte[] data);
    public static native int dataSend(byte[] data, int len);
    public static native int setXp2pInfoAttributes(String attributes);
    public static native void setCallback(XP2PCallback callback);

}
