package com.tencent.iot.explorer.link.demo.core.activity

import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.fastjson.JSON
import com.tencent.iot.explorer.link.core.auth.IoTAuth
import com.tencent.iot.explorer.link.core.auth.callback.ControlPanelCallback
import com.tencent.iot.explorer.link.core.auth.callback.MyCallback
import com.tencent.iot.explorer.link.core.auth.entity.ControlPanel
import com.tencent.iot.explorer.link.core.auth.entity.DeviceEntity
import com.tencent.iot.explorer.link.core.auth.entity.RoomEntity
import com.tencent.iot.explorer.link.core.auth.message.MessageConst
import com.tencent.iot.explorer.link.core.auth.message.MessageConst.TRTC_AUDIO_CALL_STATUS
import com.tencent.iot.explorer.link.core.auth.message.MessageConst.TRTC_VIDEO_CALL_STATUS
import com.tencent.iot.explorer.link.core.auth.message.payload.Payload
import com.tencent.iot.explorer.link.core.auth.message.upload.ArrayString
import com.tencent.iot.explorer.link.core.auth.response.BaseResponse
import com.tencent.iot.explorer.link.core.auth.socket.callback.ActivePushCallback
import com.tencent.iot.explorer.link.core.auth.util.JsonManager
import com.tencent.iot.explorer.link.demo.App
import com.tencent.iot.explorer.link.core.demo.R
import com.tencent.iot.explorer.link.demo.TRTCSdkDemoSessionManager
import com.tencent.iot.explorer.link.demo.core.adapter.ControlPanelAdapter
import com.tencent.iot.explorer.link.demo.core.adapter.OnItemListener
import com.tencent.iot.explorer.link.demo.core.holder.BaseHolder
import com.tencent.iot.explorer.link.demo.core.holder.ControlPanelHolder
import com.tencent.iot.explorer.link.demo.common.log.L
import com.tencent.iot.explorer.link.demo.core.popup.CommonPopupWindow
import com.tencent.iot.explorer.link.demo.core.popup.EditPopupWindow
import com.tencent.iot.explorer.link.demo.core.popup.EnumPopupWindow
import com.tencent.iot.explorer.link.demo.core.popup.NumberPopupWindow
import com.tencent.iot.explorer.link.demo.common.customView.MyDivider
import com.tencent.iot.explorer.link.rtc.model.RoomKey
import com.tencent.iot.explorer.link.rtc.model.TRTCUIManager
import com.tencent.iot.explorer.link.rtc.ui.audiocall.TRTCAudioCallActivity
import com.tencent.iot.explorer.link.rtc.ui.videocall.TRTCVideoCallActivity
import kotlinx.android.synthetic.main.activity_control_panel.*
import kotlinx.android.synthetic.main.menu_back_layout.*

/**
 * 控制面板
 */
class ControlPanelActivity : BaseActivity(), ControlPanelCallback, ActivePushCallback {

    private var device: DeviceEntity? = null

    //是否是共享设备
    private var hasShare = false
    private val panelList = arrayListOf<ControlPanel>()

    private lateinit var adapter: ControlPanelAdapter
    private var numberPopup: NumberPopupWindow? = null
    private var enumPopup: EnumPopupWindow? = null
    private var editPopupWindow: EditPopupWindow? = null
    private var commonPopupWindow: CommonPopupWindow? = null

    override fun getContentView(): Int {
        return R.layout.activity_control_panel
    }

    override fun initView() {
        device = get("device")
        hasShare = get("share") ?: false
        tv_title.text = device?.getAlias() ?: "控制面板"
        device?.run {
            //注册设备订阅
            IoTAuth.registerActivePush(ArrayString(DeviceId), null)
            //添加设备监听回调
            IoTAuth.addActivePushCallback(this@ControlPanelActivity)
            //获取面板数据
            IoTAuth.deviceImpl.controlPanel(ProductId, DeviceName, this@ControlPanelActivity)
        }
        rv_control_panel.layoutManager = LinearLayoutManager(this)
        adapter = ControlPanelAdapter(this, panelList)
        rv_control_panel.adapter = adapter
        rv_control_panel.addItemDecoration(MyDivider(dp2px(16), dp2px(16), dp2px(20)))
    }

    override fun setListener() {
        iv_back.setOnClickListener { finish() }
        tv_delete_device.setOnClickListener {
            if (!hasShare)
                showDeletePopup()
            else show("共享设备无法删除")
        }
        adapter.setOnItemListener(object : OnItemListener {
            override fun onItemClick(holder: BaseHolder<*>, clickView: View, position: Int) {
                (holder as? ControlPanelHolder)?.data?.run {
                    when {
                        id == "tp" -> jumpActivity(TimingProjectActivity::class.java)
                        id == "alias" -> {
                            if (!hasShare)
                                showEditPopup(panelList[position])
                            else show("共享设备无法修改")
                        }
                        id == "room" -> {
                            if (get<RoomEntity>("select_room") == null)
                                put("select_room", App.data.getCurrentRoom())
                            jumpActivity(SelectRoomActivity::class.java)
                        }
                        id == "share" -> {
                            jumpActivity(ShareUserListActivity::class.java)
                        }
                        isBoolType() or isEnumType() -> {
                            showEnumPopup(this)
                        }
                        isNumberType() -> {
                            showNumberPopup(this)
                        }
                    }
                }
            }
        })
    }

