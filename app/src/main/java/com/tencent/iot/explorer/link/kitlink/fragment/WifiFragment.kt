package com.tencent.iot.explorer.link.kitlink.fragment

import android.content.Context
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import com.alibaba.fastjson.JSON
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.core.utils.PingUtil
import com.tencent.iot.explorer.link.customview.dialog.ConnectWifiDialog
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
    private var bssid = ""
    var showTipTag = false  // 是否是连接设备热点标志

    constructor(type: Int, showTag: Boolean) : this(type) {
        showTipTag = showTag
    }

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

            } else {
                var ssid2Set = wifiManager.connectionInfo.ssid.replace("\"", "")
                if (ssid2Set.equals(CommonField.SSID_UNKNOWN) &&
                    !LocationUtil.isLocationServiceEnable(context)) {
                    ssid2Set = ""
                    tv_select_wifi.setHint(R.string.open_location_tip)
                }
                tv_select_wifi.setText(ssid2Set)
                bssid = wifiInfo!!.bssid
            }
            isNextClickable()

            if (showTipTag) {
                tv_method.visibility = View.VISIBLE
                tv_method_tip.visibility = View.VISIBLE
                tv_tip_wifi.setText(R.string.connect_dev_wifi)
                et_select_wifi_pwd.setHint(R.string.smart_config_second_hint_not_nessery)
            } else {
                tv_method.visibility = View.GONE
                tv_method_tip.visibility = View.GONE
                tv_tip_wifi.setText(R.string.input_wifi_pwd)
                et_select_wifi_pwd.setHint(R.string.smart_config_second_hint)
            }
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
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        tv_wifi_commit.setOnClickListener {

            KeyBoardUtils.hideKeyBoard(context, et_select_wifi_pwd)
            val dialog = ConnectWifiDialog(context)
            dialog.setOnDismisListener(object : ConnectWifiDialog.OnDismisListener {
                override fun OnDismisedBySuccess() {
                    onCommitWifiListener?.commitWifi(tv_select_wifi.text.toString().trim(),
                        bssid, et_select_wifi_pwd.text.trim().toString())
                }
            })
            dialog.show()

            Thread{
                kotlin.run {
                    var flag = PingUtil.connect(context!!, tv_select_wifi.text.toString().trim(),
                        bssid, et_select_wifi_pwd.text.toString().trim())

                    if (showTipTag) {   // 如果是切换设备热点，网络切换成功，即认为联网成功（使用设备的热点，无法 ping 同外网）
                        if (flag) {
                            dialog.setStatus(ConnectWifiDialog.CONNECT_WIFI_SUCCESS)
                        } else {
                            dialog.setStatus(ConnectWifiDialog.CONNECT_WIFI_FAILED)
                        }
                        dialog.refreshState()
                        return@run
                    }

                    // 热点配网和一键配网，切换网络需要验证网络是否可以 ping 通外网
                    var pingRet = false
                    if (flag) {     // 切换热点成功以后，尝试使用新热点的 ping 网络
                        for (i in 0..30) {  // 最多尝试 30 次的 ping 网测试
                            pingRet = PingUtil.ping()
                            if (pingRet) break  // 网络可用，跳出尝试
                            Thread.sleep(1000)
                        }
                    }

                    if (pingRet) {
                        dialog.setStatus(ConnectWifiDialog.CONNECT_WIFI_SUCCESS)
                    } else {
                        dialog.setStatus(ConnectWifiDialog.CONNECT_WIFI_FAILED)
                    }
                    dialog.refreshState()
                }
            }.start()
        }

        container_wifi.setOnClickListener {
            KeyBoardUtils.hideKeyBoard(context, et_select_wifi_pwd)
        }

        iv_select_wifi.setOnClickListener {

            val dialog = ListWifiDialog(context, object: WifiListAdapter.OnWifiClicked {
                override fun OnWifiClicked(item: com.tencent.iot.explorer.link.customview.dialog.WifiInfo?) {
                    tv_select_wifi.setText(item!!.ssid)
                    bssid = item!!.bssid
                    isNextClickable()
                }
            })
            dialog.show()
            dialog.setCanceledOnTouchOutside(true)
        }
    }

    private fun isNextClickable() {
        if (tv_select_wifi.text != null && (TextUtils.isEmpty(tv_select_wifi.text.toString())) ||
            tv_select_wifi.text.toString().equals(CommonField.SSID_UNKNOWN)) {
            tv_wifi_commit.isClickable = false
            tv_wifi_commit.background = getDrawable(context!!, R.drawable.bg_edit)
            return
        }

        tv_wifi_commit.isClickable = true
        tv_wifi_commit.background = getDrawable(context!!, R.drawable.btn_bg)
    }

    interface OnCommitWifiListener {
        fun commitWifi(ssid: String, bssid: String?, password: String)
    }
}