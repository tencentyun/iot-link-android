package com.tencent.iot.explorer.link

import android.app.Application
import android.content.Intent
import android.text.TextUtils
import androidx.multidex.MultiDex
import com.tencent.iot.explorer.link.auth.IoTAuth
import com.tencent.iot.explorer.link.kitlink.activity.LoginActivity
import com.tencent.iot.explorer.link.kitlink.util.AppData
import com.tencent.iot.explorer.link.kitlink.util.Weak
import com.tencent.android.tpush.XGPushConfig
import com.tencent.iot.explorer.link.util.T
import com.tencent.iot.explorer.link.kitlink.activity.BaseActivity
import com.tencent.iot.explorer.link.util.SharePreferenceUtil
import com.tencent.iot.explorer.link.kitlink.consts.CommonField
import com.tencent.iot.explorer.link.core.log.L
import com.tencent.iot.explorer.link.kitlink.activity.GuideActivity

/**
 * APP
 */
class App : Application() {

    companion object {
        //app数据
        val data = AppData.instance

        const val CONFIG = "config"
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

        fun chargeUrlAppType(): Boolean {
            if (BuildConfig.TencentIotLinkAppkey.equals(CommonField.NULL_STR)
                || TextUtils.isEmpty(BuildConfig.TencentIotLinkAppkey)) {
                return false

            } else if (BuildConfig.TencentIotLinkAppkey.equals(CommonField.IOT_APP_KEY)) {
                return false
            }

            return true
        }
    }

    override fun onCreate() {
        super.onCreate()
        MultiDex.install(this)
        IoTAuth.init()
        //初始化弹框
        T.setContext(this.applicationContext)
        //日志开关
        L.isLog = DEBUG_VERSION
        //信鸽推送日志开关
        XGPushConfig.enableDebug(applicationContext, DEBUG_VERSION)
        XGPushConfig.enablePullUpOtherApp(applicationContext, PULL_OTHER)
        language = SharePreferenceUtil.getString(this, CONFIG, "language")
        data.readLocalUser(this)
    }

    /**
     * 应用销毁
     */
    override fun onTerminate() {
        super.onTerminate()
        //关闭WebSocket
        IoTAuth.destroy()
    }

}