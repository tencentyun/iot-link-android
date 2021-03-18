package com.tencent.iot.explorer.link

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.multidex.MultiDex
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.tencent.android.tpush.XGPushConfig
import com.tencent.iot.explorer.link.core.auth.IoTAuth
import com.tencent.iot.explorer.link.core.auth.callback.MyCallback
import com.tencent.iot.explorer.link.core.auth.entity.DeviceEntity
import com.tencent.iot.explorer.link.core.auth.entity.ProductEntity
import com.tencent.iot.explorer.link.core.auth.message.MessageConst
import com.tencent.iot.explorer.link.core.auth.message.payload.Payload
import com.tencent.iot.explorer.link.core.auth.message.upload.ArrayString
import com.tencent.iot.explorer.link.core.auth.response.*
import com.tencent.iot.explorer.link.core.auth.socket.callback.PayloadMessageCallback
import com.tencent.iot.explorer.link.core.auth.util.JsonManager
import com.tencent.iot.explorer.link.core.auth.util.Weak
import com.tencent.iot.explorer.link.core.link.entity.TRTCParamsEntity
import com.tencent.iot.explorer.link.core.log.L
import com.tencent.iot.explorer.link.core.utils.SharePreferenceUtil
import com.tencent.iot.explorer.link.core.utils.Utils
import com.tencent.iot.explorer.link.kitlink.activity.BaseActivity
import com.tencent.iot.explorer.link.kitlink.activity.GuideActivity
import com.tencent.iot.explorer.link.kitlink.consts.CommonField
import com.tencent.iot.explorer.link.kitlink.util.HttpRequest
import com.tencent.iot.explorer.link.kitlink.util.WeatherUtils
import com.tencent.iot.explorer.link.rtc.model.RoomKey
import com.tencent.iot.explorer.link.rtc.model.TRTCCalling
import com.tencent.iot.explorer.link.rtc.model.TRTCUIManager
import com.tencent.iot.explorer.link.rtc.ui.audiocall.TRTCAudioCallActivity
import com.tencent.iot.explorer.link.rtc.ui.videocall.TRTCVideoCallActivity
import com.tencent.iot.explorer.link.rtc.model.TRTCCallStatus
import java.util.*
import kotlin.collections.ArrayList


/**
 * APP
 */
class App : Application(), Application.ActivityLifecycleCallbacks, PayloadMessageCallback {

