package com.tencent.iot.explorer.link.kitlink.activity

import android.bluetooth.BluetoothGatt
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.tencent.iot.explorer.link.App
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.TRTCAppSessionManager
import com.tencent.iot.explorer.link.core.auth.IoTAuth
import com.tencent.iot.explorer.link.core.auth.callback.MyCallback
import com.tencent.iot.explorer.link.core.auth.entity.DeviceEntity
import com.tencent.iot.explorer.link.core.auth.entity.NavBar
import com.tencent.iot.explorer.link.core.auth.message.MessageConst
import com.tencent.iot.explorer.link.core.auth.response.BaseResponse
import com.tencent.iot.explorer.link.core.link.entity.*
import com.tencent.iot.explorer.link.core.link.exception.TCLinkException
import com.tencent.iot.explorer.link.core.link.listener.BleDeviceConnectionListener
import com.tencent.iot.explorer.link.core.link.service.BleConfigService
import com.tencent.iot.explorer.link.core.log.L
import com.tencent.iot.explorer.link.customview.recyclerview.CRecyclerView
import com.tencent.iot.explorer.link.kitlink.entity.DevicePropertyEntity
import com.tencent.iot.explorer.link.kitlink.entity.ProductEntity
import com.tencent.iot.explorer.link.kitlink.entity.ProductsEntity
import com.tencent.iot.explorer.link.kitlink.popup.EnumPopupWindow
import com.tencent.iot.explorer.link.kitlink.popup.NumberPopupWindow
import com.tencent.iot.explorer.link.kitlink.popup.OfflinePopupWindow
import com.tencent.iot.explorer.link.kitlink.theme.PanelThemeManager
import com.tencent.iot.explorer.link.kitlink.util.HttpRequest
import com.tencent.iot.explorer.link.kitlink.util.StatusBarUtil
import com.tencent.iot.explorer.link.mvp.IPresenter
import com.tencent.iot.explorer.link.mvp.presenter.ControlPanelPresenter
import com.tencent.iot.explorer.link.mvp.view.ControlPanelView
import com.tencent.iot.explorer.link.rtc.model.RoomKey
import com.tencent.iot.explorer.link.rtc.model.TRTCUIManager
import com.tencent.iot.explorer.link.rtc.ui.audiocall.TRTCAudioCallActivity
import com.tencent.iot.explorer.link.rtc.ui.utils.NetWorkStateReceiver
import com.tencent.iot.explorer.link.rtc.ui.videocall.TRTCVideoCallActivity
import kotlinx.android.synthetic.main.activity_control_panel.*
import kotlinx.android.synthetic.main.menu_back_and_right.*
import kotlinx.android.synthetic.main.menu_back_layout.*
import kotlinx.coroutines.*


/**
 * 控制面板
 */
class ControlPanelActivity : PActivity(), CoroutineScope by MainScope(), ControlPanelView, CRecyclerView.RecyclerItemView {
    private var TAG = ControlPanelActivity::class.java.simpleName
    private var deviceEntity: DeviceEntity? = null

    private lateinit var presenter: ControlPanelPresenter

    //    private var aliasName = ""
    private var numberPopup: NumberPopupWindow? = null
    private var enumPopup: EnumPopupWindow? = null
    private var offlinePopup: OfflinePopupWindow? = null
    private var job: Job? = null
    private var connectBleJob: Job? = null
    private var netWorkStateReceiver: NetWorkStateReceiver? = null

    override fun getContentView(): Int {
        return R.layout.activity_control_panel
    }

    override fun getPresenter(): IPresenter? {
        return presenter
    }

    override fun onResume() {
        super.onResume()
        tv_title.text = deviceEntity?.getAlias()
        presenter.requestDeviceData()
        presenter.getUserSetting()
        BleConfigService.get().connetionListener = bleDeviceConnectionListener
    }

