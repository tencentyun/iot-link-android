package com.tencent.iot.explorer.link.core.demo.activity

import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.view.View
import com.alibaba.fastjson.JSON
import com.espressif.iot.esptouch.IEsptouchResult
import com.tencent.iot.explorer.link.core.auth.IoTAuth
import com.tencent.iot.explorer.link.core.auth.callback.MyCallback
import com.tencent.iot.explorer.link.core.auth.http.ConnectionListener
import com.tencent.iot.explorer.link.core.auth.http.Reconnect
import com.tencent.iot.explorer.link.core.auth.http.RetryJob
import com.tencent.iot.explorer.link.core.auth.response.BaseResponse
import com.tencent.iot.explorer.link.core.auth.response.BindDeviceTokenResponse
import com.tencent.iot.explorer.link.core.auth.response.DeviceBindTokenStateResponse
import com.tencent.iot.explorer.link.core.demo.App
import com.tencent.iot.explorer.link.core.demo.R
import com.tencent.iot.explorer.link.core.demo.log.L
import com.tencent.iot.explorer.link.core.demo.response.UserInfoResponse
import com.tencent.iot.explorer.link.core.link.SmartConfigService
import com.tencent.iot.explorer.link.core.link.SoftAPService
import com.tencent.iot.explorer.link.core.link.entity.DeviceInfo
import com.tencent.iot.explorer.link.core.link.entity.LinkTask
import com.tencent.iot.explorer.link.core.link.entity.SmartConfigStep
import com.tencent.iot.explorer.link.core.link.entity.SoftAPStep
import com.tencent.iot.explorer.link.core.link.exception.TCLinkException
import com.tencent.iot.explorer.link.core.link.listener.SmartConfigListener
import com.tencent.iot.explorer.link.core.link.listener.SoftAPListener
import kotlinx.android.synthetic.main.activity_connect_device.*
import kotlinx.android.synthetic.main.menu_back_layout.*

/**
 * 智能配网
 */
class ConnectDeviceActivity : BaseActivity() {

    private val task = LinkTask()