    companion object {
        //app数据
        val data = AppData.instance

        const val CONFIG = "config"
        const val MUST_UPGRADE_TAG = "master"
        var language: String? = ""

        // 根据编译使用的 buildType 类型确定是否是 debug 版本
        // 编译依赖的 buildType 包含 debug 字串即认为是 debug 版本
        @JvmField
        val DEBUG_VERSION: Boolean = BuildConfig.BUILD_TYPE.contains(CommonField.DEBUG_FLAG)
        const val PULL_OTHER: Boolean = false
        var activity by Weak<BaseActivity>()

        /**
         * 去登录
         */
        @Synchronized
        fun toLogin() {
            activity?.run {
                data.clear()
                startActivity(Intent(activity, GuideActivity::class.java))
            }
        }

        fun isOEMApp(): Boolean {
            if (BuildConfig.TencentIotLinkAppkey.equals(CommonField.NULL_STR)
                || TextUtils.isEmpty(BuildConfig.TencentIotLinkAppkey)) {
                return false
            } else if (BuildConfig.TencentIotLinkAppkey.equals(CommonField.IOT_APP_KEY)) {
                return false
            }
            return true
        }

        fun needUpgrade(newVersion: String): Boolean {
            if (TextUtils.isEmpty(newVersion)) return false
            var currentVersion = BuildConfig.VERSION_NAME

            // 如果是主干版本，强制升级
            if (currentVersion.startsWith(MUST_UPGRADE_TAG)) return true

            var newVerArr = newVersion.split(".")
            var curVerArr = currentVersion.split(".")
            for (i in 0..2) {
                // 按照顺序新版本只要有一位小于当前版本，直接认为无需升级
                if (i < newVerArr.size && i < curVerArr.size ) {
                    if (Utils.getFirstSeriesNumFromStr(newVerArr.get(i)) <
                            Utils.getFirstSeriesNumFromStr(curVerArr.get(i))) {
                        return false
                    } else if (Utils.getFirstSeriesNumFromStr(newVerArr.get(i)) >
                        Utils.getFirstSeriesNumFromStr(curVerArr.get(i))) {
                        return true
                    }
                }
            }
            return false
        }

        fun setEnablePayloadMessageCallback(enable: Boolean) {
            IoTAuth.setEnablePayloadMessageCallback(enable)
        }

        fun appStartBeingCall(callingType: Int, deviceId: String) {
            if (data.isForeground && !TRTCUIManager.getInstance().isCalling) { //在前台，没有正在通话时，唤起通话页面
                TRTCUIManager.getInstance().setSessionManager(TRTCAppSessionManager())

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

    override fun onCreate() {
        super.onCreate()
        MultiDex.install(this)
        IoTAuth.setWebSocketTag(Utils.getAndroidID(this)) // 设置wss的uin
        IoTAuth.init(BuildConfig.TencentIotLinkAppkey, BuildConfig.TencentIotLinkAppSecret)
        //初始化弹框
        T.setContext(this.applicationContext)
        //日志开关
        L.isLog = DEBUG_VERSION
        //日志等级
        L.LOG_LEVEL = L.LEVEL_INFO
        //信鸽推送日志开关
        XGPushConfig.enableDebug(applicationContext, DEBUG_VERSION)
        XGPushConfig.enablePullUpOtherApp(applicationContext, PULL_OTHER)
        language = SharePreferenceUtil.getString(this, CONFIG, "language")
        data.readLocalUser(this)
        data.appLifeCircleId = UUID.randomUUID().toString()
        registerActivityLifecycleCallbacks(this)
        IoTAuth.addEnterRoomCallback(this)

        var lang = Utils.getLang()
        lang = lang.substring(0,2)
        WeatherUtils.defaultLang = lang
    }

    /**
     * 应用销毁
     */
    override fun onTerminate() {
        super.onTerminate()
        //关闭WebSocket
        IoTAuth.destroy()
        T.setContext(null)
    }

    private var activityReferences = 0
    private var isActivityChangingConfigurations = false

    override fun onActivityStarted(activity: Activity) {
        Utils.clearMsgNotify(activity, data.notificationId)
        if (++activityReferences == 1 && !isActivityChangingConfigurations) {
            // App enters foreground
            data.isForeground = true
            requestDeviceList()
            if (activity is AppLifeCircleListener) {
                activity.onAppGoforeground()
            }
        }
    }

    override fun onActivityStopped(activity: Activity) {
        isActivityChangingConfigurations = activity.isChangingConfigurations
        if (--activityReferences == 0 && !isActivityChangingConfigurations) {
            // App enters background
            data.isForeground = false
            if (activity is AppLifeCircleListener) {
                activity.onAppGoBackground()
            }
        }
    }

    fun requestDeviceList() {
        HttpRequest.instance.deviceList(
            App.data.getCurrentFamily().FamilyId,
            App.data.getCurrentRoom().RoomId,
            0,
            object: MyCallback {
                override fun fail(msg: String?, reqCode: Int) {
                    if (msg != null) L.e(msg)
                }

                override fun success(response: BaseResponse, reqCode: Int) {
                    if (response.isSuccess()) {
                        response.parse(DeviceListResponse::class.java)?.run {
                            val deviceList = DeviceList
                            val productIdList = ArrayList<String>()
                            // TRTC: 轮询在线的trtc设备的call_status
                            for (device in DeviceList) {
                                val deviceIds = ArrayList<String>()
                                deviceIds.add(device.DeviceId)
                                productIdList.add(device.ProductId)
                                getDeviceOnlineStatus(device.ProductId, deviceIds, device)
                            }
                            // TRTC：轮询在线的trtc共享设备的call_status
                            for (device in data.shareDeviceList) {
                                val deviceIds = ArrayList<String>()
                                if (!device.DeviceId.isNullOrEmpty() && !device.ProductId.isNullOrEmpty()) {
                                    deviceIds.add(device.DeviceId)
                                    productIdList.add(device.ProductId)
                                    getDeviceOnlineStatus(device.ProductId, deviceIds, device)
                                }
                            }
                        }
                    }
                }
            })
    }

    private fun getDeviceOnlineStatus(productId: String, deviceIds: ArrayList<String>, device: DeviceEntity) {
        HttpRequest.instance.deviceOnlineStatus(productId, deviceIds, object : MyCallback {
            override fun fail(msg: String?, reqCode: Int) {
                L.e(msg ?: "")
            }

            override fun success(response: BaseResponse, reqCode: Int) {
                if (response.isSuccess()) {
                    response.parse(DeviceOnlineResponse::class.java)?.run {
                        if (!DeviceStatuses.isNullOrEmpty()) {
                            DeviceStatuses!!.forEach {
                                if (it.Online == 1) {//设备在线
                                    getDeviceProducts(productId, device)
                                }
                            }
                        }
                    }
                }
            }
        })
    }

    private fun getDeviceProducts(productId: String, device: DeviceEntity) {
        HttpRequest.instance.deviceProducts(arrayListOf(productId), object: MyCallback {
            override fun fail(msg: String?, reqCode: Int) {
                if (msg != null) L.e(msg)
            }

            override fun success(response: BaseResponse, reqCode: Int) {

                if (!response.isSuccess()) return
                response.parse(DeviceProductResponse::class.java)?.run {
                    if (Products.isNotEmpty()) {
                        val product = Products[0]

                        if (product.Services.isNotEmpty()) {
                            product.Services.forEach {
                                if (it == "TRTC") { //是TRTC类产品
                                    val trtcDeviceIdList = ArrayString()
                                    trtcDeviceIdList.addValue(device.DeviceId)
                                    getDeviceCallStatus(device)
                                    // TRTC: trtc设备注册websocket监听
                                    IoTAuth.registerActivePush(trtcDeviceIdList, null)
                                }
                            }
                        }
                    }
                }
            }
        })
    }

    /**
     * 获取 设备当前状态(如亮度、开关状态等)
     */
    private fun getDeviceCallStatus(device: DeviceEntity) {
        HttpRequest.instance.deviceData(
            device.ProductId,
            device.DeviceName,
            object : MyCallback {
                override fun fail(msg: String?, reqCode: Int) {
                    if (msg != null) L.e(msg)
                }

                override fun success(response: BaseResponse, reqCode: Int) {
                    if (response.code == 0) { //获取 设备当前状态(如亮度、开关状态等) 成功
                        // 解析设备状态
                        val json = response.data as JSONObject
                        val dataJson = json.getJSONObject(CommonField.DATA)
                        if (dataJson == null || dataJson.isEmpty()) {
                            return
                        }
                        val videoCallStatusJson = dataJson.getJSONObject(MessageConst.TRTC_VIDEO_CALL_STATUS)
                        var videoCallStatus = -1
                        if (videoCallStatusJson != null) {
                            videoCallStatus = videoCallStatusJson.getInteger("Value")
                        }

                        val audioCallStatusJson =
                            dataJson.getJSONObject(MessageConst.TRTC_AUDIO_CALL_STATUS)
                        var audioCallStatus = -1
                        if (audioCallStatusJson != null) {
                            audioCallStatus = audioCallStatusJson.getInteger("Value")
                        }
                        // 判断设备的video_call_status, audio_call_status字段是否等于1，若等于1，就调用CallDevice接口
                        if (videoCallStatus == 1) {
                            App.appStartBeingCall(TRTCCalling.TYPE_VIDEO_CALL, device.DeviceId)
                        } else if (audioCallStatus == 1) {
                            App.appStartBeingCall(TRTCCalling.TYPE_AUDIO_CALL, device.DeviceId)
                        }
                    }

                }
            })
    }

    override fun onActivityPaused(activity: Activity) {}
    override fun onActivityDestroyed(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivityResumed(activity: Activity) {}

    /**
     * 呼叫设备获取trtc参数信息
     */
    fun startBeingCall(callingType: Int, deviceId: String) {
        if (data.callingDeviceId != "") { //App主动呼叫
            trtcCallDevice(callingType)
        } else { //App被动
            appStartBeingCall(callingType, deviceId)
        }
    }

    /**
     * 呼叫设备获取trtc参数信息
     */
    private fun trtcCallDevice(callingType: Int) {
        HttpRequest.instance.trtcCallDevice(App.data.callingDeviceId, object: MyCallback {
            override fun fail(msg: String?, reqCode: Int) {
                if (msg != null) L.e(msg)
            }

            override fun success(response: BaseResponse, reqCode: Int) {
                // 解析房间参数，并呼叫页面
                val json = response.data as com.alibaba.fastjson.JSONObject
                if (json == null || !json.containsKey(MessageConst.TRTC_PARAMS)) {
                    activity?.runOnUiThread {
                        Toast.makeText(App.activity, "对方正忙...", Toast.LENGTH_LONG).show()
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
                TRTCUIManager.getInstance().joinRoom(TRTCCalling.TYPE_VIDEO_CALL, App.data.callingDeviceId, room)
            } else if (room.callType == TRTCCalling.TYPE_AUDIO_CALL) {
                TRTCUIManager.getInstance().joinRoom(TRTCCalling.TYPE_AUDIO_CALL, App.data.callingDeviceId, room)
            }
        }
    }

    override fun payloadMessage(payload: Payload) {

        var jsonObject = org.json.JSONObject(payload.json)
        val action = jsonObject.getString(MessageConst.MODULE_ACTION);
        if (action == MessageConst.DEVICE_CHANGE) { //收到了设备属性改变的wss消息
            var paramsObject = jsonObject.getJSONObject(MessageConst.PARAM) as org.json.JSONObject
            val subType = paramsObject.getString(MessageConst.SUB_TYPE)
            if (subType == MessageConst.REPORT) { //收到了设备端上报的属性状态改变的wss消息

                var payloadParamsObject = org.json.JSONObject(payload.payload)
                val payloadParamsJson = payloadParamsObject.getJSONObject(MessageConst.PARAM)

                var method = ""
                if (payloadParamsObject.has(MessageConst.METHOD)) {
                    method = payloadParamsObject.getString(MessageConst.METHOD)
                }
                if (method != "report") {
                    return; //过滤掉非report消息
                }
                var videoCallStatus = -1
                if (payloadParamsJson.has(MessageConst.TRTC_VIDEO_CALL_STATUS)) {
                    videoCallStatus = payloadParamsJson.getInt(MessageConst.TRTC_VIDEO_CALL_STATUS)
                }
                var audioCallStatus = -1
                if (payloadParamsJson.has(MessageConst.TRTC_AUDIO_CALL_STATUS)) {
                    audioCallStatus = payloadParamsJson.getInt(MessageConst.TRTC_AUDIO_CALL_STATUS)
                }

                if (videoCallStatus == -1 && audioCallStatus == -1) {
                    return; //过滤掉非音视频通话的消息
                }

                var deviceId = ""
                if (payloadParamsJson.has(MessageConst.USERID)) {
                    deviceId = payloadParamsJson.getString(MessageConst.USERID)
                }

                // 判断主动呼叫的回调中收到的_sys_userid不为自己的userid则被其他用户抢先呼叫设备了，提示用户 对方正忙...
                val userId = SharePreferenceUtil.getString(activity, App.CONFIG, CommonField.USER_ID)
                if (data.callingDeviceId != "" && deviceId != userId) {
                    if (TRTCUIManager.getInstance().isCalling) { //当前正显示音视频通话页面，finish掉
                        if (data.callingDeviceId == payload.deviceId) {
                            TRTCUIManager.getInstance().userBusy()
                            TRTCUIManager.getInstance().exitRoom()
                            activity?.runOnUiThread {
                                Toast.makeText(activity, "对方正忙...", Toast.LENGTH_LONG).show()
                            }
                            return
                        } else { //其他的设备又呼叫了该用户
                            if (videoCallStatus == TRTCCallStatus.TYPE_CALLING.value) {
                                controlDevice(MessageConst.TRTC_VIDEO_CALL_STATUS, "0", payload.deviceId)
                                return
                            } else if (audioCallStatus == TRTCCallStatus.TYPE_CALLING.value) {
                                controlDevice(MessageConst.TRTC_AUDIO_CALL_STATUS, "0", payload.deviceId)
                                return
                            }
                        }
                    }
                }

                // 判断被动呼叫时，已经被一台设备呼叫，又接到其他设备的呼叫请求，则调用AppControldeviceData拒绝其他设备的请求
                if (data.callingDeviceId == "" && TRTCUIManager.getInstance().isCalling) {
                    if (videoCallStatus == TRTCCallStatus.TYPE_CALLING.value) {
                        controlDevice(MessageConst.TRTC_VIDEO_CALL_STATUS, "0", payload.deviceId)
                    } else if (audioCallStatus == TRTCCallStatus.TYPE_CALLING.value) {
                        controlDevice(MessageConst.TRTC_AUDIO_CALL_STATUS, "0", payload.deviceId)
                    }
                }

                // 判断payload中是否包含设备的video_call_status, audio_call_status字段以及是否等于1，若等于1，就调用CallDevice接口, 主动拨打
                if (videoCallStatus == 1) {
                    if (data.callingDeviceId == "" && deviceId != "" && !deviceId.contains(userId)) { //App被动呼叫 _sys_userid有值 且不包含当前用户的userid
                    } else {
                        startBeingCall(TRTCCalling.TYPE_VIDEO_CALL, payload.deviceId)
                    }
                } else if (audioCallStatus == 1) {
                    if (data.callingDeviceId == "" && deviceId != "" && !deviceId.contains(userId)) { //App被动呼叫 _sys_userid有值 且不包含当前用户的userid
                    } else {
                        startBeingCall(TRTCCalling.TYPE_AUDIO_CALL, payload.deviceId)
                    }
                } else if (videoCallStatus == 0 || audioCallStatus == 0) { //空闲或拒绝了，当前正显示音视频通话页面的话，finish掉
                    if (TRTCUIManager.getInstance().deviceId == payload.deviceId) {
                        if (TRTCUIManager.getInstance().callStatus == TRTCCallStatus.TYPE_CALLING.value) {
                            if (data.callingDeviceId == "") { //被动呼叫
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
                } else if (videoCallStatus == 2 || audioCallStatus == 2) {
                    if (TRTCUIManager.getInstance().callStatus == TRTCCallStatus.TYPE_CALLING.value && data.callingDeviceId == "") {
                        activity?.runOnUiThread {
                            Toast.makeText(activity, "其他用户已接听...", Toast.LENGTH_LONG).show()
                        }
                        TRTCUIManager.getInstance().otherUserAccept()
                        TRTCUIManager.getInstance().exitRoom()
                    }
                }
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
        var userId = SharePreferenceUtil.getString(activity, CONFIG, CommonField.USER_ID)
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

interface AppLifeCircleListener {
    fun onAppGoforeground()
    fun onAppGoBackground()
}