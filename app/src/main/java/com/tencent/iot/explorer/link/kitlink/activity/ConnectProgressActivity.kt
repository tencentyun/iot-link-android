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
import com.tencent.iot.explorer.link.customview.progress.bean.StepBean
import com.tencent.iot.explorer.link.kitlink.consts.CommonField
import com.tencent.iot.explorer.link.kitlink.fragment.DeviceFragment
import com.tencent.iot.explorer.link.kitlink.popup.CommonPopupWindow
import com.tencent.iot.explorer.link.mvp.IPresenter
import com.tencent.iot.explorer.link.mvp.presenter.ConnectPresenter
import com.tencent.iot.explorer.link.mvp.view.ConnectView
import kotlinx.android.synthetic.main.activity_connect_progress.*
import kotlinx.android.synthetic.main.activity_connect_progress.softap_step_progress
import kotlinx.android.synthetic.main.activity_connect_progress.tv_soft_ap_cancel
import kotlinx.android.synthetic.main.activity_connect_progress.tv_soft_ap_title
import java.util.ArrayList

class ConnectProgressActivity : PActivity(), ConnectView {

    private lateinit var presenter: ConnectPresenter
    private var type = DeviceFragment.ConfigType.SmartConfig.id
    private var ssid = ""
    private var bssid = ""
    private var wifiPassword = ""
    private var loadType = 0
    private var productId = ""
    private var quit = false
    private var rotate = RotateAnimation(0f, 360f,
        Animation.RELATIVE_TO_SELF, 0.5f,
        Animation.RELATIVE_TO_SELF, 0.5f)
    @Volatile
    private var state = ConnectProgressState.Init;

    private var closePopup: CommonPopupWindow? = null

    enum class ConnectProgressState (val id:Int) {
        Init(0), MobileAndDeviceConnectSuccess(1), SendMessageToDeviceSuccess(2), DeviceConnectServiceSuccess(3),InitSuccess(4);
    }

    override fun getContentView(): Int {
        return R.layout.activity_connect_progress
    }

    private fun refreshTypeView() {
        if (type == DeviceFragment.ConfigType.SoftAp.id){
            tv_soft_ap_title.setText(R.string.soft_config_network)
            val stepsBeanList = ArrayList<StepBean>()
            stepsBeanList.add(StepBean(getString(R.string.config_hardware)))
            stepsBeanList.add(StepBean(getString(R.string.set_target_wifi)))
            stepsBeanList.add(StepBean(getString(R.string.connect_device)))
            stepsBeanList.add(StepBean(getString(R.string.start_config_network)))
            softap_step_progress.currentStep = 4
            softap_step_progress.setStepViewTexts(stepsBeanList)
            softap_step_progress.setTextSize(12)
        } else {
            tv_soft_ap_title.setText(R.string.smart_config_config_network)
            val stepsBeanList = ArrayList<StepBean>()
            stepsBeanList.add(StepBean(getString(R.string.config_hardware)))
            stepsBeanList.add(StepBean(getString(R.string.select_wifi)))
            stepsBeanList.add(StepBean(getString(R.string.start_config_network)))
            softap_step_progress.currentStep = 3
            softap_step_progress.setStepViewTexts(stepsBeanList)
            softap_step_progress.setTextSize(12)
        }
    }

    private fun showPopup() {
        if (closePopup == null) {
            closePopup = CommonPopupWindow(this)
            closePopup?.setCommonParams(
                getString(R.string.exit_toast_title),
                getString(R.string.exit_toast_content)
            )
            closePopup?.setMenuText(getString(R.string.cancel), getString(R.string.confirm))
            closePopup?.setBg(soft_ap_bg)
            closePopup?.onKeyListener = object : CommonPopupWindow.OnKeyListener {
                override fun confirm(popupWindow: CommonPopupWindow) {
                    quit = true
                    if (type == DeviceFragment.ConfigType.SmartConfig.id) {
                        backTo(3)
                    } else {
                        backTo(4)
                    }
                }

                override fun cancel(popupWindow: CommonPopupWindow) {
                    popupWindow.dismiss()
                }
            }
        }
        closePopup?.show(soft_ap)
    }

