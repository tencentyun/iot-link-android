package com.tencent.iot.explorer.link.rtc.model;

public interface TRTCCallingParamsCallback {

    void joinRoom(Integer callingType, String deviceId, RoomKey roomKey);

    void exitRoom();

    void userBusy();
}
