package com.tencent.iot.explorer.link.rtc.model;

public class TRTCUIManager {

    private static TRTCUIManager instance;

    private TRTCCallingParamsCallback callingParamsCallback = null;

    private TRTCSessionManager sessionManager = null;

    public Boolean isCalling = false;

    public int callStatus = TRTCCallStatus.TYPE_IDLE_OR_REFUSE.getValue(); //应用端音视频呼叫状态

    public String deviceId = "";

    public synchronized static TRTCUIManager getInstance() {
        if (instance == null) {
            instance = new TRTCUIManager();
        }
        return instance;
    }

    public void setSessionManager(TRTCSessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    public void addCallingParamsCallback(TRTCCallingParamsCallback callingParamsCallback) {
        this.callingParamsCallback = callingParamsCallback;
    }

    public void removeCallingParamsCallback() {
        this.callingParamsCallback = null;
        sessionManager.resetTRTCStatus();
    }

    public void didAcceptJoinRoom(Integer callingType, String deviceId) {
        sessionManager.joinRoom(callingType, deviceId);
    }

    public void refuseEnterRoom(Integer callingType, String deviceId) {
        sessionManager.exitRoom(callingType, deviceId);
    }

    public void joinRoom(Integer callingType, String deviceId, RoomKey roomKey) {
        if (callingParamsCallback != null) {
            callingParamsCallback.joinRoom(callingType, deviceId, roomKey);
        }
    }

    public void exitRoom() {
        if (callingParamsCallback != null) {
            callingParamsCallback.exitRoom();
        }
    }

    public void userBusy() {
        if (callingParamsCallback != null) {
            callingParamsCallback.userBusy();
        }
    }

    public void otherUserAccept() {
        if (callingParamsCallback != null) {
            callingParamsCallback.otherUserAccept();
        }
    }
}