    override fun initView() {
//        App.setEnableEnterRoomCallback(false)
        presenter = ControlPanelPresenter(this)
        netWorkStateReceiver = NetWorkStateReceiver()
        deviceEntity = get("device")
        deviceEntity?.run {
            presenter.setProductId(ProductId)
            presenter.setDeviceName(DeviceName)
            presenter.setDeviceId(DeviceId)
            //不能添加头部，否则bindView中gridLayoutManager的getSpanSize(position: Int)会出错
            PanelThemeManager.instance.bindView(this@ControlPanelActivity, crv_panel)
            crv_panel.setList(presenter.model!!.devicePropertyList)
            crv_panel.addRecyclerItemView(this@ControlPanelActivity)
            presenter.requestControlPanel()
            presenter.registerActivePush()
            getDeviceType(ProductId, object: OnTypeGeted {
                override fun onType(type: String) {
                    if (type == "ble") {
                        launch (Dispatchers.Main) {
                            startScanBleDev()
                        }
                    }
                }
            })

            if (online != 1) {//延时显示
                job = CoroutineScope(Dispatchers.IO).launch {
                    delay(200)
                    CoroutineScope(Dispatchers.Main).launch {
                        showOfflinePopup()
                    }
                }
            }
        }
    }

    private fun startScanBleDev() {
        ble_connect_layout.visibility = View.VISIBLE
        BleConfigService.get().startScanBluetoothDevices()
        search_ble_dev_layout.visibility = View.VISIBLE
        search_reault_layout.visibility = View.GONE
    }

    private fun stopScanBleDev(connected: Boolean) {
        launch(Dispatchers.Main) {
            ble_connect_layout.visibility = View.VISIBLE
            BleConfigService.get().stopScanBluetoothDevices()
            search_ble_dev_layout.visibility = View.GONE
            search_reault_layout.visibility = View.VISIBLE
            if (connected) {
                search_reault_layout.setBackgroundResource(R.color.blue_006EFF)
                retry_connect.setTextColor(this@ControlPanelActivity.resources.getColor(R.color.white))
                retry_connect.setText(R.string.break_ble_connect)
                retry_connect.setOnClickListener {
                    BleConfigService.get().bluetoothGatt?.let {
                        it?.close()
                        stopScanBleDev(false)
                    }
                }
            } else {
                search_reault_layout.setBackgroundResource(R.color.red_E65A59)
                retry_connect.setTextColor(this@ControlPanelActivity.resources.getColor(R.color.white))
                retry_connect.setText(R.string.scanning_retry)
                retry_connect.setOnClickListener { startScanBleDev() }
            }
        }
    }

