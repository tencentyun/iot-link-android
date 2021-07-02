package com.tencent.iot.explorer.link.kitlink.activity

import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.tencent.iot.explorer.link.App
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.TRTCAppSessionManager
import com.tencent.iot.explorer.link.core.auth.entity.DeviceEntity
import com.tencent.iot.explorer.link.kitlink.entity.DevicePropertyEntity
import com.tencent.iot.explorer.link.core.auth.entity.NavBar
import com.tencent.iot.explorer.link.core.auth.message.MessageConst
import com.tencent.iot.explorer.link.kitlink.popup.EnumPopupWindow
import com.tencent.iot.explorer.link.kitlink.popup.NumberPopupWindow
import com.tencent.iot.explorer.link.kitlink.theme.PanelThemeManager
import com.tencent.iot.explorer.link.kitlink.util.StatusBarUtil
import com.tencent.iot.explorer.link.mvp.IPresenter
import com.tencent.iot.explorer.link.mvp.presenter.ControlPanelPresenter
import com.tencent.iot.explorer.link.mvp.view.ControlPanelView
import com.tencent.iot.explorer.link.customview.recyclerview.CRecyclerView
import com.tencent.iot.explorer.link.kitlink.popup.OfflinePopupWindow
import com.tencent.iot.explorer.link.rtc.model.RoomKey
import com.tencent.iot.explorer.link.rtc.model.TRTCUIManager
import com.tencent.iot.explorer.link.rtc.ui.audiocall.TRTCAudioCallActivity
import com.tencent.iot.explorer.link.rtc.ui.videocall.TRTCVideoCallActivity
import kotlinx.android.synthetic.main.activity_control_panel.*
import kotlinx.android.synthetic.main.menu_back_and_right.*
import kotlinx.android.synthetic.main.menu_back_layout.*
import kotlinx.coroutines.*

/**
 * 控制面板
 */
class ControlPanelActivity : PActivity(), ControlPanelView, CRecyclerView.RecyclerItemView {

    private var deviceEntity: DeviceEntity? = null

    private lateinit var presenter: ControlPanelPresenter

    //    private var aliasName = ""
    private var numberPopup: NumberPopupWindow? = null
    private var enumPopup: EnumPopupWindow? = null
    private var offlinePopup: OfflinePopupWindow? = null
    private var job: Job? = null

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
    }

    override fun initView() {
//        App.setEnableEnterRoomCallback(false)
        presenter = ControlPanelPresenter(this)
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
            numberPopup?.onUploadListener = object : NumberPopupWindow.OnUploadListener {
                override fun upload(progress: Int) {
                    controlDevice(entity.id, progress.toString())
                    numberPopup?.dismiss()
                }
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
            TRTCUIManager.getInstance().deviceId = App.data.callingDeviceId
            TRTCAudioCallActivity.startCallSomeone(this, RoomKey(), App.data.callingDeviceId)
            return
        } else if (entity.id == MessageConst.TRTC_VIDEO_CALL_STATUS) {
            if (checkTRTCCallStatusIsBusy()) {
                return
            }
            controlDevice(entity.id, "1")
            TRTCUIManager.getInstance().isCalling = true
            TRTCUIManager.getInstance().setSessionManager(TRTCAppSessionManager())
            TRTCUIManager.getInstance().deviceId = App.data.callingDeviceId
            TRTCVideoCallActivity.startCallSomeone(this, RoomKey(), App.data.callingDeviceId)
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
//        App.setEnableEnterRoomCallback(true)
        super.onDestroy()
    }
}