    /**
     * 显示云端定时,添加设备详情
     */
    private fun showTimingProject() {
        IoTAuth.deviceImpl.panelConfig()?.Panel?.standard?.run {
            if (timingProject) {
                val tp = ControlPanel()
                tp.name = "云端定时"
                tp.id = "tp"
                panelList.add(tp)
            }
        }
        val alias = ControlPanel()
        alias.name = "设备别名"
        alias.id = "alias"
        alias.value = device?.getAlias() ?: ""
        panelList.add(alias)
        if (!hasShare) {
            val room = ControlPanel()
            room.name = "更换房间"
            room.id = "room"
            panelList.add(room)
            val share = ControlPanel()
            share.name = "设备分享"
            share.id = "share"
            panelList.add(share)
        }
    }

    /**
     * 显示nav bar和主题
     */
    private fun showNavBarAndTheme() {
        IoTAuth.deviceImpl.panelConfig()?.Panel?.standard?.run {
            navBar.run {
                //显示nav bar
                card_nav_bar.visibility = if (isShowNavBar()) View.VISIBLE else View.GONE
                //显示template
                ll_template.visibility = if (isShowTemplate()) View.VISIBLE else View.GONE
                tv_template_name.text =
                        IoTAuth.deviceImpl.product()?.myTemplate?.getTemplateName(templateId) ?: ""
                iv_timing_project.setOnClickListener {
                    jumpActivity(TimingProjectActivity::class.java)
                }
                panelList.forEach {
                    if (it.id == templateId) {
                        val panel = it
                        iv_template.setOnClickListener {
                            when {
                                panel.isBoolType() or panel.isEnumType() -> {
                                    showEnumPopup(panel)
                                }
                                panel.isNumberType() -> {
                                    showNumberPopup(panel)
                                }
                            }
                        }
                    }
                }
            }
            tv_panel_theme.text = "$theme"
        }
    }

    /**
     * 控制设备
     */
    private fun controlDevice(id: String, value: String) {
        L.d("上报数据:id=$id value=$value")
        var data = "{\"$id\":\"$value\"}"
        if (id == TRTC_VIDEO_CALL_STATUS || id == TRTC_AUDIO_CALL_STATUS) { //如果点击选择的是trtc设备的呼叫状态
            if (value == "1") { //并且状态值为1，代表应用正在call设备
                App.data.callingDeviceId = "${device?.ProductId}/${device?.DeviceName}" //保存下设备id（productId/deviceName）
                val userId = App.data.userInfo.UserID
                data = "{\"$id\":$value, \"${MessageConst.USERID}\":\"$userId\"}"
            }
        }
        device?.let {
            IoTAuth.deviceImpl.controlDevice(it.ProductId, it.DeviceName, data,
                    object : MyCallback {
                        override fun fail(msg: String?, reqCode: Int) {
                            L.e(msg ?: "")
                        }

                        override fun success(response: BaseResponse, reqCode: Int) {
                            L.e(response.msg)
                        }
                    })
        }
    }

    /**
     * 提交aliasName
     */
    private fun commitAlias(entity: ControlPanel) {
        if (TextUtils.isEmpty(entity.value)) return
        device?.let {
            IoTAuth.deviceImpl.modifyDeviceAlias(it.ProductId, it.getAlias(), entity.value,
                    object : MyCallback {
                        override fun fail(msg: String?, reqCode: Int) {
                            L.e(msg ?: "")
                        }

                        override fun success(response: BaseResponse, reqCode: Int) {
                            if (response.isSuccess()) {
                                device?.AliasName = entity.value
                                refresh()
                            } else {
                                show(response.msg)
                            }
                            editPopupWindow?.dismiss()
                        }
                    }
            )
        }
    }

    /**
     * 删除设备
     */
    private fun deleteDevice() {
        device?.run {
            IoTAuth.deviceImpl.deleteDevice(
                    App.data.getCurrentFamily().FamilyId, ProductId, DeviceName,
                    object : MyCallback {
                        override fun fail(msg: String?, reqCode: Int) {
                            show(msg)
                        }

                        override fun success(response: BaseResponse, reqCode: Int) {
                            if (response.isSuccess()) {
                                finish()
                            } else {
                                show(response.msg)
                            }
                        }
                    }
            )
        }
    }

