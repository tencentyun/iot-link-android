package com.tencent.iot.explorer.link.kitlink.fragment

import android.content.Intent
import android.text.TextUtils
import android.view.View
import com.tencent.iot.explorer.link.App
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.core.log.L
import com.tencent.iot.explorer.link.kitlink.activity.HelpCenterActivity
import com.tencent.iot.explorer.link.kitlink.activity.SoftApActivity
import com.tencent.iot.explorer.link.mvp.IPresenter
import com.tencent.iot.explorer.link.mvp.presenter.ConnectPresenter
import com.tencent.iot.explorer.link.mvp.view.ConnectView
import com.tencent.iot.explorer.link.util.T
import com.tencent.iot.explorer.link.customview.progress.WaveProgress
import com.tencent.iot.explorer.link.kitlink.activity.SmartConnectActivity
import com.tencent.iot.explorer.link.kitlink.consts.CommonField
import com.tencent.iot.explorer.link.kitlink.consts.LoadViewTxtType
import kotlinx.android.synthetic.main.connected.*
import kotlinx.android.synthetic.main.connecting.*
import kotlinx.android.synthetic.main.fragment_connect_progress.*
import kotlinx.android.synthetic.main.unconnected.*

/**
 * 配网进度、绑定设备
 */
class ConnectProgressFragment(type: Int, loadType: Int, productId: String) : BaseFragment(), ConnectView, View.OnClickListener {

    private lateinit var presenter: ConnectPresenter
    private var type = WifiFragment.smart_config
    private var ssid = ""
    private var bssid = ""
    private var wifiPassword = ""
    private var loadType: Int
    private var productId: String

    var onRestartListener: OnRestartListener? = null

    init {
        this.type = type
        this.loadType = loadType
        this.productId = productId
    }

    fun setWifiInfo(ssid: String, bssid: String, wifiPassword: String) {
        this.ssid = ssid
        this.bssid = bssid
        this.wifiPassword = wifiPassword
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            if (presenter.isNullService(type))
                presenter.initService(type, context!!)
            presenter.setWifiInfo(ssid, bssid, wifiPassword)
            presenter.startConnect()
        }
    }


    override fun getPresenter(): IPresenter? {
        return presenter
    }

    override fun getContentView(): Int {
        return R.layout.fragment_connect_progress
    }

    override fun startHere(view: View) {
        showConnecting()
        setListener()
        presenter = ConnectPresenter(this)
        presenter.setWifiInfo(ssid, bssid, wifiPassword)
        presenter.initService(type, context!!)
        presenter.startConnect()
    }

    private fun setListener() {
        tv_connect_again.setOnClickListener(this)
        tv_tab_connect_way.setOnClickListener(this)
        tv_add_new_device.setOnClickListener(this)
        tv_back_to_home_page.setOnClickListener(this)
        tv_connect_more_cause.setOnClickListener(this)

        wp_connected.setOnIncreaseListener(object : WaveProgress.OnIncreaseListener {
            override fun finish(view: WaveProgress, progress: Int) {
                activity?.runOnUiThread {
                    tv_progress?.run {
                        text = "$progress"
                        if (progress >= 100) {
                            progress_bg.setBackgroundResource(R.drawable.bg_progress_100)
                            showConnectSuccess()
                        }
                    }
                }
            }
        })
    }

    override fun onClick(v: View?) {
        when (v) {
            tv_connect_again -> {
                presenter.stopConnect()
                wp_connected.setProgress(0)
                showConnecting()
                onRestartListener?.restart()
            }
            // 切换连接方式
            tv_tab_connect_way -> {
                activity?.finish()
                if (type == WifiFragment.smart_config) {
//                    jumpActivity(SoftApActivity::class.java)
                    startActivityWithExtra(SoftApActivity::class.java, loadType, productId)
                } else {
//                    jumpActivity(SmartConnectActivity::class.java)
                    startActivityWithExtra(SmartConnectActivity::class.java, loadType, productId)
                }
            }
            // 继续添加设备
            tv_add_new_device -> {
                activity?.finish()
                if (type == WifiFragment.soft_ap) {
//                    jumpActivity(SoftApActivity::class.java)
                    startActivityWithExtra(SoftApActivity::class.java, loadType, productId)
                } else {
//                    jumpActivity(SmartConnectActivity::class.java)
                    startActivityWithExtra(SmartConnectActivity::class.java, loadType, productId)
                }
            }
            tv_back_to_home_page -> {
                backToMain()
            }
            tv_connect_more_cause -> {
                jumpActivity(HelpCenterActivity::class.java)
            }
        }
    }

    private fun startActivityWithExtra(cls: Class<*>?, loadType: Int, productId: String) {
        var intent = Intent(context, cls)
        intent.putExtra(CommonField.LOAD_VIEW_TXT_TYPE, loadType)
        intent.putExtra(CommonField.PRODUCT_ID, productId)
        startActivity(intent)
    }

    override fun connectSuccess() {
        activity?.run {
            runOnUiThread {
                wp_connected.setProgress(100, true)
                App.data.setRefreshLevel(2)
            }
        }
    }

    override fun connectStep(step: Int) {
        activity?.run {
            runOnUiThread {
                L.e("progress=${step * 100 / 5}")
                wp_connected.setProgress(step * 100 / 5, true)
                progress_bg.setBackgroundResource(R.drawable.bg_progress)
            }
        }
    }

    override fun deviceConnectToWifiFail() {
        activity?.run {
            runOnUiThread {
                wp_connected?.setProgress(0)
                T.show(getString(R.string.connect_failed_check_password))
                showConnectFail()
            }
        }
    }

    override fun softApConnectToWifiFail(ssid: String) {
        activity?.runOnUiThread {
            T.show(getString(R.string.connect_ssid_failed_handle, ssid)) //"连接到网络：$ssid 失败，请手动连接"
        }
    }

    override fun connectFail(code: String, message: String) {
        activity?.run {
            runOnUiThread {
                showConnectFail()
                if (!TextUtils.isEmpty(message))
                    T.show(message)
                L.e("connectFail:$message")
            }
        }
    }


    private fun showConnecting() {
        if (connecting != null) {
            connecting.visibility = View.VISIBLE
            connected.visibility = View.GONE
            unconnected.visibility = View.GONE
        }
    }

    private fun showConnectSuccess() {
        if (connecting != null) {
            connecting.visibility = View.GONE
            connected.visibility = View.VISIBLE
            unconnected.visibility = View.GONE
        }
    }

    private fun showConnectFail() {
        if (connecting != null) {
            connecting.visibility = View.GONE
            connected.visibility = View.GONE
            unconnected.visibility = View.VISIBLE
        }
    }

    interface OnRestartListener {
        fun restart()
    }

}