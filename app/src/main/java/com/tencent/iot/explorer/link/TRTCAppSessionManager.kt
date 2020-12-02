package com.tencent.iot.explorer.link

import android.text.TextUtils
import com.alibaba.fastjson.JSON
import com.tencent.iot.explorer.link.core.auth.callback.MyCallback
import com.tencent.iot.explorer.link.core.auth.message.MessageConst
import com.tencent.iot.explorer.link.core.auth.response.BaseResponse
import com.tencent.iot.explorer.link.core.link.entity.TRTCParamsEntity
import com.tencent.iot.explorer.link.core.log.L
import com.tencent.iot.explorer.link.kitlink.util.HttpRequest
import com.tencent.iot.explorer.trtc.model.RoomKey
import com.tencent.iot.explorer.trtc.model.TRTCCalling
import com.tencent.iot.explorer.trtc.model.TRTCSessionManager
import com.tencent.iot.explorer.trtc.model.TRTCUIManager

class TRTCAppSessionManager : TRTCSessionManager() {

    override fun joinRoom(callingType: Int, deviceId: String) {
        super.joinRoom(callingType, deviceId)

        startBeingCall(callingType, deviceId)
    }

    /**
     * 呼叫设备获取trtc参数信息
     */
    fun startBeingCall(callingType: Int, deviceId: String) {
        HttpRequest.instance.trtcCallDevice(deviceId, object: MyCallback {
            override fun fail(msg: String?, reqCode: Int) {
                if (msg != null) L.e(msg)
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
}