package com.tencent.iot.explorer.link.kitlink.fragment

import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.provider.Settings
import android.view.View
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.mvp.IPresenter
import kotlinx.android.synthetic.main.fragment_soft_hotspot.*

class SoftHotspotFragment : BaseFragment() {

    var onNextListener: OnNextListener? = null
    private var isFirst = true

    override fun getPresenter(): IPresenter? {
        return null
    }

    override fun getContentView(): Int {
        return R.layout.fragment_soft_hotspot
    }

    override fun onResume() {
        super.onResume()
        showButton()
    }

    private fun showConnect() {
        tv_soft_connect_hotspot.visibility = View.VISIBLE
        tv_soft_reconnect_hotspot.visibility = View.GONE
        tv_soft_connect_confirm.visibility = View.GONE
    }

    private fun showReconnect() {
        tv_soft_connect_hotspot.visibility = View.GONE
        tv_soft_reconnect_hotspot.visibility = View.VISIBLE
        tv_soft_connect_confirm.visibility = View.VISIBLE
    }

    private fun showButton() {
        if (isFirst) return
        val wifiManager =
            context!!.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        if (wifiManager.connectionInfo.bssid != null) {
            showReconnect()
        } else {
            showConnect()
        }
        isFirst = false
    }

    override fun startHere(view: View) {
        setListener()
    }

    private fun setListener() {
        tv_soft_connect_hotspot.setOnClickListener {
            startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
            isFirst = false
        }
        tv_soft_reconnect_hotspot.setOnClickListener {
            startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
        }
        tv_soft_connect_confirm.setOnClickListener {
            onNextListener?.onNext()
            isFirst = true
            showConnect()
        }
    }

    interface OnNextListener {
        fun onNext()
    }


}