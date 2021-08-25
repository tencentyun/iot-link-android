package com.tencent.iot.explorer.link.demo

import android.app.Application
import android.text.TextUtils
import android.widget.Toast
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
import com.tencent.iot.explorer.link.demo.common.log.L
import com.tencent.iot.explorer.link.core.link.entity.TRTCParamsEntity
import com.tencent.iot.explorer.link.demo.rtc.TRTCSdkDemoSessionManager
import com.tencent.iot.explorer.link.rtc.model.*
import com.tencent.iot.explorer.link.rtc.ui.audiocall.TRTCAudioCallActivity
import com.tencent.iot.explorer.link.rtc.ui.videocall.TRTCVideoCallActivity

class App : Application(), PayloadMessageCallback {

    companion object {
        val data = AppData.instance
        var activity by Weak<BaseActivity>()
        fun appStartBeingCall(callingType: Int, deviceId: String) {
            if (!TRTCUIManager.getInstance().isCalling) { //没有正在通话时，唤起通话页面
                TRTCUIManager.getInstance().setSessionManager(TRTCSdkDemoSessionManager())

                TRTCUIManager.getInstance().deviceId = deviceId
                if (callingType == TRTCCalling.TYPE_VIDEO_CALL) {
                    TRTCUIManager.getInstance().isCalling = true
                    TRTCVideoCallActivity.startBeingCall(activity, RoomKey(), deviceId)
                } else if (callingType == TRTCCalling.TYPE_AUDIO_CALL) {
                    TRTCUIManager.getInstance().isCalling = true
                    TRTCAudioCallActivity.startBeingCall(activity, RoomKey(), deviceId)
                }
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
        if (data.callingDeviceId != "") { //App主动呼叫
            trtcCallDevice(callingType)
        } else { //App被动呼叫
            appStartBeingCall(callingType, deviceId)
        }
    }

    /**
     * 呼叫设备获取trtc参数信息
     */
    private fun trtcCallDevice(callingType: Int) {
        IoTAuth.deviceImpl.trtcCallDevice(data.callingDeviceId, object: MyCallback {
            override fun fail(msg: String?, reqCode: Int) {
                if (msg != null) L.e(msg)
            }

            override fun success(response: BaseResponse, reqCode: Int) {
                // 解析房间参数，并呼叫页面
                val json = response.data as com.alibaba.fastjson.JSONObject
                if (json == null || !json.containsKey(MessageConst.TRTC_PARAMS)) {
                    activity?.runOnUiThread {
                        Toast.makeText(activity, "对方正忙...", Toast.LENGTH_LONG).show()
                    }
                    TRTCUIManager.getInstance().exitRoom()
                } else {
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
            }
        })
    }

    /**
     * 呼叫设备进入trtc房间通话
     */
    fun enterRoom(room: RoomKey) {
        activity?.runOnUiThread {
            if (room.callType == TRTCCalling.TYPE_VIDEO_CALL) {
                TRTCUIManager.getInstance().joinRoom(TRTCCalling.TYPE_VIDEO_CALL, data.callingDeviceId, room)
                data.callingDeviceId = ""
            } else if (room.callType == TRTCCalling.TYPE_AUDIO_CALL) {
                TRTCUIManager.getInstance().joinRoom(TRTCCalling.TYPE_AUDIO_CALL, data.callingDeviceId, room)
                data.callingDeviceId = ""
            }
        }
    }

    override fun payloadMessage(payload: Payload) {
        val userId = data.userInfo.UserID
        val trtcPayload = TRTCPayload(payload.json, payload.payload, payload.deviceId)
        TRTCUIManager.getInstance().payloadMessage(trtcPayload, userId, object:
            TRTCCallback {
            override fun busy() {
                TRTCUIManager.getInstance().userBusy()
                TRTCUIManager.getInstance().exitRoom()
                activity?.runOnUiThread {
                    Toast.makeText(activity, "对方正忙...", Toast.LENGTH_LONG).show()
                }
            }

            override fun updateCallStatus(key: String?, value: String?, deviceId: String?) {
                controlDevice(key!!, value!!, deviceId!!)
            }

            override fun startCall(type: Int, deviceId: String?) {
                startBeingCall(type, deviceId!!)
            }

            override fun otherUserAnswered() {
                activity?.runOnUiThread {
                    Toast.makeText(activity, "其他用户已接听...", Toast.LENGTH_LONG).show()
                }
                TRTCUIManager.getInstance().otherUserAccept()
                TRTCUIManager.getInstance().exitRoom()
            }

            override fun hungUp() {
                if (TRTCUIManager.getInstance().callStatus == TRTCCallStatus.TYPE_CALLING.value) {
                    if (TRTCUIManager.getInstance().callingDeviceId == "") { //被动呼叫
                        activity?.runOnUiThread {
                            Toast.makeText(activity, "对方正忙...", Toast.LENGTH_LONG).show()
                        }
                    } else { //主动呼叫
                        activity?.runOnUiThread {
                            Toast.makeText(activity, "对方正忙...", Toast.LENGTH_LONG).show()
                        }
                    }
                }
                TRTCUIManager.getInstance().exitRoom()
            }
        })
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

        com.tencent.iot.explorer.link.core.log.L.d("上报数据:id=$id value=$value")
        var userId = data.userInfo.UserID
        var data = "{\"$id\":$value, \"${MessageConst.USERID}\":\"$userId\"}"
        IoTAuth.deviceImpl.controlDevice(productId, deviceName, data, object: MyCallback {
            override fun fail(msg: String?, reqCode: Int) {
                if (msg != null) com.tencent.iot.explorer.link.core.log.L.e(msg)
            }

            override fun success(response: BaseResponse, reqCode: Int) {

            }

        })
    }
}