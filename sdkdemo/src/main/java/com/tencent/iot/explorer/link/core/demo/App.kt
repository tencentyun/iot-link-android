package com.tencent.iot.explorer.link.core.demo

import android.app.Application
import android.text.TextUtils
import androidx.multidex.MultiDex
import com.alibaba.fastjson.JSON
import com.tencent.iot.explorer.link.core.auth.IoTAuth
import com.tencent.iot.explorer.link.core.auth.callback.MyCallback
import com.tencent.iot.explorer.link.core.auth.entity.User
import com.tencent.iot.explorer.link.core.auth.listener.LoginExpiredListener
import com.tencent.iot.explorer.link.core.auth.message.MessageConst
import com.tencent.iot.explorer.link.core.auth.message.payload.Payload
import com.tencent.iot.explorer.link.core.auth.response.BaseResponse
import com.tencent.iot.explorer.link.core.auth.socket.callback.PayloadMessageCallback
import com.tencent.iot.explorer.link.core.auth.util.Weak
import com.tencent.iot.explorer.link.core.demo.activity.BaseActivity
import com.tencent.iot.explorer.link.core.demo.log.L
import com.tencent.iot.explorer.link.core.link.entity.TRTCParamsEntity
import com.tencent.iot.explorer.trtc.model.RoomKey
import com.tencent.iot.explorer.trtc.model.TRTCCalling
import com.tencent.iot.explorer.trtc.model.TRTCUIManager
import com.tencent.iot.explorer.trtc.ui.audiocall.TRTCAudioCallActivity
import com.tencent.iot.explorer.trtc.ui.videocall.TRTCVideoCallActivity

class App : Application(), PayloadMessageCallback {

    companion object {
        val data = AppData.instance
        var activity by Weak<BaseActivity>()
        fun appStartBeingCall(callingType: Int, deviceId: String) {
            TRTCUIManager.getInstance().setSessionManager(TRTCSdkDemoSessionManager())
            if (callingType == TRTCCalling.TYPE_VIDEO_CALL) {
                TRTCUIManager.getInstance().isCalling = true
                TRTCVideoCallActivity.startBeingCall(activity, RoomKey(), deviceId)
            } else if (callingType == TRTCCalling.TYPE_AUDIO_CALL) {
                TRTCUIManager.getInstance().isCalling = true
                TRTCAudioCallActivity.startBeingCall(activity, RoomKey(), deviceId)
            }
        }
    }

    private val APP_KEY = BuildConfig.TencentIotLinkSDKDemoAppkey
    private val APP_SECRET = BuildConfig.TencentIotLinkSDKDemoAppSecret

    override fun onCreate() {
        super.onCreate()
        L.isLog = true
        IoTAuth.openLog(true)
        /*
         * 此处仅供参考, 需自建服务接入物联网平台服务，以免 App Secret 泄露
         * 自建服务可参考此处 https://cloud.tencent.com/document/product/1081/45901#.E6.90.AD.E5.BB.BA.E5.90.8E.E5.8F.B0.E6.9C.8D.E5.8A.A1.2C-.E5.B0.86-app-api-.E8.B0.83.E7.94.A8.E7.94.B1.E8.AE.BE.E5.A4.87.E7.AB.AF.E5.8F.91.E8.B5.B7.E5.88.87.E6.8D.A2.E4.B8.BA.E7.94.B1.E8.87.AA.E5.BB.BA.E5.90.8E.E5.8F.B0.E6.9C.8D.E5.8A.A1.E5.8F.91.E8.B5.B7
         */
        IoTAuth.init(APP_KEY, APP_SECRET)
        IoTAuth.addLoginExpiredListener(object : LoginExpiredListener {
            override fun expired(user: User) {
                L.d("用户登录过期")
            }
        })
        IoTAuth.registerSharedBugly(this) //接入共享式bugly
        MultiDex.install(this)
        IoTAuth.addEnterRoomCallback(this)
    }

    override fun onTerminate() {
        IoTAuth.destroy()
        super.onTerminate()
    }

    fun startBeingCall(callingType: Int, deviceId: String) {
        if (App.data.callingDeviceId != "") {
            trtcCallDevice(callingType)
        } else {
            appStartBeingCall(callingType, deviceId)
        }
    }

    /**
     * 呼叫设备获取trtc参数信息
     */
    private fun trtcCallDevice(callingType: Int) {
        IoTAuth.deviceImpl.trtcCallDevice(App.data.callingDeviceId, object: MyCallback {
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

                var room = RoomKey()
                room.userId = params.UserId
                room.appId = params.SdkAppId
                room.userSig = params.UserSig
                room.roomId = params.StrRoomId
                room.callType = callingType
                enterRoom(room)
            }
        })
    }

    /**
     * 呼叫设备进入trtc房间通话
     */
    fun enterRoom(room: RoomKey) {
        activity?.runOnUiThread {
            if (room.callType == TRTCCalling.TYPE_VIDEO_CALL) {
                TRTCUIManager.getInstance().joinRoom(TRTCCalling.TYPE_VIDEO_CALL, App.data.callingDeviceId, room)
                App.data.callingDeviceId = ""
            } else if (room.callType == TRTCCalling.TYPE_AUDIO_CALL) {
                TRTCUIManager.getInstance().joinRoom(TRTCCalling.TYPE_AUDIO_CALL, App.data.callingDeviceId, room)
                App.data.callingDeviceId = ""
            }
        }
    }

    override fun payloadMessage(payload: Payload) {
        var jsonObject = org.json.JSONObject(payload.json)
        val action = jsonObject.getString(MessageConst.MODULE_ACTION);
        if (action == MessageConst.DEVICE_CHANGE) { //收到了设备属性改变的wss消息
            var paramsObject = jsonObject.getJSONObject(MessageConst.PARAM) as org.json.JSONObject
            val subType = paramsObject.getString(MessageConst.SUB_TYPE)
            if (subType == MessageConst.REPORT) { //收到了设备端属性状态改变的wss消息

                var payloadParamsObject = org.json.JSONObject(payload.payload)
                val payloadParamsJson = payloadParamsObject.getJSONObject(MessageConst.PARAM)
                var videoCallStatus = -1
                if (payloadParamsJson.has(MessageConst.TRTC_VIDEO_CALL_STATUS)) {
                    videoCallStatus = payloadParamsJson.getInt(MessageConst.TRTC_VIDEO_CALL_STATUS)
                }
                var audioCallStatus = -1
                if (payloadParamsJson.has(MessageConst.TRTC_AUDIO_CALL_STATUS)) {
                    audioCallStatus = payloadParamsJson.getInt(MessageConst.TRTC_AUDIO_CALL_STATUS)
                }

                var deviceId = ""
                if (payloadParamsJson.has(MessageConst.USERID)) {
                    deviceId = payloadParamsJson.getString(MessageConst.USERID)
                }

                // 判断payload中是否包含设备的video_call_status, audio_call_status字段以及是否等于1，若等于1，就调用CallDevice接口, 主动拨打
                if (videoCallStatus == 1) {
                    startBeingCall(2, deviceId)
                } else if (audioCallStatus == 1) {
                    startBeingCall(1, deviceId)
                }
            }
        }
    }
}