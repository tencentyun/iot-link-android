package com.tencent.iot.explorer.link

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import androidx.multidex.MultiDex
import com.alibaba.fastjson.JSON
import com.tencent.android.tpush.XGPushConfig
import com.tencent.iot.explorer.link.core.auth.IoTAuth
import com.tencent.iot.explorer.link.core.auth.callback.MyCallback
import com.tencent.iot.explorer.link.core.auth.message.MessageConst
import com.tencent.iot.explorer.link.core.auth.response.BaseResponse
import com.tencent.iot.explorer.link.core.auth.socket.callback.StartBeingCallCallback
import com.tencent.iot.explorer.link.core.auth.util.Weak
import com.tencent.iot.explorer.link.core.link.entity.TRTCParamsEntity
import com.tencent.iot.explorer.link.core.log.L
import com.tencent.iot.explorer.link.core.utils.SharePreferenceUtil
import com.tencent.iot.explorer.link.core.utils.Utils
import com.tencent.iot.explorer.link.kitlink.activity.BaseActivity
import com.tencent.iot.explorer.link.kitlink.activity.GuideActivity
import com.tencent.iot.explorer.link.kitlink.consts.CommonField
import com.tencent.iot.explorer.link.kitlink.util.HttpRequest
import com.tencent.iot.explorer.trtc.model.RoomKey
import com.tencent.iot.explorer.trtc.model.TRTCCalling
import com.tencent.iot.explorer.trtc.model.TRTCUIManager
import com.tencent.iot.explorer.trtc.ui.audiocall.TRTCAudioCallActivity
import com.tencent.iot.explorer.trtc.ui.videocall.TRTCVideoCallActivity
import java.util.*


/**
 * APP
 */
class App : Application(), Application.ActivityLifecycleCallbacks, StartBeingCallCallback {

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

        fun setEnableEnterRoomCallback(enable: Boolean) {
            IoTAuth.setEnableEnterRoomCallback(enable)
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
        //信鸽推送日志开关
        XGPushConfig.enableDebug(applicationContext, DEBUG_VERSION)
        XGPushConfig.enablePullUpOtherApp(applicationContext, PULL_OTHER)
        language = SharePreferenceUtil.getString(this, CONFIG, "language")
        data.readLocalUser(this)
        data.appLifeCircleId = UUID.randomUUID().toString()
        registerActivityLifecycleCallbacks(this)
        IoTAuth.addEnterRoomCallback(this)
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
            if (activity is AppLifeCircleListener) {
                activity.onAppGoforeground()
            }
        }
    }

    override fun onActivityStopped(activity: Activity) {
        isActivityChangingConfigurations = activity.isChangingConfigurations
        if (--activityReferences == 0 && !isActivityChangingConfigurations) {
            // App enters background
            if (activity is AppLifeCircleListener) {
                activity.onAppGoBackground()
            }
        }
    }

    override fun onActivityPaused(activity: Activity) {}
    override fun onActivityDestroyed(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivityResumed(activity: Activity) {}

    /**
     * 呼叫设备获取trtc参数信息
     */
    override fun startBeingCall(callingType: Int, deviceId: String) {
        TRTCUIManager.getInstance().setSessionManager(TRTCAppSessionManger())

        if (callingType == TRTCCalling.TYPE_VIDEO_CALL) {
            TRTCVideoCallActivity.startBeingCall(activity, RoomKey(), deviceId)
        } else if (callingType == TRTCCalling.TYPE_AUDIO_CALL) {
            TRTCAudioCallActivity.startBeingCall(activity, RoomKey(), deviceId)
        }
    }
}

interface AppLifeCircleListener {
    fun onAppGoforeground()
    fun onAppGoBackground()
}