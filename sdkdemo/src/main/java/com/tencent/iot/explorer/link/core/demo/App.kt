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
    }

    override fun onTerminate() {
        IoTAuth.destroy()
        super.onTerminate()
    }
}