    private var bleDeviceConnectionListener = object: BleDeviceConnectionListener {
        override fun onBleDeviceFounded(bleDevice: BleDevice) {
            if (bleDevice.productId == deviceEntity?.ProductId && !TextUtils.isEmpty(bleDevice.productId)) {
                //&& bleDevice.devName == deviceEntity?.DeviceName) {
                BleConfigService.get().bluetoothGatt = BleConfigService.get().connectBleDeviceAndGetLocalPsk(bleDevice, presenter.getProductId(), presenter.getDeviceName())
            } else if (!TextUtils.isEmpty(bleDevice.bindTag)) {
                deviceEntity?.let {
                    if (bleDevice.bindTag == BleConfigService.bytesToHex(BleConfigService.getBindTag(it.ProductId, it.DeviceName))) {
                        BleConfigService.get().bluetoothGatt = BleConfigService.get().connectBleDeviceAndGetLocalPsk(bleDevice, presenter.getProductId(), presenter.getDeviceName())
                    }
                }
            }
        }

        override fun onBleDeviceConnected() {
            launch {
                BleConfigService.get().bluetoothGatt?.let {
                    delay(3000)
//                    if (BleConfigService.get().setMtuSize(it, 512)) return@launch
                    launch (Dispatchers.Main) {
                        delay(1000)
                        BleConfigService.get().bluetoothGatt?.let {
                            BleConfigService.get().stopScanBluetoothDevices()
                            if (!BleConfigService.get().connectSubDevice(it)) {
                                stopScanBleDev(false)
                                return@launch
                            } else {
                                connectBleJob = launch (Dispatchers.Main) {
                                    delay(10000)
                                    stopScanBleDev(false)
                                }
                            }
                        }
                    }
                    stopScanBleDev(false)
                }
            }
        }
        override fun onBleDeviceDisconnected(exception: TCLinkException) {

        }
        override fun onBleDeviceInfo(bleDeviceInfo: BleDeviceInfo) {}
        override fun onBleSetWifiModeResult(success: Boolean) {}
        override fun onBleSendWifiInfoResult(success: Boolean) {}
        override fun onBleWifiConnectedInfo(wifiConnectInfo: BleWifiConnectInfo) {}
        override fun onBlePushTokenResult(success: Boolean) {}
        override fun onMtuChanged(mtu: Int, status: Int) {
            L.d(TAG, "onMtuChanged mtu $mtu status $status")
        }
        override fun onBleBindSignInfo(bleDevBindCondition: BleDevBindCondition) {}
        override fun onBleSendSignInfo(bleDevSignResult: BleDevSignResult) {
            stopScanBleDev(true)
            connectBleJob?.cancel()
        }
        override fun onBleUnbindSignInfo(signInfo: String) {}
        override fun onBlePropertyValue(bleDeviceProperty: BleDeviceProperty) {
            L.d(TAG, "onBlePropertyValue $bleDeviceProperty ")
        }
        override fun onBleControlPropertyResult(result: Int) {}
        override fun onBleRequestCurrentProperty() {
            presenter.model?.getBleDeviceStatus()?.let {
                BleConfigService.get().sendCurrentBleDeviceProperty(BleConfigService.get().bluetoothGatt,
                    it
                )
            }
        }
        override fun onBleNeedPushProperty(eventId: Int, bleDeviceProperty: BleDeviceProperty) {}
        override fun onBleReportActionResult(reason: Int, actionId: Int, bleDeviceProperty: BleDeviceProperty) {}
        override fun onBleDeviceFirmwareVersion(firmwareVersion: BleDeviceFirmwareVersion) {
            if (firmwareVersion.mtuFlag == 1) { // 是否设置 mtu 当 mtu flag为 1 时，进行 MTU 设置；当 mtu flag 为 0 时，不设置 MTU
                BleConfigService.get().setMtuSize(BleConfigService.get().bluetoothGatt, firmwareVersion.mtuSize)
            }

            deviceEntity?.run {
                IoTAuth.deviceImpl.checkFirmwareUpdate(ProductId, DeviceName, object: MyCallback{
                    override fun fail(msg: String?, reqCode: Int) {

                    }

                    override fun success(response: BaseResponse, reqCode: Int) {
                    val json = response.data as JSONObject
                        val dstVersion = json.getString("DstVersion")
                        val currentVersion = json.getString("CurrentVersion")
                        if (!dstVersion.equals(currentVersion)) {

                        }
                    }
                })
            }
        }
        override fun onBleDeviceMtuSize(size: Int) {}
        override fun onBleDeviceTimeOut(timeLong: Int) {}
    }

    override fun setListener() {
        iv_back.setOnClickListener { finish() }
        iv_right.setOnClickListener {
            if (App.data.getCurrentFamily().Role == 1 && !deviceEntity!!.shareDevice) {
                jumpActivity(DeviceDetailsActivity::class.java)
            }
        }
    }

    /**
     * 显示设备离线弹框
     */
    private fun showOfflinePopup() {
        if (offlinePopup == null)
            offlinePopup = OfflinePopupWindow(this)
        offlinePopup?.onToHomeListener = object : OfflinePopupWindow.OnToHomeListener {
            override fun toHome(popupWindow: OfflinePopupWindow) {
                popupWindow.dismiss()
                finish()
            }

            override fun toFeedback(popupWindow: OfflinePopupWindow) {
                jumpActivity(FeedbackActivity::class.java, true)
            }
        }
        offlinePopup?.setBg(control_panel_bg)

        if (!this@ControlPanelActivity.isFinishing) {
            offlinePopup?.show(control_panel)
        }
    }

    /**
     *  获取列表对象
     */
    fun getDeviceProperty(position: Int): DevicePropertyEntity {
        presenter.model?.let {
            it.devicePropertyList.run {
                if (position >= size) {// 云端定时
                    val entity = DevicePropertyEntity()
                    entity.type = "btn-col-1"
                    return entity
                }
                return this[position]
            }
        }?:let {
            return DevicePropertyEntity()
        }
    }

