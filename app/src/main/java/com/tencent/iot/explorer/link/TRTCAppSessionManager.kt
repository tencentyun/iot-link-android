package com.tencent.iot.explorer.link

import android.text.TextUtils
import android.widget.Toast
import com.alibaba.fastjson.JSON
import com.tencent.iot.explorer.link.core.auth.callback.MyCallback
import com.tencent.iot.explorer.link.core.auth.message.MessageConst
import com.tencent.iot.explorer.link.core.auth.response.BaseResponse
import com.tencent.iot.explorer.link.core.link.entity.TRTCParamsEntity
import com.tencent.iot.explorer.link.core.log.L
import com.tencent.iot.explorer.link.core.utils.SharePreferenceUtil
import com.tencent.iot.explorer.link.kitlink.consts.CommonField
import com.tencent.iot.explorer.link.kitlink.util.HttpRequest
import com.tencent.iot.explorer.link.rtc.model.RoomKey
import com.tencent.iot.explorer.link.rtc.model.TRTCCalling
import com.tencent.iot.explorer.link.rtc.model.TRTCSessionManager
import com.tencent.iot.explorer.link.rtc.model.TRTCUIManager

class TRTCAppSessionManager : TRTCSessionManager() {

    override fun joinRoom(callingType: Int, deviceId: String) {
        super.joinRoom(callingType, deviceId)

        startBeingCall(callingType, deviceId)
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
        App.data.callingDeviceId = ""
    }

    /**
     * 呼叫设备获取trtc参数信息
     */
    fun startBeingCall(callingType: Int, deviceId: String) {
        HttpRequest.instance.trtcCallDevice(deviceId, object: MyCallback {
            override fun fail(msg: String?, reqCode: Int) {
                if (msg != null) L.e(msg)
                App.activity?.runOnUiThread {
                    Toast.makeText(App.activity, "对方正忙...", Toast.LENGTH_LONG).show()
                }
                TRTCUIManager.getInstance().exitRoom()
            }

            override fun success(response: BaseResponse, reqCode: Int) {
                // 解析房间参数，并呼叫页面
                val json = response.data as com.alibaba.fastjson.JSONObject
                if (json == null || !json.containsKey(MessageConst.TRTC_PARAMS)) return;
                val data = json.getString(MessageConst.TRTC_PARAMS)
                if (TextUtils.isEmpty(data)) return;
                val params = JSON.parseObject(data, TRTCParamsEntity::class.java)

                enterRoom(callingType, params, deviceId)
            }
        })
    }

    /**
     * 被设备呼叫进入trtc房间通话
     */
    private fun enterRoom(callingType: Int, params: TRTCParamsEntity, deviceId: String) {
        var room = RoomKey()
        room.userId = params.UserId
        room.appId = params.SdkAppId
        room.userSig = params.UserSig
        room.roomId = params.StrRoomId
        room.callType = callingType
        App.activity?.runOnUiThread {
            if (room.callType == TRTCCalling.TYPE_VIDEO_CALL) {
                TRTCUIManager.getInstance().joinRoom(callingType, deviceId, room)
//                TRTCVideoCallActivity.startBeingCall(App.activity, room, deviceId)
            } else if (room.callType == TRTCCalling.TYPE_AUDIO_CALL) {
                TRTCUIManager.getInstance().joinRoom(callingType, deviceId, room)
//                TRTCAudioCallActivity.startBeingCall(App.activity, room, deviceId)
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
        var data = "{\"$id\":$value, \"${MessageConst.USERID}\":\"$userId\"}"
        HttpRequest.instance.controlDevice(productId, deviceName, data, object: MyCallback {
            override fun fail(msg: String?, reqCode: Int) {
                if (msg != null) L.e(msg)
            }

            override fun success(response: BaseResponse, reqCode: Int) {

            }

        })
    }
}