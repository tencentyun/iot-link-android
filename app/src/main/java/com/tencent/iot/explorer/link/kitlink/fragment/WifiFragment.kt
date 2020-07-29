package com.tencent.iot.explorer.link.kitlink.fragment

import android.content.Context
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import com.alibaba.fastjson.JSON
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.core.utils.PingUtil
import com.tencent.iot.explorer.link.customview.dialog.ListWifiDialog
import com.tencent.iot.explorer.link.customview.dialog.WifiListAdapter
import com.tencent.iot.explorer.link.kitlink.consts.CommonField
import com.tencent.iot.explorer.link.mvp.IPresenter
import com.tencent.iot.explorer.link.util.T
import com.tencent.iot.explorer.link.util.check.LocationUtil
import com.tencent.iot.explorer.link.util.keyboard.KeyBoardUtils
import kotlinx.android.synthetic.main.fragment_wifi.*
import kotlinx.android.synthetic.main.smart_config_second.*


/**
 * 输入wifi密码
 */
class WifiFragment(type: Int) : BaseFragment() {

    private var type = 0
    private var wifiInfo: WifiInfo? = null
    private var showPwd = false

    var onCommitWifiListener: OnCommitWifiListener? = null

    companion object {
        const val smart_config = 0
        const val soft_ap = 1
    }

    init {
        this.type = type
        showWifiInfo()
    }

    /**
     * 展示wifi
     */
    private fun showWifiInfo() {
        context?.let {
            val wifiManager = it.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            wifiInfo = wifiManager.connectionInfo
            if (wifiInfo == null || wifiInfo!!.bssid == null) {
                tv_select_wifi.hint = getString(R.string.not_network)
                tv_wifi_commit.isEnabled = false
            } else {
                tv_select_wifi.setText(wifiManager.connectionInfo.ssid.replace("\"", ""))
                if (tv_select_wifi.text.contains(CommonField.SSID_UNKNOWN)) {
                    T.show(getString(R.string.open_location_tip))
                }
            }
            tv_select_wifi.isEnabled = type == soft_ap
        }
    }

    override fun getPresenter(): IPresenter? {
        return null
    }

    override fun getContentView(): Int {
        return R.layout.fragment_wifi
    }

    override fun onResume() {
        showWifiInfo()
        super.onResume()
    }

    override fun startHere(view: View) {
        showWifiInfo()
        et_select_wifi_pwd.addClearImage(iv_wifi_eye_clear)
        et_select_wifi_pwd.addShowImage(iv_wifi_eye, R.mipmap.icon_visible, R.mipmap.icon_invisible)

        tv_select_wifi.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                s?.let {
                    tv_wifi_commit.isEnabled = it.isNotEmpty()
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        tv_wifi_commit.setOnClickListener {
            wifiInfo?.let {
                onCommitWifiListener?.commitWifi(
                    it.ssid.replace("\"", ""),
                    it.bssid,
                    et_select_wifi_pwd.text.trim().toString()
                )
            }
            PingUtil.connect(context!!, tv_select_wifi.text.toString(),
                "", et_select_wifi_pwd.text.toString())
            KeyBoardUtils.hideKeyBoard(context, et_select_wifi_pwd)
        }

        container_wifi.setOnClickListener {
            KeyBoardUtils.hideKeyBoard(context, et_select_wifi_pwd)
        }

        iv_select_wifi.setOnClickListener {

            val dialog = ListWifiDialog(context, object: WifiListAdapter.OnWifiClicked {
                override fun OnWifiClicked(item: com.tencent.iot.explorer.link.customview.dialog.WifiInfo?) {
                    tv_select_wifi.setText(item!!.ssid)
                }
            })
            dialog.show()
            dialog.setCanceledOnTouchOutside(true)
        }
    }

    interface OnCommitWifiListener {
        fun commitWifi(ssid: String, bssid: String?, password: String)
    }
}