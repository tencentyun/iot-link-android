package com.tencent.iot.explorer.link.kitlink.activity

import android.content.Intent
import android.graphics.Color
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import com.tencent.iot.explorer.link.App
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.T
import com.tencent.iot.explorer.link.core.link.entity.SmartConfigStep
import com.tencent.iot.explorer.link.core.link.entity.SoftAPStep
import com.tencent.iot.explorer.link.kitlink.consts.CommonField
import com.tencent.iot.explorer.link.kitlink.fragment.ConnectProgressFragment
import com.tencent.iot.explorer.link.kitlink.fragment.DeviceFragment
import com.tencent.iot.explorer.link.mvp.IPresenter
import com.tencent.iot.explorer.link.mvp.presenter.ConnectPresenter
import com.tencent.iot.explorer.link.mvp.view.ConnectView
import kotlinx.android.synthetic.main.activity_connect_progress.*

class ConnectProgressActivity : PActivity(), ConnectView {

    private lateinit var presenter: ConnectPresenter
    private var type = DeviceFragment.ConfigType.SmartConfig.id
    private var ssid = ""
    private var bssid = ""
    private var wifiPassword = ""
    private var loadType = 0
    private var productId = ""
    private var rotate = RotateAnimation(0f, 360f,
        Animation.RELATIVE_TO_SELF, 0.5f,
        Animation.RELATIVE_TO_SELF, 0.5f)
    @Volatile
    private var state = ConnectProgressState.Init;

    enum class ConnectProgressState (val id:Int) {
        Init(0), MobileAndDeviceConnectSuccess(1), SendMessageToDeviceSuccess(2), DeviceConnectServiceSuccess(3),InitSuccess(4);
    }

    override fun getContentView(): Int {
        return R.layout.activity_connect_progress
    }

    override fun initView() {
        type = intent.getIntExtra(CommonField.CONFIG_TYPE, DeviceFragment.ConfigType.SmartConfig.id)
        val lin = LinearInterpolator()
        rotate.setInterpolator(lin)
        rotate.setDuration(2000) //设置动画持续周期
        rotate.setRepeatCount(-1) //设置重复次数
        rotate.setFillAfter(true) //动画执行完后是否停留在执行完的状态
        rotate.setStartOffset(10) //执行前的等待时间

        refreshView()
        presenter = ConnectPresenter(this)
        presenter.setWifiInfo(ssid, bssid, wifiPassword)
        presenter.initService(type, this)
        presenter.startConnect()
    }

    private fun refreshView() {
        run {
            runOnUiThread {
                when (state) {
                    ConnectProgressFragment.ConnectProgressState.Init -> { //初始状态
                        iv_phone_connect_device.animation = rotate
                        iv_phone_send_device.animation = rotate
                        iv_device_connect_cloud.animation = rotate
                        iv_init_success.animation = rotate
                        tv_phone_connect_device.setTextColor(resources.getColor(R.color.uncomplete_progress))
                        tv_phone_send_device.setTextColor(resources.getColor(R.color.uncomplete_progress))
                        tv_device_connect_cloud.setTextColor(resources.getColor(R.color.uncomplete_progress))
                        tv_init_success.setTextColor(resources.getColor(R.color.uncomplete_progress))
                    }
                    ConnectProgressFragment.ConnectProgressState.MobileAndDeviceConnectSuccess -> { //手机与设备连接成功状态
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
                    ConnectProgressFragment.ConnectProgressState.SendMessageToDeviceSuccess -> { //手机与设备连接成功，向设备发送消息成功状态
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
                    ConnectProgressFragment.ConnectProgressState.DeviceConnectServiceSuccess -> {//手机与设备连接成功，向设备发送消息成功，设备连接云端成功状态
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
                    ConnectProgressFragment.ConnectProgressState.InitSuccess -> {//手机与设备连接成功，向设备发送消息成功，设备连接云端成功，初始化成功状态
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

                        var successIntent = Intent(this, ConfigNetSuccessActivity::class.java)
                        successIntent.putExtra(CommonField.CONFIG_TYPE, type)
                        successIntent.putExtra(CommonField.DEVICE_NAME, presenter.model?.deviceInfo?.deviceName)
                        startActivity(successIntent)
                        finish()
                    }
                }
            }
        }
    }

    override fun setListener() {

    }

    override fun getPresenter(): IPresenter? {
        return presenter
    }

    override fun connectSuccess() {
        TODO("Not yet implemented")
    }

    // 根据回调，处理界面的进度步骤
    override fun connectStep(step: Int) {
        if (type == DeviceFragment.ConfigType.SmartConfig.id) {
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
        runOnUiThread {
            T.show(getString(R.string.connect_ssid_failed_handle, ssid)) //"连接到网络：$ssid 失败，请手动连接"
        }
    }

    override fun connectFail(code: String, message: String) {
        showfailedReason()
    }

    private fun showfailedReason() {
        run {
            runOnUiThread {
                var failedIntent = Intent(this, ConfigNetFailedActivity::class.java)
                failedIntent.putExtra(CommonField.CONFIG_TYPE, type)
                startActivity(failedIntent)
                finish()
            }
        }
    }
}