    /**
     * 控制设备
     */
    fun controlDevice(id: String, value: String) {
        presenter.controlDevice(id, value)
    }

    override fun doAction(
            viewHolder: CRecyclerView.CViewHolder<*>, clickView: View, position: Int
    ) {
        deviceEntity?.let {
            if (it.online == 1)
                PanelThemeManager.instance.doAction(
                        viewHolder,
                        clickView,
                        position
                )
        }
    }

    override fun getViewHolder(parent: ViewGroup, viewType: Int): CRecyclerView.CViewHolder<*>? {
        return PanelThemeManager.instance.getViewHolder(parent, viewType)
    }

    override fun getViewType(position: Int): Int {
        presenter.model?.let {
            return PanelThemeManager.instance.getViewType(presenter.model!!.devicePropertyList[position])
        }?:let {
            return PanelThemeManager.instance.getViewType(DevicePropertyEntity())
        }
    }

    /**
     * 显示面板
     */
    override fun showControlPanel(navBar: NavBar?, timingProject: Boolean) {
        runOnUiThread {
            initTheme()
            PanelThemeManager.instance.showTheme(this, timingProject)
            showNavBar(navBar)
        }
    }

    override fun refreshDeviceStatus(isOnline: Boolean) {
        if (!isOnline) {
            job = CoroutineScope(Dispatchers.IO).launch {
                delay(200)
                CoroutineScope(Dispatchers.Main).launch {
                    showOfflinePopup()
                }
            }
        }
    }

    /**
     *  显示NavBar
     */
    private fun showNavBar(navBar: NavBar?) {
        navBar?.run {
            if (isShowNavBar()) {
                card_nav_bar.visibility = View.VISIBLE
                if (isShowTemplate()) {
                    ll_template.visibility = View.VISIBLE
                    presenter.model!!.getDevicePropertyForId(navBar.templateId)?.run {
                        if (isBoolType()) {
                            tv_template_name.text = name
                            iv_template.setOnClickListener {
                                when (id) {
                                    "power_switch" -> controlDevice(id, if (getValue() == "1") "0" else "1")
                                }
                            }
                        }
                    }
                } else {
                    ll_template.visibility = View.GONE
                }
                ll_timing_project.visibility = if (isShowTimingProject()) {
                    iv_timing_project.setOnClickListener { jumpToCloudTiming() }
                    View.VISIBLE
                } else {
                    View.GONE
                }
                card_nav_bar.background = getDrawable(R.drawable.control_simple_nav_bar_bg)
                tv_template_name.setTextColor(getMyColor(R.color.black_333333))
                tv_timing_project.setTextColor(getMyColor(R.color.black_333333))
                iv_template.setImageResource(R.mipmap.icon_nav_bar_simple_switch)
                iv_timing_project.setImageResource(R.mipmap.icon_nav_bar_simple_timer)
            } else {
                card_nav_bar.visibility = View.GONE
            }
        }
    }

    /**
     * 跳转到云端定时
     */
    fun jumpToCloudTiming() {
        put("property", presenter.model!!.devicePropertyList)
        jumpActivity(CloudTimingActivity::class.java)
    }

    /**
     * 切换主题背景
     */
    private fun initTheme() {
        StatusBarUtil.setStatusBarDarkTheme(this, true)
        iv_right.setImageResource(R.mipmap.icon_black_more)
        control_panel.setBackgroundColor(resources.getColor(R.color.white))
    }

    /**
     * 显示进度弹框
     */
    fun showNumberPopup(entity: DevicePropertyEntity) {
        if (numberPopup == null) {
            numberPopup = NumberPopupWindow(this)
        }
        numberPopup?.onUploadListener = object : NumberPopupWindow.OnUploadListener {
            override fun upload(progress: Int) {
                controlDevice(entity.id, progress.toString())
                numberPopup?.dismiss()
            }
        }
        numberPopup!!.showTitle(entity.name)
        val min = entity.numberEntity!!.min.toDouble().toInt()
        numberPopup!!.setRange(
                min,
                entity.numberEntity!!.max.toDouble().toInt()
        )
        val p = entity.getValue().toDouble().toInt()
        numberPopup!!.setProgress(if (p < min) min else p)
        numberPopup!!.setUnit(entity.numberEntity!!.unit)
        numberPopup?.setBg(control_panel_bg)
        numberPopup?.show(control_panel)
    }

