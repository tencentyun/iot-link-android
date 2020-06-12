package com.tencent.iot.explorer.link.core.demo.activity

import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.provider.Settings
import android.view.View
import com.tencent.iot.explorer.link.core.auth.IoTAuth
import com.tencent.iot.explorer.link.core.auth.callback.MyCallback
import com.tencent.iot.explorer.link.core.auth.http.RetryJob
import com.tencent.iot.explorer.link.core.auth.http.RetryListener
import com.tencent.iot.explorer.link.core.auth.response.BaseResponse
import com.tencent.iot.explorer.link.core.demo.App
import com.tencent.iot.explorer.link.core.demo.R
import com.tencent.iot.explorer.link.core.demo.log.L
import com.tencent.iot.explorer.link.core.link.IoTLink
import com.tencent.iot.explorer.link.core.link.entity.DeviceInfo
import com.tencent.iot.explorer.link.core.link.entity.DeviceTask
import com.tencent.iot.explorer.link.core.link.listener.SmartConfigListener
import com.tencent.iot.explorer.link.core.link.listener.SoftAPListener
import com.tencent.iot.explorer.link.core.link.service.SmartConfigService
import com.tencent.iot.explorer.link.core.link.service.SoftAPService
import kotlinx.android.synthetic.main.activity_connect_device.*
import kotlinx.android.synthetic.main.menu_back_layout.*
import java.net.InetAddress

/**
 * 智能配网
 */
class ConnectDeviceActivity : BaseActivity() {

    private val task = DeviceTask()
    private var retryJob: RetryJob? = null

    private var type = 1

    override fun getContentView(): Int {
        return R.layout.activity_connect_device
    }

    override fun initView() {
        type = get<Int>("type") ?: 1
        if (type == 1) {
            showSmartConfig()
        } else {
            showSoftAP()
            retryJob = RetryJob()
        }
        tv_progress_hint.text = "未开始"
    }

    override fun onResume() {
        super.onResume()
        (applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager)?.connectionInfo?.let {
            L.d("当前WIFI名：${it.ssid}")
            tv_select_wifi.text = it.ssid.replace("\"", "")
            if (type == 1) {
                task.mSsid = it.ssid
                task.mBssid = it.bssid ?: ""
            } else {
                if (tv_select_hotspot.visibility == View.VISIBLE) {
                    task.mSsid = it.ssid
                    task.mBssid = it.bssid ?: ""
                } else {
                    tv_reselect_hotspot.visibility = View.VISIBLE
                    tv_start_connect.visibility = View.VISIBLE
                    iv_select_wifi.visibility = View.GONE
                    et_select_wifi_pwd.visibility = View.INVISIBLE
                    smart_second_wifi_pwd.visibility = View.INVISIBLE
                }
            }
        }
    }

    override fun setListener() {
        iv_back.setOnClickListener { finish() }
        tv_start_connect.setOnClickListener {
            task.mPassword = et_select_wifi_pwd.text.trim().toString()
            if (type == 1) {
                smartConfig()
            } else {
                softAP()
            }
            it.isClickable = false
            it.alpha = 0.6f
            tv_reselect_hotspot.visibility = View.INVISIBLE
        }
        iv_select_wifi.setOnClickListener {
            startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
        }
        tv_select_hotspot.setOnClickListener {
            tv_select_hotspot.visibility = View.GONE
            startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
        }
        tv_reselect_hotspot.setOnClickListener {
            startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
        }
    }

    private fun showSmartConfig() {
        tv_title.text = "智能配网"
        tv_reselect_hotspot.visibility = View.INVISIBLE
        tv_select_hotspot.visibility = View.INVISIBLE
    }

    private fun showSoftAP() {
        tv_title.text = "自助配网"
        tv_select_hotspot.visibility = View.VISIBLE
        tv_start_connect.visibility = View.INVISIBLE
    }


    /**
     * 绑定设备
     */
    private fun bindDevice(deviceInfo: DeviceInfo) {
        App.data.getCurrentFamily().run {
            IoTAuth.deviceImpl.wifiBindDevice(FamilyId, deviceInfo,
                object : MyCallback {
                    override fun fail(msg: String?, reqCode: Int) {
                        L.e(msg ?: "")
                        showProgressText(msg)
                    }

                    override fun success(response: BaseResponse, reqCode: Int) {
                        if (response.isSuccess())
                            showProgressText("绑定成功")
                        else
                            showProgressText(response.msg)
                        //取消网络请求重发监听
                        retryJob?.stop()
                    }
                })
        }
    }

    /**
     * 多次重发绑定请求
     */
    private fun retry(deviceInfo: DeviceInfo) {
        retryJob?.start(object : RetryListener {
            override fun onRetry() {
                bindDevice(deviceInfo)
            }
        })
    }


    /**
     * 智能配网
     */
    private fun smartConfig() {
        val service = SmartConfigService(this, task)
        service.listener = object : SmartConfigListener {
            override fun connectedToWifi(
                isSuccess: Boolean, bssid: String, isCancel: Boolean, inetAddress: InetAddress
            ) {
                showProgressText(
                    if (isSuccess) {
                        "设备联网成功，正在获取签名"
                    } else {
                        "设备联网失败，重试中"
                    }
                )
            }

            override fun connectFailed() {
                showProgressText("设备联网失败,停止配网")
            }

            override fun onSuccess(deviceInfo: DeviceInfo) {
                showProgressText("获取签名成功,开始绑定设备")
                bindDevice(deviceInfo)
            }

            override fun onFail(code: String, msg: String) {
                showProgressText(msg)
            }
        }
        IoTLink.instance.start(service)
        showProgressText("正在连接设备")
    }

    /**
     * 自助配网
     */
    private fun softAP() {
        val service = SoftAPService(this, task)
        service.listener = object : SoftAPListener {
            override fun connectedToWifi(deviceInfo: DeviceInfo) {
                showProgressText("手机联网成功，开始绑定设备")
                //重新连接上网络时请求不一定成功，所以在这里做请求监听
                retry(deviceInfo)
            }

            override fun connectFailed(deviceInfo: DeviceInfo, ssid: String) {
                showProgressText("手机联网失败，尝试使用其它网络绑定设备")
                //重连网络失败，可以做监听
                retry(deviceInfo)
            }

            override fun onSuccess(deviceInfo: DeviceInfo) {
                showProgressText("获取签名成功,与热点断开并尝试连接网络")
            }

            override fun onFail(code: String, msg: String) {
                showProgressText(msg)
            }
        }
        IoTLink.instance.start(service)
        showProgressText("正在连接设备")
    }

    private fun showProgressText(test: String?) {
        tv_progress_hint.text = test ?: ""
    }

    override fun onDestroy() {
        IoTLink.instance.stop()
        super.onDestroy()
    }
}
