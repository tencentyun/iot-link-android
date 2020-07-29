package com.tencent.iot.explorer.link.kitlink.fragment

import android.content.Intent
import android.graphics.Color
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.core.link.entity.SmartConfigStep
import com.tencent.iot.explorer.link.core.log.L
import com.tencent.iot.explorer.link.kitlink.activity.ConfigNetFailedActivity
import com.tencent.iot.explorer.link.kitlink.activity.HelpCenterActivity
import com.tencent.iot.explorer.link.kitlink.activity.SoftApActivity
import com.tencent.iot.explorer.link.mvp.IPresenter
import com.tencent.iot.explorer.link.mvp.presenter.ConnectPresenter
import com.tencent.iot.explorer.link.mvp.view.ConnectView
import com.tencent.iot.explorer.link.util.T
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
    private var rotate = RotateAnimation(0f, 360f,
        Animation.RELATIVE_TO_SELF, 0.5f,
        Animation.RELATIVE_TO_SELF, 0.5f)

    var onRestartListener: OnRestartListener? = null
    private var state = 0;

    init {
        this.type = type
        val lin = LinearInterpolator()
        rotate.setInterpolator(lin)
        rotate.setDuration(2000) //设置动画持续周期
        rotate.setRepeatCount(-1) //设置重复次数
        rotate.setFillAfter(true) //动画执行完后是否停留在执行完的状态
        rotate.setStartOffset(10) //执行前的等待时间
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
        refreshView()
        presenter = ConnectPresenter(this)
        presenter.setWifiInfo(ssid, bssid, wifiPassword)
        presenter.initService(type, context!!)
        presenter.startConnect()
    }

    private fun refreshView() {
        when(state) {
            0 -> {
                iv_phone_connect_device.animation = rotate
                iv_phone_send_device.animation = rotate
                iv_device_connect_cloud.animation = rotate
                iv_init_success.animation = rotate
                tv_phone_connect_device.setTextColor(resources.getColor(R.color.uncomplete_progress))
                tv_phone_connect_device.setTextColor(resources.getColor(R.color.uncomplete_progress))
                tv_phone_connect_device.setTextColor(resources.getColor(R.color.uncomplete_progress))
                tv_phone_connect_device.setTextColor(resources.getColor(R.color.uncomplete_progress))
            }
            1 -> {
                iv_phone_connect_device.animation = null
                tv_phone_connect_device.setTextColor(Color.BLACK)
            }
            2 -> {
                iv_phone_send_device.animation = null
                tv_phone_send_device.setTextColor(Color.BLACK)
            }
            3 -> {
                iv_device_connect_cloud.animation = null
                tv_phone_send_device.setTextColor(Color.BLACK)
            }
            4 -> {
                iv_init_success.animation = null
                tv_phone_send_device.setTextColor(Color.BLACK)
            }
        }
    }

    override fun onClick(v: View?) {
        when (v) {
            tv_connect_again -> {
                wp_connected.setProgress(0)
//                showConnecting()
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

    override fun connectSuccess() {
        activity?.run {
            runOnUiThread {
                refreshView()
            }
        }
    }

    //enum class SmartConfigStep {
    //    /**
    //     * 开始配网
    //     */
    //    STEP_LINK_START,
    //    /**
    //     * 正在配网
    //     */
    //    STEP_DEVICE_CONNECTING,
    //    /**
    //     * 发送wifi信息给设备
    //     */
    //    STEP_SEND_WIFI_INFO,
    //    /**
    //     * 设备成功连接到wifi
    //     */
    //    STEP_DEVICE_CONNECTED_TO_WIFI,
    //    /**
    //     * 从设备端获取到设备信息
    //     */
    //    STEP_GOT_DEVICE_INFO,
    //    /**
    //     * 开始绑定
    //     */
    //    STEP_DEVICE_BOUND,
    //    /**
    //     * 配网成功(包括配网、绑定)
    //     */
    //    STEP_LINK_SUCCESS
    //}

    override fun connectStep(step: Int) {
        Log.e("XXX", "step=" + step)
        activity?.run {
            runOnUiThread {
                when(step) {
                    SmartConfigStep.STEP_DEVICE_CONNECTED_TO_WIFI.ordinal -> {
                        state = 1
                    }
                    SmartConfigStep.STEP_GOT_DEVICE_INFO.ordinal -> {
                        state = 2
                    }
                    SmartConfigStep.STEP_DEVICE_BOUND.ordinal -> {
                        state = 3
                    }
                    SmartConfigStep.STEP_LINK_SUCCESS.ordinal -> {
                        state = 4
                    }
                }
                refreshView()
            }
        }
    }

    override fun deviceConnectToWifiFail() {
        activity?.run {
            runOnUiThread {
//                wp_connected?.setProgress(0)
//                T.show("网络连接失败，请检查密码是否正确")
//                showConnectFail()
            }
        }
    }

    override fun softApConnectToWifiFail(ssid: String) {
        activity?.runOnUiThread {
//            T.show("连接到网络：$ssid 失败，请手动连接")
        }
    }

    override fun connectFail(code: String, message: String) {
        activity?.run {
            runOnUiThread {
                startActivity(Intent(context, ConfigNetFailedActivity::class.java))
            }
        }
    }

    interface OnRestartListener {
        fun restart()
    }

}