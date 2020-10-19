package com.tencent.iot.explorer.link.mvp.model

import android.content.Context
import android.location.Location
import com.espressif.iot.esptouch.IEsptouchResult
import com.tencent.iot.explorer.link.App
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.core.auth.http.ConnectionListener
import com.tencent.iot.explorer.link.core.auth.http.Reconnect
import com.tencent.iot.explorer.link.core.auth.response.DeviceBindTokenStateResponse
import com.tencent.iot.explorer.link.core.link.service.SmartConfigService
import com.tencent.iot.explorer.link.core.link.service.SoftAPService
import com.tencent.iot.explorer.link.core.link.entity.*
import com.tencent.iot.explorer.link.core.link.exception.TCLinkException
import com.tencent.iot.explorer.link.core.link.listener.SmartConfigListener
import com.tencent.iot.explorer.link.core.link.listener.SoftAPListener
import com.tencent.iot.explorer.link.core.log.L
import com.tencent.iot.explorer.link.kitlink.fragment.WifiFragment
import com.tencent.iot.explorer.link.kitlink.response.BaseResponse
import com.tencent.iot.explorer.link.kitlink.util.*
import com.tencent.iot.explorer.link.mvp.ParentModel
import com.tencent.iot.explorer.link.mvp.view.ConnectView
import com.tencent.iot.explorer.link.T

/**
 * 配网进度、绑定设备
 */
class ConnectModel(view: ConnectView) : ParentModel<ConnectView>(view), MyCallback {

    private val TAG = this.javaClass.simpleName
    private val maxTime = 100   // 执行最大时间间隔
    private val interval = 2    // 每次执行的时间间隔
    private val unKnowError = 99  // 每次执行的时间间隔

    var smartConfig: SmartConfigService? = null
    var softAP: SoftAPService? = null
    var ssid = ""
    var bssid = ""
    var password = ""
    @Volatile
    var checkDeviceBindTokenStateStarted = false
    private var deviceInfo: DeviceInfo? = null

    var type = WifiFragment.smart_config

    fun initService(type: Int, context: Context) {
        if (type == WifiFragment.smart_config) {
            smartConfig =
                SmartConfigService(context.applicationContext)
        } else {
            softAP = SoftAPService(context.applicationContext)
        }
    }

    /**
     *  智能配网监听
     */
    private val smartConfigListener = object : SmartConfigListener {
        override fun onSuccess(deviceInfo: DeviceInfo) {
            this@ConnectModel.deviceInfo = deviceInfo
            this.onStep(SmartConfigStep.STEP_DEVICE_BOUND)
            checkDeviceBindTokenState()
        }

        override fun deviceConnectToWifi(result: IEsptouchResult) {
        }

        override fun onStep(step: SmartConfigStep) {
            view.connectStep(step.ordinal)
        }

        override fun deviceConnectToWifiFail() {
            view.deviceConnectToWifiFail()
        }

        override fun onFail(exception: TCLinkException) {
            view.connectFail(exception.errorCode, exception.errorMessage)
        }
    }

    /**
     * 开始智能配网
     */
    fun startSmartConnect() {
        smartConfig?.let {
            val location = Location("temp")
            location.longitude = 0.0
            location.latitude = 0.0
            val task = LinkTask()
            task.mSsid = ssid
            task.mBssid = bssid
            task.mPassword = password
            task.mAccessToken = App.data.bindDeviceToken
            task.mLocation = location
            L.e("ssid:$ssid")
            it.startConnect(task, smartConfigListener)
        }
        deviceInfo = null
    }

    private val connectionListener = object : ConnectionListener {
        override fun onConnected() {
            checkDeviceBindTokenState()
        }
    }

    private val softAPListener = object : SoftAPListener {
        override fun onSuccess(deviceInfo: DeviceInfo) {
            this@ConnectModel.deviceInfo = deviceInfo
            this.onStep(SoftAPStep.STEP_DEVICE_BOUND)
            L.e("开始绑定设备")
        }

        override fun reconnectedSuccess(deviceInfo: DeviceInfo) {
            Reconnect.instance.start(connectionListener)
        }

        override fun reconnectedFail(deviceInfo: DeviceInfo, ssid: String) {
            view.softApConnectToWifiFail(ssid)
            Reconnect.instance.start(connectionListener)
        }

        override fun onStep(step: SoftAPStep) {
            view.connectStep(step.ordinal)
        }

        override fun onFail(code: String, msg: String) {
            view.connectFail(code, msg)
        }
    }

    /**
     * 开始自助配网
     */
    fun startSoftAppConnect() {

        softAP?.let {
            val location = Location("temp")
            location.longitude = 0.0
            location.latitude = 0.0
            val task = LinkTask()
            task.mSsid = ssid
            task.mBssid = bssid
            task.mPassword = password
            task.mAccessToken = App.data.bindDeviceToken
            task.mLocation = location
            task.mRegion = App.data.region
            L.d("ssid:$ssid, password:$password")
            it.startConnect(task, softAPListener)
        }
        deviceInfo = null
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
            softAPListener.onFail(unKnowError.toString(), T.getContext().getString(R.string.get_bind_state_failed)) //"获取设备与 token 的绑定状态失败"
            return
        }

        val nextNo = currentNo + 1
        HttpRequest.instance.checkDeviceBindTokenState(object: MyCallback{
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

    /**
     *  绑定设备
     */
    private fun wifiBindDevice(data: DeviceInfo) {
        HttpRequest.instance.wifiBindDevice(App.data.getCurrentFamily().FamilyId, data, this)
    }

    override fun fail(msg: String?, reqCode: Int) {
        L.e(msg ?: "")
        softAPListener.onFail(unKnowError.toString(), T.getContext().getString(R.string.get_family_bind_state_failed)) //"获取家庭与设备绑定关系失败"
    }

    override fun success(response: BaseResponse, reqCode: Int) {
        if (response.isSuccess()) {
            when(type) {
                WifiFragment.smart_config -> smartConfigListener.onStep(SmartConfigStep.STEP_LINK_SUCCESS)
                WifiFragment.soft_ap -> softAPListener.onStep(SoftAPStep.STEP_LINK_SUCCESS)
            }
            view?.connectSuccess()
        } else {
            view?.connectFail("bind_fail", response.msg)
        }
        //绑定操作响应后，不论结果如何，一律停止监听。
        Reconnect.instance.stop(connectionListener)
    }

    override fun onDestroy() {
        smartConfig?.stopConnect()
        softAP?.stopConnect()
        Reconnect.instance.stop(connectionListener)
        L.e("停止配网")
        super.onDestroy()
    }

}