package com.tencent.iot.explorer.link.mvp.presenter

import android.content.Context
import com.tencent.iot.explorer.link.kitlink.fragment.DeviceFragment
import com.tencent.iot.explorer.link.mvp.ParentPresenter
import com.tencent.iot.explorer.link.mvp.model.ConnectModel
import com.tencent.iot.explorer.link.mvp.view.ConnectView

class ConnectPresenter(view: ConnectView) :
    ParentPresenter<ConnectModel, ConnectView>(view) {

    override fun getIModel(view: ConnectView): ConnectModel {
        return ConnectModel(view)
    }

    fun initService(type: Int, context: Context) {
        model?.type = type
        model?.initService(type, context)
    }

    fun isNullService(type: Int): Boolean {
        if (model == null) throw NullPointerException("ConnectModel is not init")
        return if (type == DeviceFragment.ConfigType.SmartConfig.id) {
            model!!.smartConfig == null
        } else {
            model!!.softAP == null
        }
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
            if (type == DeviceFragment.ConfigType.SmartConfig.id) {
                startSmartConnect()
            } else {
                startSoftAppConnect()
            }
        }
    }

    /**
     * 停止配网
     */
    fun stopConnect() {
        model?.run {
            if (type == DeviceFragment.ConfigType.SmartConfig.id) {
                model?.smartConfig?.stopConnect()
            } else {
                model?.softAP?.stopConnect()
                model?.softAP = null
            }
        }
    }

}