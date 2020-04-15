package com.mvp.model

import android.content.Context
import android.location.Location
import com.espressif.iot.esptouch.IEsptouchResult
import com.kitlink.App
import com.kitlink.device.*
import com.kitlink.device.smartconfig.SmartConfigListener
import com.kitlink.device.smartconfig.SmartConfigService
import com.kitlink.device.smartconfig.SmartConfigStep
import com.kitlink.device.smartconfig.SmartConfigTask
import com.kitlink.device.softap.SoftAPListener
import com.kitlink.device.softap.SoftAPService
import com.kitlink.device.softap.SoftAPStep
import com.kitlink.device.softap.SoftApTask
import com.kitlink.fragment.WifiFragment
import com.kitlink.response.BaseResponse
import com.kitlink.util.ConnectionListener
import com.kitlink.util.HttpRequest
import com.kitlink.util.MyCallback
import com.kitlink.util.Reconnect
import com.mvp.ParentModel
import com.mvp.view.ConnectView
import com.util.L

/**
 * 配网进度、绑定设备
 */
class ConnectModel(view: ConnectView) : ParentModel<ConnectView>(view), MyCallback {

    var smartConfig: SmartConfigService? = null
    var softAP: SoftAPService? = null
    var ssid = ""
    var bssid = ""
    var password = ""

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
            this.onStep(SmartConfigStep.STEP_DEVICE_BOUND)
            wifiBindDevice(deviceInfo)
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
            val task = SmartConfigTask()
            task.mSsid = ssid
            task.mBssid = bssid
            task.mPassword = password
            task.mAccessToken = "none"
            task.mLocation = location
            L.e("ssid:$ssid")
            it.startConnect(task, smartConfigListener)
        }
    }

    private var deviceInfo: DeviceInfo? = null
    private val connectionListener = object : ConnectionListener {
        override fun onConnected() {
            wifiBindDevice(deviceInfo!!)
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
            val task = SoftApTask()
            task.mSsid = ssid
            task.mBssid = bssid
            task.mPassword = password
            task.mAccessToken = "none"
            task.mLocation = location
            L.e("ssid:$ssid,password:$password")
            it.startConnect(task, softAPListener)
        }
    }

    /**
     *  绑定设备
     */
    private fun wifiBindDevice(data: DeviceInfo) {
        HttpRequest.instance.wifiBindDevice(App.data.getCurrentFamily().FamilyId, data, this)
    }

    override fun fail(msg: String?, reqCode: Int) {
        L.e(msg ?: "")
    }

    override fun success(response: BaseResponse, reqCode: Int) {
        if (response.isSuccess()) {
            if (type == WifiFragment.smart_config) {
                smartConfigListener.onStep(SmartConfigStep.STEP_LINK_SUCCESS)
            } else {
                softAPListener.onStep(SoftAPStep.STEP_LINK_SUCCESS)
            }
            view?.connectSuccess()
        } else {
            view?.connectFail("bind_fail", response.msg)
        }
        //绑定操作响应后，不论结果如何,一律停止监听。
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