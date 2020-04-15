package com.mvp.presenter

import android.content.Context
import com.kitlink.fragment.WifiFragment
import com.mvp.ParentPresenter
import com.mvp.model.ConnectModel
import com.mvp.view.ConnectView

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
        return if (type == WifiFragment.smart_config) {
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
            if (type == WifiFragment.smart_config) {
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
            if (type == WifiFragment.smart_config) {
                model?.smartConfig?.stopConnect()
            } else {
                model?.softAP?.stopConnect()
                model?.softAP = null
            }
        }
    }

}