    private var smartConfigService:SmartConfigService? = null
    private var softAPService:SoftAPService? = null
    private var retryJob: RetryJob? = null
    @Volatile
    var checkDeviceBindTokenStateStarted = false
    private val maxTime = 100   // 执行最大时间间隔
    private val interval = 2    // 每次执行的时间间隔
    private val unKnowError = 99  // 每次执行的时间间隔
    private var deviceInfo: DeviceInfo? = null
    private var pwd = ""

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
            var ssidName = it.ssid.replace("\"", "")
            tv_select_wifi.text = ssidName
            if (type == 1) {
                task.mSsid = ssidName
                task.mBssid = it.bssid ?: ""
                task.mAccessToken = App.data.bindDeviceToken
            } else {
                if (tv_select_hotspot.visibility == View.VISIBLE) {
                    task.mSsid = ssidName
                    task.mBssid = it.bssid ?: ""
                    task.mAccessToken = App.data.bindDeviceToken
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
     * 智能配网
     */
    private fun smartConfig() {
        smartConfigService = SmartConfigService(this)
        smartConfigService?.startConnect(task, smartConfigListener)

        showProgressText("正在连接设备")
    }

    private val smartConfigListener = object : SmartConfigListener {
        override fun onSuccess(deviceInfo: DeviceInfo) {
            this@ConnectDeviceActivity.deviceInfo = deviceInfo
            this.onStep(SmartConfigStep.STEP_DEVICE_BOUND)
            checkDeviceBindTokenState()
        }

        override fun deviceConnectToWifi(result: IEsptouchResult) {
        }

        override fun onStep(step: SmartConfigStep) {
            showProgressText(step.name)
        }

        override fun deviceConnectToWifiFail() {
            showProgressText("设备连接 wifi 失败")
        }

        override fun onFail(exception: TCLinkException) {
            showProgressText(exception.errorMessage)
        }
    }

    /**
     * 自助配网
     */
    private fun softAP() {
        softAPService = SoftAPService(this)
        softAPService?.startConnect(task, softAPListener)
        showProgressText("正在连接设备")
    }

    private val connectionListener = object : ConnectionListener {
        override fun onConnected() {
            checkDeviceBindTokenState()
        }
    }

    private val softAPListener = object : SoftAPListener {
        override fun onSuccess(deviceInfo: DeviceInfo) {
            this@ConnectDeviceActivity.deviceInfo = deviceInfo
            this.onStep(SoftAPStep.STEP_DEVICE_BOUND)
            L.e("开始绑定设备")
        }

        override fun reconnectedSuccess(deviceInfo: DeviceInfo) {
            Reconnect.instance.start(connectionListener)
        }

        override fun reconnectedFail(deviceInfo: DeviceInfo, ssid: String) {
            Reconnect.instance.start(connectionListener)
        }

        override fun onStep(step: SoftAPStep) {
            showProgressText(step.name)
        }

        override fun onFail(code: String, msg: String) {
            showProgressText(msg)
        }
    }

    private fun showProgressText(test: String?) {
        tv_progress_hint.text = test ?: ""
    }

    override fun onDestroy() {
        if (smartConfigService != null) {
            smartConfigService?.stopConnect()
            smartConfigService = null
        }
        super.onDestroy()
    }

    private fun checkDeviceBindTokenState() {
        if (checkDeviceBindTokenStateStarted) return

        var maxTimes2Try = maxTime / interval
        var currentNo = 0

        // 开启线程做网络请求
        Thread(Runnable {
            checkDeviceBindTokenState(currentNo, maxTimes2Try, interval)
        }).start()
        checkDeviceBindTokenStateStarted = true
    }

    private fun checkDeviceBindTokenState(currentNo: Int, maxTimes: Int, interval: Int) {
        // 结束递归的条件，避免失败后的无限递归
        if (currentNo >= maxTimes || currentNo < 0) {
            Reconnect.instance.stop(connectionListener)
            checkDeviceBindTokenStateStarted = false
            softAPListener.onFail(unKnowError.toString(), "获取设备与 token 的绑定状态失败")
            return
        }

        val nextNo = currentNo + 1
        IoTAuth.deviceImpl.checkDeviceBindTokenState(App.data.userInfo.UserID, App.data.bindDeviceToken,
        object: MyCallback{
            override fun fail(msg: String?, reqCode: Int) {
                // 失败进行到下一次的递归
                Thread.sleep(interval.toLong() * 1000)
                checkDeviceBindTokenState(nextNo, maxTimes, interval)
            }

            override fun success(response: BaseResponse, reqCode: Int) {
                // 只要有一次成功，就回调成功
                response.parse(DeviceBindTokenStateResponse::class.java)?.State.let {
                    when(it) {
                        2 -> {
                            Reconnect.instance.stop(connectionListener)
                            checkDeviceBindTokenStateStarted = false
                            Thread(Runnable {
                                wifiBindDevice(deviceInfo!!)
                            }).start()
                        } else -> {
                        // 主线程回调，子线程开启新的网络请求，避免阻塞主线程
                        Thread(Runnable{
                            Thread.sleep(interval.toLong() * 1000)
                            checkDeviceBindTokenState(nextNo, maxTimes, interval)
                        }).start()
                    }
                    }
                }
            }
        })
    }

    private fun wifiBindDevice(data: DeviceInfo) {
        IoTAuth.deviceImpl.wifiBindDevice(App.data.userInfo.UserID, App.data.bindDeviceToken,
            App.data.getCurrentFamily().FamilyId, data, object: MyCallback{
                override fun fail(msg: String?, reqCode: Int) {
                    TODO("Not yet implemented")
                }

                override fun success(response: BaseResponse, reqCode: Int) {
                    if (response.isSuccess()) {
                        when(type) {
                            1 -> smartConfigListener.onStep(SmartConfigStep.STEP_LINK_SUCCESS)
                            else -> softAPListener.onStep(SoftAPStep.STEP_LINK_SUCCESS)
                        }
                    }
                    //绑定操作响应后，不论结果如何，一律停止监听。
                    Reconnect.instance.stop(connectionListener)
                }

            })
    }
}
