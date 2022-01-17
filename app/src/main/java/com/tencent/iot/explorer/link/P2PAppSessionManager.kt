package com.tencent.iot.explorer.link

import com.tencent.iot.explorer.link.core.auth.callback.MyCallback
import com.tencent.iot.explorer.link.core.auth.message.MessageConst
import com.tencent.iot.explorer.link.core.auth.response.BaseResponse
import com.tencent.iot.explorer.link.core.log.L
import com.tencent.iot.explorer.link.core.utils.SharePreferenceUtil
import com.tencent.iot.explorer.link.kitlink.consts.CommonField
import com.tencent.iot.explorer.link.kitlink.util.HttpRequest
import com.tencent.iot.explorer.link.rtc.model.RoomKey
import com.tencent.iot.explorer.link.rtc.model.TRTCCalling
import com.tencent.iot.explorer.link.rtc.model.TRTCSessionManager
import com.tencent.iot.explorer.link.rtc.model.TRTCUIManager

class P2PAppSessionManager : TRTCSessionManager() {

    override fun joinRoom(callingType: Int, deviceId: String) {
        super.joinRoom(callingType, deviceId)

        if (TRTCUIManager.getInstance().callingDeviceId.equals("")) { //被叫
            if (callingType == TRTCCalling.TYPE_VIDEO_CALL) {
                controlDevice(MessageConst.TRTC_VIDEO_CALL_STATUS, "1", deviceId)
            } else if (callingType == TRTCCalling.TYPE_AUDIO_CALL) {
                controlDevice(MessageConst.TRTC_AUDIO_CALL_STATUS, "1", deviceId)
            }
        } else {
            startBeingCall(callingType, deviceId)
        }
    }

    override fun exitRoom(callingType: Int, deviceId: String) {
        super.exitRoom(callingType, deviceId)
        if (callingType == TRTCCalling.TYPE_VIDEO_CALL) {
            controlDevice(MessageConst.TRTC_VIDEO_CALL_STATUS, "0", deviceId)
        } else if (callingType == TRTCCalling.TYPE_AUDIO_CALL) {
            controlDevice(MessageConst.TRTC_AUDIO_CALL_STATUS, "0", deviceId)
        }
    }

    override fun resetTRTCStatus() {
        super.resetTRTCStatus()
        TRTCUIManager.getInstance().callingDeviceId = ""
    }

    /**
     * 呼叫设备获取trtc参数信息
     */
    fun startBeingCall(callingType: Int, deviceId: String) {
        App.activity?.runOnUiThread {
            if (callingType == TRTCCalling.TYPE_VIDEO_CALL) {
                TRTCUIManager.getInstance().joinRoom(callingType, deviceId, RoomKey())
            } else if (callingType == TRTCCalling.TYPE_AUDIO_CALL) {
                TRTCUIManager.getInstance().joinRoom(callingType, deviceId, RoomKey())
            }
        }
    }

    /**
     * 用户控制设备(上报数据)
     */
    fun controlDevice(id: String, value: String, deviceId: String) {

        val list = deviceId.split("/")

        var productId = ""
        var deviceName = ""
        if (list.size == 2) {
            productId = list[0]
            deviceName = list[1]
        } else { //deviceId格式有问题
            return
        }

        L.d("上报数据:id=$id value=$value")
        val userId = SharePreferenceUtil.getString(App.activity, App.CONFIG, CommonField.USER_ID)
        var callerId = ""
        var calledId = ""
        if (TRTCUIManager.getInstance().callingDeviceId.equals("")) { //被叫
            callerId = deviceId
            calledId = userId
        } else { //主叫
            callerId = userId
            calledId = deviceId
        }
        var data = "{\"$id\":$value, \"${MessageConst.TRTC_CALLEDID}\":\"$calledId\", \"${MessageConst.TRTC_CALLERID}\":\"$callerId\"}"
        HttpRequest.instance.controlDevice(productId, deviceName, data, object: MyCallback {
            override fun fail(msg: String?, reqCode: Int) {
                if (msg != null) L.e(msg)
            }

            override fun success(response: BaseResponse, reqCode: Int) {

            }

        })
    }
}