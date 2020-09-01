package com.tencent.iot.explorer.link.core.demo

import android.app.Application
import androidx.multidex.MultiDex
import com.tencent.iot.explorer.link.core.auth.IoTAuth
import com.tencent.iot.explorer.link.core.auth.entity.User
import com.tencent.iot.explorer.link.core.auth.listener.LoginExpiredListener
import com.tencent.iot.explorer.link.core.demo.log.L

class App : Application() {

    companion object {
        val data = AppData.instance
    }

    private val APP_KEY = BuildConfig.TencentIotLinkSDKDemoAppkey

    override fun onCreate() {
        super.onCreate()
        L.isLog = true
        //需要打印日志时要在IoTAuth.init(APP_KEY)之前调用
        // 否则看不到"The SDK initialized successfully"的日志
        IoTAuth.openLog(true)
        IoTAuth.init(APP_KEY)
        IoTAuth.addLoginExpiredListener(object : LoginExpiredListener {
            override fun expired(user: User) {
                L.d("用户登录过期")
            }
        })
        IoTAuth.registerSharedBugly(this) //接入共享式bugly
        MultiDex.install(this)
    }

    override fun onTerminate() {
        IoTAuth.destroy()
        super.onTerminate()
    }
}