    /**
     * 检查设备TRTC状态是否空闲
     */
    fun checkTRTCCallStatusIsBusy() : Boolean {
        if (!netWorkStateReceiver!!.isConnected(getApplicationContext())) {
            Toast.makeText(this, "网络异常请重试", Toast.LENGTH_LONG).show()
            return true;
        }
        var audioCallStatus = "0";
        var videoCallStatus = "0";
        presenter.model!!.devicePropertyList.forEach {
            if (it.id == MessageConst.TRTC_AUDIO_CALL_STATUS) {
                audioCallStatus = it.getValue()
            }
            if (it.id == MessageConst.TRTC_VIDEO_CALL_STATUS) {
                videoCallStatus = it.getValue()
            }
        }
        if (audioCallStatus != "0" || videoCallStatus != "0") { //表示设备不在空闲状态，提示用户 对方正忙...
            Toast.makeText(this, "对方正忙...", Toast.LENGTH_LONG).show()
            return true
        }
        return false
    }

    /**
     * 显示枚举弹框
     */
    fun showEnumPopup(entity: DevicePropertyEntity) {
        //特殊处理，当设备为trtc设备时。虽然call_status是枚举类型，但产品要求不弹弹窗，点击即拨打语音或视频通话。
        if (entity.id == MessageConst.TRTC_AUDIO_CALL_STATUS) {
            if (checkTRTCCallStatusIsBusy()) {
                return
            }
            controlDevice(entity.id, "1")
            TRTCUIManager.getInstance().setSessionManager(TRTCAppSessionManager())
            TRTCUIManager.getInstance().isCalling = true
            TRTCUIManager.getInstance().deviceId = TRTCUIManager.getInstance().callingDeviceId
            TRTCAudioCallActivity.startCallSomeone(this, RoomKey(), TRTCUIManager.getInstance().callingDeviceId)
            return
        } else if (entity.id == MessageConst.TRTC_VIDEO_CALL_STATUS) {
            if (checkTRTCCallStatusIsBusy()) {
                return
            }
            controlDevice(entity.id, "1")
            TRTCUIManager.getInstance().isCalling = true
            TRTCUIManager.getInstance().setSessionManager(TRTCAppSessionManager())
            TRTCUIManager.getInstance().deviceId = TRTCUIManager.getInstance().callingDeviceId
            TRTCVideoCallActivity.startCallSomeone(this, RoomKey(), TRTCUIManager.getInstance().callingDeviceId)
            return
        }
        if (enumPopup == null) {
            enumPopup = EnumPopupWindow(this)
        }
        enumPopup?.onUploadListener = object : EnumPopupWindow.OnUploadListener {
            override fun upload(value: String) {
                controlDevice(entity.id, value)
                enumPopup?.dismiss()
            }
        }
        enumPopup!!.showTitle(entity.name)
        enumPopup!!.selectKey = entity.getValue()
        enumPopup!!.setList(entity.enumEntity!!.mapping)
        enumPopup?.setBg(control_panel_bg)
        enumPopup?.show(control_panel)
    }

    override fun onBackPressed() {
        enumPopup?.run {
            if (isShowing) {
                dismiss()
                return
            }
        }
        numberPopup?.run {
            if (isShowing) {
                dismiss()
                return
            }
        }
        offlinePopup?.run {
            if (isShowing) {
                dismiss()
                return
            }
        }
        super.onBackPressed()
    }

    override fun onDestroy() {
        PanelThemeManager.instance.destroy()
        job?.cancel()
        cancel()
        BleConfigService.get().bluetoothGatt?.close()
        BleConfigService.get().stopScanBluetoothDevices()
        BleConfigService.get().bluetoothGatt = null
//        App.setEnableEnterRoomCallback(true)
        super.onDestroy()
    }
}
