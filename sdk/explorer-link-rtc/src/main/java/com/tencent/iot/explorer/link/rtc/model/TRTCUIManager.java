package com.tencent.iot.explorer.link.rtc.model;

import android.text.TextUtils;

import com.tencent.iot.explorer.link.rtc.consts.Common;

import org.json.JSONException;
import org.json.JSONObject;

public class TRTCUIManager {

    private static TRTCUIManager instance;

    private TRTCCallingParamsCallback callingParamsCallback = null;

    private TRTCSessionManager sessionManager = null;

    public Boolean isCalling = false;

    public int callStatus = TRTCCallStatus.TYPE_IDLE_OR_REFUSE.getValue(); //应用端音视频呼叫状态

    public String deviceId = "";

    public String callingDeviceId = ""; //主动呼叫的设备的id

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
        if (sessionManager == null) return;
        sessionManager.resetTRTCStatus();
    }

    public void didAcceptJoinRoom(Integer callingType, String deviceId) {
        if (sessionManager == null) return;
        sessionManager.joinRoom(callingType, deviceId);
    }

    public void refuseEnterRoom(Integer callingType, String deviceId) {
        if (sessionManager == null) return;
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

    public void userOffline(String deviceId) {
        if (callingParamsCallback != null) {
            callingParamsCallback.userOffline(deviceId);
        }
    }

    public void payloadMessage(TRTCPayload trtcPayload, String userId, TRTCCallback callback) {
        String json = trtcPayload.getJson();
        String payload = trtcPayload.getPayload();
        String payloadDeviceId = trtcPayload.getDeviceId();
        try {
            JSONObject jsonObject = new JSONObject(json);
            String action = jsonObject.getString(Common.TRTC_ACTION);
            if (Common.TRTC_DEVICE_CHANGE.equals(action)) { //收到了设备属性改变的wss消息
                JSONObject paramsObject = jsonObject.getJSONObject(Common.TRTC_PARAM);
                String subType = paramsObject.getString(Common.TRTC_SUB_TYPE);
                if (Common.TRTC_REPORT.equals(subType)) { //收到了设备端上报的属性状态改变的wss消息
                    JSONObject payloadParamsObject = new JSONObject(payload);
                    JSONObject payloadParamsJson = payloadParamsObject.getJSONObject(Common.TRTC_PARAM);
                    String method = "";
                    if (payloadParamsObject.has(Common.TRTC_METHOD)) {
                        method = payloadParamsObject.getString(Common.TRTC_METHOD);
                    }
                    if (!Common.TRTC_REPORT_LOW.equals(method)) {
                        return; //过滤掉非report消息
                    }
                    int videoCallStatus = Common.TRTC_STATUS_NONE;
                    if (payloadParamsJson.has(Common.TRTC_VIDEO_CALL_STATUS)) {
                        videoCallStatus = payloadParamsJson.getInt(Common.TRTC_VIDEO_CALL_STATUS);
                    }
                    int audioCallStatus = Common.TRTC_STATUS_NONE;
                    if (payloadParamsJson.has(Common.TRTC_AUDIO_CALL_STATUS)) {
                        audioCallStatus = payloadParamsJson.getInt(Common.TRTC_AUDIO_CALL_STATUS);
                    }

                    String deviceId = "";
                    if (payloadParamsJson.has(Common.TRTC_USERID)) {
                        deviceId = payloadParamsJson.getString(Common.TRTC_USERID);
                    }

                    String rejectId = "";
                    if (payloadParamsJson.has(Common.TRTC_EXTRA_INFO)) {
                        JSONObject extraInfo = new JSONObject(payloadParamsJson.getString(Common.TRTC_EXTRA_INFO));
                        if (extraInfo.has(Common.TRTC_REJECT_USERID)) {
                            rejectId = extraInfo.getString(Common.TRTC_REJECT_USERID);
                        }
                    }

                    if (videoCallStatus == Common.TRTC_STATUS_NONE && audioCallStatus == Common.TRTC_STATUS_NONE
                            && rejectId.isEmpty()) {
                        return; //过滤掉非音视频通话的消息
                    }

                    // 判断主动呼叫的回调中收到_sys_extra_info中的reject_userId为自己的userId，表示设备正和其他设备通话，拒绝了本次呼叫
                    if (!rejectId.isEmpty()) {
                        if (!TextUtils.isEmpty(callingDeviceId) && rejectId.equals(userId) && isCalling) {
                            callback.busy();
                        }
                        return;
                    }

                    // 判断主动呼叫的回调中收到的_sys_userid不为自己的userid则被其他用户抢先呼叫设备了，提示用户 对方正忙...
                    if (!TextUtils.isEmpty(callingDeviceId) && !deviceId.equals(userId)) {
                        if (isCalling) { //当前正显示音视频通话页面，finish掉
                            if (callingDeviceId.equals(userId)) {
                                callback.busy();
                                return;
                            } else { //其他的设备又呼叫了该用户
                                if (videoCallStatus == TRTCCallStatus.TYPE_CALLING.getValue()) {
                                    callback.updateCallStatus(Common.TRTC_VIDEO_CALL_STATUS, "0", payloadDeviceId);
                                    return;
                                } else if (audioCallStatus == TRTCCallStatus.TYPE_CALLING.getValue()) {
                                    callback.updateCallStatus(Common.TRTC_AUDIO_CALL_STATUS, "0", payloadDeviceId);
                                    return;
                                }
                            }
                        }
                    }

                    // 判断被动呼叫时，已经被一台设备呼叫，又接到其他设备的呼叫请求，则调用AppControldeviceData拒绝其他设备的请求
                    if (TextUtils.isEmpty(callingDeviceId) && isCalling) {
                        if (videoCallStatus == TRTCCallStatus.TYPE_CALLING.getValue()) {
                            callback.updateCallStatus(Common.TRTC_VIDEO_CALL_STATUS, "0", payloadDeviceId);
                        } else if (audioCallStatus == TRTCCallStatus.TYPE_CALLING.getValue()) {
                            callback.updateCallStatus(Common.TRTC_AUDIO_CALL_STATUS, "0", payloadDeviceId);
                        }
                    }

                    // 判断payload中是否包含设备的video_call_status, audio_call_status字段以及是否等于1，若等于1，就调用CallDevice接口, 主动拨打
                    if (videoCallStatus == Common.TRTC_STATUS_CALL) {
                        if (TextUtils.isEmpty(callingDeviceId) && !TextUtils.isEmpty(deviceId) && !deviceId.contains(userId)) { //App被动呼叫 _sys_userid有值 且不包含当前用户的userid
                        } else {
                            callback.startCall(TRTCCalling.TYPE_VIDEO_CALL, payloadDeviceId);
                        }
                    } else if (audioCallStatus == Common.TRTC_STATUS_CALL) {
                        if (TextUtils.isEmpty(callingDeviceId) && !TextUtils.isEmpty(deviceId) && !deviceId.contains(userId)) { //App被动呼叫 _sys_userid有值 且不包含当前用户的userid
                        } else {
                            callback.startCall(TRTCCalling.TYPE_AUDIO_CALL, payloadDeviceId);
                        }
                    } else if (videoCallStatus == Common.TRTC_STATUS_FREE_OR_REJECT
                            || audioCallStatus == Common.TRTC_STATUS_FREE_OR_REJECT) { //空闲或拒绝了，当前正显示音视频通话页面的话，finish掉
                        if (this.deviceId.equals(payloadDeviceId)) {
                            callback.hungUp();
                        }
                    } else if (videoCallStatus == Common.TRTC_STATUS_CALLING
                            || audioCallStatus == Common.TRTC_STATUS_CALLING) {
                        if (callStatus == TRTCCallStatus.TYPE_CALLING.getValue() && TextUtils.isEmpty(callingDeviceId)) {
                            callback.otherUserAnswered();
                        }
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
