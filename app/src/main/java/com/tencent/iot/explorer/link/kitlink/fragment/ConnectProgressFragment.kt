package com.tencent.iot.explorer.link.kitlink.fragment

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
import kotlinx.android.synthetic.main.connected.*
import kotlinx.android.synthetic.main.connecting.*
import kotlinx.android.synthetic.main.fragment_connect_progress.*
import kotlinx.android.synthetic.main.unconnected.*

/**
 * 配网进度、绑定设备
 */
class ConnectProgressFragment(type: Int) : BaseFragment(), ConnectView, View.OnClickListener {

    private lateinit var presenter: ConnectPresenter
    private var type = WifiFragment.smart_config
    private var ssid = ""
    private var bssid = ""
    private var wifiPassword = ""

    var onRestartListener: OnRestartListener? = null

    init {
        this.type = type
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
                wp_connected.setProgress(0)
                showConnecting()
                presenter.stopConnect()
                onRestartListener?.restart()
            }
            tv_tab_connect_way, tv_add_new_device -> {
                activity?.finish()
                jumpActivity(SoftApActivity::class.java)
            }
            tv_back_to_home_page -> {
                backToMain()
            }
            tv_connect_more_cause -> {
                jumpActivity(HelpCenterActivity::class.java)
            }
        }
    }

    override fun connectSuccess() {}

    // 根据回调，处理界面的进度步骤
    override fun connectStep(step: Int) {
        if (type == WifiFragment.smart_config) {
            when (step) {
                SmartConfigStep.STEP_DEVICE_CONNECTED_TO_WIFI.ordinal -> {
                    state = 1
                    refreshView()
                }
                SmartConfigStep.STEP_GOT_DEVICE_INFO.ordinal -> {
                    state = 2
                    refreshView()
                }
                SmartConfigStep.STEP_DEVICE_BOUND.ordinal -> {
                    state = 3
                    refreshView()
                }
                SmartConfigStep.STEP_LINK_SUCCESS.ordinal -> {
                    state = 4
                    refreshView()
                }
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

        showfailedReason()
    }

    override fun softApConnectToWifiFail(ssid: String) {
        showfailedReason()
    }

    override fun connectFail(code: String, message: String) {
        showfailedReason()
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