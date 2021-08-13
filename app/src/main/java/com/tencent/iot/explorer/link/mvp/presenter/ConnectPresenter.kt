package com.tencent.iot.explorer.link.mvp.presenter

import android.content.Context
import com.tencent.iot.explorer.link.core.link.entity.BleDevice
import com.tencent.iot.explorer.link.kitlink.entity.ConfigType
import com.tencent.iot.explorer.link.mvp.ParentPresenter
import com.tencent.iot.explorer.link.mvp.model.ConnectModel
import com.tencent.iot.explorer.link.mvp.view.ConnectView

class ConnectPresenter(view: ConnectView) :
    ParentPresenter<ConnectModel, ConnectView>(view) {

    override fun getIModel(view: ConnectView): ConnectModel {
        return ConnectModel(view)
    }

    fun initService(type: Int, context: Context) {
        model?.initService(type, context)
    }

    fun setExtraInfo(bleDevice: BleDevice?) {
        model?.bleDevice = bleDevice
    }

    fun setWifiInfo(ssid: String, bssid: String, password: String) {
        model?.let {
            it.ssid = ssid
            it.bssid = bssid
            it.password = password
        }
    }

    /**
     * 开始配网
     */
    fun startConnect() {
        model?.run {
            when(type) {
                ConfigType.SmartConfig.id -> startSmartConnect()
                ConfigType.SoftAp.id -> startSoftAppConnect()
                ConfigType.BleConfig.id -> startBleConfigNet()
            }
        }
    }

    /**
     * 停止配网
     */
    fun stopConnect() {
        model?.run {
            when(type) {
                ConfigType.SmartConfig.id -> smartConfig?.stopConnect()
                ConfigType.SoftAp.id -> softAP?.stopConnect()
                ConfigType.BleConfig.id -> {

                }
                else -> return@run
            }
        }
    }
}