    /**
     * 显示编辑框
     */
    private fun showEditPopup(entity: ControlPanel) {
        if (editPopupWindow == null) {
            editPopupWindow = EditPopupWindow(this)
        }
        editPopupWindow!!.setShowData(
                getString(R.string.timing_name), entity.value
        )
        editPopupWindow!!.onVerifyListener = object : EditPopupWindow.OnVerifyListener {
            override fun onVerify(text: String) {
                if (text.isNotEmpty()) {
                    entity.value = text
                }
                editPopupWindow?.dismiss()
                commitAlias(entity)
            }
        }
        editPopupWindow!!.setBg(control_panel_bg)
        editPopupWindow!!.show(control_panel)
    }

    private fun showDeletePopup() {
        if (commonPopupWindow == null) {
            commonPopupWindow = CommonPopupWindow(this)
        }
        commonPopupWindow?.setCommonParams(
                getString(R.string.delete_toast_title),
                getString(R.string.delete_toast_content)
        )
        commonPopupWindow?.setMenuText("", getString(R.string.delete))
        commonPopupWindow?.setBg(control_panel_bg)
        commonPopupWindow?.show(control_panel)
        commonPopupWindow?.onKeyListener = object : CommonPopupWindow.OnKeyListener {
            override fun confirm(popupWindow: CommonPopupWindow) {
                popupWindow.dismiss()
                deleteDevice()
            }

            override fun cancel(popupWindow: CommonPopupWindow) {
                popupWindow.dismiss()
            }
        }
    }

    /**
     * 显示进度弹框
     */
    fun showNumberPopup(entity: ControlPanel) {
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
        if (entity.value.isNotEmpty()) {
            numberPopup!!.setProgress(entity.value.toDouble().toInt())
        } else {
            numberPopup!!.setProgress(0)
        }
        numberPopup?.setBg(control_panel_bg)
        numberPopup?.show(control_panel)
    }

    /**
     * 检查设备TRTC状态是否空闲
     */
    fun checkTRTCCallStatusIsBusy() : Boolean {
        var audioCallStatus = "0";
        var videoCallStatus = "0";
        panelList.forEach {
            if (it.id == TRTC_AUDIO_CALL_STATUS) {
                audioCallStatus = it.value
            }
            if (it.id == TRTC_VIDEO_CALL_STATUS) {
                videoCallStatus = it.value
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
    fun showEnumPopup(entity: ControlPanel) {
        //特殊处理，当设备为trtc设备时。虽然call_status是枚举类型，但产品要求不弹弹窗，点击即拨打语音或视频通话。
        if (entity.id == MessageConst.TRTC_AUDIO_CALL_STATUS) {
            if (checkTRTCCallStatusIsBusy()) {
                return
            }
            controlDevice(entity.id, "1")
            TRTCUIManager.getInstance().setSessionManager(TRTCSdkDemoSessionManager())
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
            TRTCUIManager.getInstance().setSessionManager(TRTCSdkDemoSessionManager())
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
        enumPopup!!.selectValue = entity.value
        enumPopup!!.setList(entity.define)
        enumPopup?.setBg(control_panel_bg)
        enumPopup?.show(control_panel)
    }

    /**
     * SDK内部长连接重新连接上时
     */
    override fun reconnected() {
        //为了确保设备订阅不断开，可重新订阅
        //注册设备监听
        device?.run {
            IoTAuth.registerActivePush(ArrayString(DeviceId), null)
        }
    }

    /**
     * 设备监听成功回调（内部解析成功）
     */
    override fun success(payload: Payload) {
        L.e("Payoad", payload.toString())
        JSON.parseObject(payload.data).run {
            keys.forEachIndexed { _, id ->
                run setData@{
                    panelList.forEach {
                        if (id == it.id) {
                            it.value = getString(id)
                            refresh()
                            return@setData
                        }
                    }
                }
            }
        }
    }

    /**
     * 设备监听成功回调（内部解析失败）
     */
    override fun unknown(json: String, errorMessage: String) {
        L.e("unknown-json", json)
        L.e("unknown-errorMessage", errorMessage)
    }

    /**
     * 面板数据返回成功
     */
    override fun success(panelList: List<ControlPanel>) {
        L.d("面板数据返回成功", JsonManager.toJson(panelList))
        runOnUiThread {
            this.panelList.addAll(panelList)
            showTimingProject()
            showNavBarAndTheme()
            refresh()
        }
    }

    /**
     * 面板数据更新时
     */
    override fun refresh() {
        L.e("面板数据更新时 refresh()")
        runOnUiThread {
            adapter.notifyDataSetChanged()
        }
    }

    override fun fail(msg: String) {
        L.d(TAG, msg)
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
        super.onBackPressed()
    }

    override fun onDestroy() {
        enumPopup?.dismiss()
        numberPopup?.dismiss()
        //清空面板数据
        IoTAuth.deviceImpl.clearData()
        App.data.callingDeviceId = "" //暂时打电话的入口只在控制面板内，所以销毁了控制面板，就重置一下callingDeviceId为空字符串，代表没有在打电话了。
        //移除当前监听回调
        device?.run {
            IoTAuth.removeActivePushCallback(ArrayString(DeviceId), this@ControlPanelActivity)
        }
        super.onDestroy()
    }
}
