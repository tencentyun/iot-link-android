package com.tencent.iot.explorer.link.core.demo.activity

import android.text.TextUtils
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.fastjson.JSON
import com.tencent.iot.explorer.link.core.auth.IoTAuth
import com.tencent.iot.explorer.link.core.auth.callback.ControlPanelCallback
import com.tencent.iot.explorer.link.core.auth.callback.MyCallback
import com.tencent.iot.explorer.link.core.auth.entity.ControlPanel
import com.tencent.iot.explorer.link.core.auth.entity.Device
import com.tencent.iot.explorer.link.core.auth.entity.Room
import com.tencent.iot.explorer.link.core.auth.message.payload.Payload
import com.tencent.iot.explorer.link.core.auth.message.upload.ArrayString
import com.tencent.iot.explorer.link.core.auth.response.BaseResponse
import com.tencent.iot.explorer.link.core.auth.socket.callback.ActivePushCallback
import com.tencent.iot.explorer.link.core.auth.util.JsonManager
import com.tencent.iot.explorer.link.core.demo.App
import com.tencent.iot.explorer.link.core.demo.R
import com.tencent.iot.explorer.link.core.demo.adapter.ControlPanelAdapter
import com.tencent.iot.explorer.link.core.demo.adapter.OnItemListener
import com.tencent.iot.explorer.link.core.demo.holder.BaseHolder
import com.tencent.iot.explorer.link.core.demo.holder.ControlPanelHolder
import com.tencent.iot.explorer.link.core.demo.log.L
import com.tencent.iot.explorer.link.core.demo.popup.CommonPopupWindow
import com.tencent.iot.explorer.link.core.demo.popup.EditPopupWindow
import com.tencent.iot.explorer.link.core.demo.popup.EnumPopupWindow
import com.tencent.iot.explorer.link.core.demo.popup.NumberPopupWindow
import com.tencent.iot.explorer.link.core.demo.view.MyDivider
import kotlinx.android.synthetic.main.activity_control_panel.*
import kotlinx.android.synthetic.main.menu_back_layout.*

/**
 * 控制面板
 */
class ControlPanelActivity : BaseActivity(), ControlPanelCallback, ActivePushCallback {

    private var device: Device? = null

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
                            if (get<Room>("select_room") == null)
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
        val data = "{\"$id\":\"$value\"}"
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
        numberPopup!!.setProgress(entity.value.toDouble().toInt())
        numberPopup?.setBg(control_panel_bg)
        numberPopup?.show(control_panel)
    }

    /**
     * 显示枚举弹框
     */
    fun showEnumPopup(entity: ControlPanel) {
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
        //移除当前监听回调
        device?.run {
            IoTAuth.removeActivePushCallback(ArrayString(DeviceId), this@ControlPanelActivity)
        }
        super.onDestroy()
    }
}