    override fun initView() {
        productId = intent.getStringExtra(CommonField.PRODUCT_ID) ?: ""
        type = intent.getIntExtra(CommonField.CONFIG_TYPE, DeviceFragment.ConfigType.SmartConfig.id)
        if (intent.hasExtra(CommonField.SSID)) {
            ssid = intent.getStringExtra(CommonField.SSID)
        }
        if (intent.hasExtra(CommonField.BSSID)) {
            bssid = intent.getStringExtra(CommonField.BSSID)
        }
        if (intent.hasExtra(CommonField.PWD)) {
            wifiPassword = intent.getStringExtra(CommonField.PWD)
        }

        refreshTypeView()
        refreshView()

        val lin = LinearInterpolator()
        rotate.setInterpolator(lin)
        rotate.setDuration(2000) //设置动画持续周期
        rotate.setRepeatCount(-1) //设置重复次数
        rotate.setFillAfter(true) //动画执行完后是否停留在执行完的状态
        rotate.setStartOffset(10) //执行前的等待时间

        presenter = ConnectPresenter(this)
        presenter.setWifiInfo(ssid, bssid, wifiPassword)
        presenter.initService(type, this)
        presenter.startConnect()
    }

    private fun refreshView() {
        run {
            runOnUiThread {
                when (state) {
                    ConnectProgressState.Init -> { //初始状态
                        iv_phone_connect_device.animation = rotate
                        iv_phone_send_device.animation = rotate
                        iv_device_connect_cloud.animation = rotate
                        iv_init_success.animation = rotate
                        tv_phone_connect_device.setTextColor(resources.getColor(R.color.gray_A1A7B2))
                        tv_phone_send_device.setTextColor(resources.getColor(R.color.gray_A1A7B2))
                        tv_device_connect_cloud.setTextColor(resources.getColor(R.color.gray_A1A7B2))
                        tv_init_success.setTextColor(resources.getColor(R.color.gray_A1A7B2))
                    }
                    ConnectProgressState.MobileAndDeviceConnectSuccess -> { //手机与设备连接成功状态
                        iv_phone_connect_device.animation = null
                        iv_phone_send_device.animation = rotate
                        iv_device_connect_cloud.animation = rotate
                        iv_init_success.animation = rotate
                        iv_phone_connect_device.setImageResource(R.mipmap.task_selected)
                        iv_phone_send_device.setImageResource(R.mipmap.loading)
                        iv_device_connect_cloud.setImageResource(R.mipmap.loading)
                        iv_init_success.setImageResource(R.mipmap.loading)
                        tv_phone_connect_device.setTextColor(resources.getColor(R.color.black_15161A))
                        tv_phone_send_device.setTextColor(resources.getColor(R.color.gray_A1A7B2))
                        tv_device_connect_cloud.setTextColor(resources.getColor(R.color.gray_A1A7B2))
                        tv_init_success.setTextColor(resources.getColor(R.color.gray_A1A7B2))
                    }
                    ConnectProgressState.SendMessageToDeviceSuccess -> { //手机与设备连接成功，向设备发送消息成功状态
                        iv_phone_connect_device.animation = null
                        iv_phone_send_device.animation = null
                        iv_device_connect_cloud.animation = rotate
                        iv_init_success.animation = rotate
                        iv_phone_connect_device.setImageResource(R.mipmap.task_selected)
                        iv_phone_send_device.setImageResource(R.mipmap.task_selected)
                        iv_device_connect_cloud.setImageResource(R.mipmap.loading)
                        iv_init_success.setImageResource(R.mipmap.loading)
                        tv_phone_connect_device.setTextColor(resources.getColor(R.color.black_15161A))
                        tv_phone_send_device.setTextColor(resources.getColor(R.color.black_15161A))
                        tv_device_connect_cloud.setTextColor(resources.getColor(R.color.gray_A1A7B2))
                        tv_init_success.setTextColor(resources.getColor(R.color.gray_A1A7B2))
                    }
                    ConnectProgressState.DeviceConnectServiceSuccess -> {//手机与设备连接成功，向设备发送消息成功，设备连接云端成功状态
                        iv_phone_connect_device.animation = null
                        iv_phone_send_device.animation = null
                        iv_device_connect_cloud.animation = null
                        iv_init_success.animation = rotate
                        iv_phone_connect_device.setImageResource(R.mipmap.task_selected)
                        iv_phone_send_device.setImageResource(R.mipmap.task_selected)
                        iv_device_connect_cloud.setImageResource(R.mipmap.task_selected)
                        iv_init_success.setImageResource(R.mipmap.loading)
                        tv_phone_connect_device.setTextColor(resources.getColor(R.color.black_15161A))
                        tv_phone_send_device.setTextColor(resources.getColor(R.color.black_15161A))
                        tv_device_connect_cloud.setTextColor(resources.getColor(R.color.black_15161A))
                        tv_init_success.setTextColor(resources.getColor(R.color.gray_A1A7B2))
                    }
                    ConnectProgressState.InitSuccess -> {//手机与设备连接成功，向设备发送消息成功，设备连接云端成功，初始化成功状态
                        iv_phone_connect_device.animation = null
                        iv_phone_send_device.animation = null
                        iv_device_connect_cloud.animation = null
                        iv_init_success.animation = null
                        iv_phone_connect_device.setImageResource(R.mipmap.task_selected)
                        iv_phone_send_device.setImageResource(R.mipmap.task_selected)
                        iv_device_connect_cloud.setImageResource(R.mipmap.task_selected)
                        iv_init_success.setImageResource(R.mipmap.task_selected)
                        tv_phone_connect_device.setTextColor(resources.getColor(R.color.black_15161A))
                        tv_phone_send_device.setTextColor(resources.getColor(R.color.black_15161A))
                        tv_device_connect_cloud.setTextColor(resources.getColor(R.color.black_15161A))
                        tv_init_success.setTextColor(resources.getColor(R.color.black_15161A))
                        App.data.setRefreshLevel(2)

                        var successIntent = Intent(this, ConfigNetSuccessActivity::class.java)
                        successIntent.putExtra(CommonField.CONFIG_TYPE, type)
                        if (presenter.model?.deviceInfo?.deviceName != null) {
                            successIntent.putExtra(CommonField.DEVICE_NAME, presenter.model?.deviceInfo?.deviceName)
                        } else {
                            successIntent.putExtra(CommonField.DEVICE_NAME, "")
                        }
                        startActivity(successIntent)
                        backToDeviceCategoryActivity()
                    }
                }
            }
        }
    }

    override fun setListener() {
        tv_soft_ap_cancel.setOnClickListener {
            showPopup()
        }
    }

    override fun getPresenter(): IPresenter? {
        return presenter
    }

    override fun connectSuccess() {

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

    override fun onBackPressed() {
        showPopup()
    }

    override fun onDestroy() {
        closePopup?.dismiss()
        super.onDestroy()
    }

    private fun backToDeviceCategoryActivity() {
        var stop = false
        while (!stop) {
            if (App.data.activityList != null && App.data.activityList.size <= 0) break

            if (App.data.activityList.last != null && App.data.activityList.last is DeviceCategoryActivity) {
                stop = true
            } else {
                if (App.data.activityList.last != null) {
                    App.data.activityList.last.finish()
                    App.data.activityList.removeLast()
                }
            }
        }
    }

    private fun showfailedReason() {
        run {
            runOnUiThread {
                if (!quit) {
                    var failedIntent = Intent(this, ConfigNetFailedActivity::class.java)
                    failedIntent.putExtra(CommonField.CONFIG_TYPE, type)
                    failedIntent.putExtra(CommonField.PRODUCT_ID, productId)
                    startActivity(failedIntent)
                    backToDeviceCategoryActivity()
                }
            }
        }
    }
}