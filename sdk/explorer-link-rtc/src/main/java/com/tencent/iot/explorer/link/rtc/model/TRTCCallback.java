package com.tencent.iot.explorer.link.rtc.model;

public interface TRTCCallback {
    void busy();

    void updateCallStatus(String k, String v, String deviceId);

    void startCall(int type, String deviceId);

    void otherUserAnswered();

    void hungUp();
}
