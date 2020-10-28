package com.tencent.iot.explorer.link.kitlink.fragment

import android.content.Intent
import android.graphics.Color
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import com.tencent.iot.explorer.link.App
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.core.link.entity.SmartConfigStep
import com.tencent.iot.explorer.link.core.link.entity.SoftAPStep
import com.tencent.iot.explorer.link.kitlink.consts.CommonField
import com.tencent.iot.explorer.link.mvp.IPresenter
import com.tencent.iot.explorer.link.mvp.presenter.ConnectPresenter
import com.tencent.iot.explorer.link.mvp.view.ConnectView
import com.tencent.iot.explorer.link.T
import com.tencent.iot.explorer.link.kitlink.activity.*
import kotlinx.android.synthetic.main.connected.*
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
    private var rotate = RotateAnimation(0f, 360f,
        Animation.RELATIVE_TO_SELF, 0.5f,
        Animation.RELATIVE_TO_SELF, 0.5f)

    var onRestartListener: OnRestartListener? = null
    @Volatile
    private var state = ConnectProgressState.Init;

    enum class ConnectProgressState (val id:Int) {
        Init(0), MobileAndDeviceConnectSuccess(1), SendMessageToDeviceSuccess(2), DeviceConnectServiceSuccess(3),InitSuccess(4);
    }

    init {
        this.type = type
        this.loadType = loadType
        this.productId = productId
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
        activity?.run {
            runOnUiThread {
                when (state) {
                    ConnectProgressState.Init -> { //初始状态
                        iv_phone_connect_device.animation = rotate
                        iv_phone_send_device.animation = rotate
                        iv_device_connect_cloud.animation = rotate
                        iv_init_success.animation = rotate
                        tv_phone_connect_device.setTextColor(resources.getColor(R.color.uncomplete_progress))
                        tv_phone_send_device.setTextColor(resources.getColor(R.color.uncomplete_progress))
                        tv_device_connect_cloud.setTextColor(resources.getColor(R.color.uncomplete_progress))
                        tv_init_success.setTextColor(resources.getColor(R.color.uncomplete_progress))
                    }
                    ConnectProgressState.MobileAndDeviceConnectSuccess -> { //手机与设备连接成功状态
                        iv_phone_connect_device.animation = null
                        iv_phone_send_device.animation = rotate
                        iv_device_connect_cloud.animation = rotate
                        iv_init_success.animation = rotate
                        iv_phone_connect_device.setImageResource(R.mipmap.wifi_selected)
                        iv_phone_send_device.setImageResource(R.mipmap.loading)
                        iv_device_connect_cloud.setImageResource(R.mipmap.loading)
                        iv_init_success.setImageResource(R.mipmap.loading)
                        tv_phone_connect_device.setTextColor(Color.BLACK)
                        tv_phone_send_device.setTextColor(resources.getColor(R.color.uncomplete_progress))
                        tv_device_connect_cloud.setTextColor(resources.getColor(R.color.uncomplete_progress))
                        tv_init_success.setTextColor(resources.getColor(R.color.uncomplete_progress))
                    }
                    ConnectProgressState.SendMessageToDeviceSuccess -> { //手机与设备连接成功，向设备发送消息成功状态
                        iv_phone_connect_device.animation = null
                        iv_phone_send_device.animation = null
                        iv_device_connect_cloud.animation = rotate
                        iv_init_success.animation = rotate
                        iv_phone_connect_device.setImageResource(R.mipmap.wifi_selected)
                        iv_phone_send_device.setImageResource(R.mipmap.wifi_selected)
                        iv_device_connect_cloud.setImageResource(R.mipmap.loading)
                        iv_init_success.setImageResource(R.mipmap.loading)
                        tv_phone_connect_device.setTextColor(Color.BLACK)
                        tv_phone_send_device.setTextColor(Color.BLACK)
                        tv_device_connect_cloud.setTextColor(resources.getColor(R.color.uncomplete_progress))
                        tv_init_success.setTextColor(resources.getColor(R.color.uncomplete_progress))
                    }
                    ConnectProgressState.DeviceConnectServiceSuccess -> {//手机与设备连接成功，向设备发送消息成功，设备连接云端成功状态
                        iv_phone_connect_device.animation = null
                        iv_phone_send_device.animation = null
                        iv_device_connect_cloud.animation = null
                        iv_init_success.animation = rotate
                        iv_phone_connect_device.setImageResource(R.mipmap.wifi_selected)
                        iv_phone_send_device.setImageResource(R.mipmap.wifi_selected)
                        iv_device_connect_cloud.setImageResource(R.mipmap.wifi_selected)
                        iv_init_success.setImageResource(R.mipmap.loading)
                        tv_phone_connect_device.setTextColor(Color.BLACK)
                        tv_phone_send_device.setTextColor(Color.BLACK)
                        tv_device_connect_cloud.setTextColor(Color.BLACK)
                        tv_init_success.setTextColor(resources.getColor(R.color.uncomplete_progress))
                    }
                    ConnectProgressState.InitSuccess -> {//手机与设备连接成功，向设备发送消息成功，设备连接云端成功，初始化成功状态
                        iv_phone_connect_device.animation = null
                        iv_phone_send_device.animation = null
                        iv_device_connect_cloud.animation = null
                        iv_init_success.animation = null
                        iv_phone_connect_device.setImageResource(R.mipmap.wifi_selected)
                        iv_phone_send_device.setImageResource(R.mipmap.wifi_selected)
                        iv_device_connect_cloud.setImageResource(R.mipmap.wifi_selected)
                        iv_init_success.setImageResource(R.mipmap.wifi_selected)
                        tv_phone_connect_device.setTextColor(Color.BLACK)
                        tv_phone_send_device.setTextColor(Color.BLACK)
                        tv_device_connect_cloud.setTextColor(Color.BLACK)
                        tv_init_success.setTextColor(Color.BLACK)
                        App.data.setRefreshLevel(2)

                        var successIntent = Intent(context, ConfigNetSuccessActivity::class.java)
                        successIntent.putExtra(CommonField.CONFIG_NET_TYPE, type)
                        successIntent.putExtra(CommonField.DEVICE_NAME, presenter.model?.deviceInfo?.deviceName)
                        startActivity(successIntent)
                        finish()
                    }
                }
            }
        }
    }

    override fun onClick(v: View?) {
        when (v) {
            tv_connect_again -> {
                presenter.stopConnect()
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

    private fun startActivityWithExtra(cls: Class<*>?, loadType: Int, productId: String) {
        var intent = Intent(context, cls)
        intent.putExtra(CommonField.LOAD_VIEW_TXT_TYPE, loadType)
        intent.putExtra(CommonField.PRODUCT_ID, productId)
        startActivity(intent)
    }

    override fun connectSuccess() {}

    // 根据回调，处理界面的进度步骤
    override fun connectStep(step: Int) {
        if (type == WifiFragment.smart_config) {
            when (step) {
                SmartConfigStep.STEP_DEVICE_CONNECTED_TO_WIFI.ordinal -> {
                    state = ConnectProgressState.MobileAndDeviceConnectSuccess
                    refreshView()
                }
                SmartConfigStep.STEP_GOT_DEVICE_INFO.ordinal -> {
                    state = ConnectProgressState.SendMessageToDeviceSuccess
                    refreshView()
                }
                SmartConfigStep.STEP_DEVICE_BOUND.ordinal -> {
                    state = ConnectProgressState.DeviceConnectServiceSuccess
                    refreshView()
                }
                SmartConfigStep.STEP_LINK_SUCCESS.ordinal -> {
                    state = ConnectProgressState.InitSuccess
                    refreshView()
                }
            }

        } else {
            when (step) {
                SoftAPStep.STEP_SEND_WIFI_INFO.ordinal -> {
                    state = ConnectProgressState.MobileAndDeviceConnectSuccess
                    refreshView()
                }
                SoftAPStep.STEP_GOT_DEVICE_INFO.ordinal -> {
                    state = ConnectProgressState.SendMessageToDeviceSuccess
                    refreshView()
                }
                SoftAPStep.STEP_DEVICE_BOUND.ordinal -> {
                    state = ConnectProgressState.DeviceConnectServiceSuccess
                    refreshView()
                }
                SoftAPStep.STEP_LINK_SUCCESS.ordinal -> {
                    state = ConnectProgressState.InitSuccess
                    refreshView()
                }
            }
        }
    }

    override fun deviceConnectToWifiFail() {
        showfailedReason()
    }

    override fun softApConnectToWifiFail(ssid: String) {
        activity?.runOnUiThread {
            T.show(getString(R.string.connect_ssid_failed_handle, ssid)) //"连接到网络：$ssid 失败，请手动连接"
        }
    }

    override fun connectFail(code: String, message: String) {
        showfailedReason()
    }

    interface OnRestartListener {
        fun restart()
    }

    private fun showfailedReason() {
        activity?.run {
            runOnUiThread {
                var failedIntent = Intent(context, ConfigNetFailedActivity::class.java)
                failedIntent.putExtra(CommonField.CONFIG_NET_TYPE, type)
                startActivity(failedIntent)
                finish()
            }
        }
    }

}