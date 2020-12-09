package com.tencent.iot.explorer.link.kitlink.activity

import android.app.Activity
import android.content.Intent
import android.text.TextUtils
import android.util.Log
import com.alibaba.fastjson.JSON
import com.tencent.iot.explorer.link.App
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.core.log.L
import com.tencent.iot.explorer.link.kitlink.popup.CommonPopupWindow
import com.tencent.iot.explorer.link.kitlink.popup.EditPopupWindow
import com.tencent.iot.explorer.link.kitlink.util.HttpRequest
import com.tencent.iot.explorer.link.core.auth.callback.MyCallback
import com.tencent.iot.explorer.link.mvp.IPresenter
import com.tencent.iot.explorer.link.mvp.presenter.DeviceDetailPresenter
import com.tencent.iot.explorer.link.mvp.view.DeviceDetailView
import com.tencent.iot.explorer.link.T
import com.tencent.iot.explorer.link.core.auth.entity.DeviceEntity
import com.tencent.iot.explorer.link.core.auth.entity.RoomEntity
import com.tencent.iot.explorer.link.core.auth.response.BaseResponse
import com.tencent.iot.explorer.link.kitlink.consts.CommonField
import com.tencent.iot.explorer.link.kitlink.entity.EditNameValue
import com.tencent.iot.explorer.link.kitlink.entity.ManualTask
import com.tencent.iot.explorer.link.kitlink.util.Utils
import kotlinx.android.synthetic.main.activity_device_details.*
import kotlinx.android.synthetic.main.menu_back_layout.*

class DeviceDetailsActivity : PActivity(), DeviceDetailView {

    private var deviceEntity: DeviceEntity? = null

    private lateinit var presenter: DeviceDetailPresenter
    private var commonPopupWindow: CommonPopupWindow? = null
    private var editPopupWindow: EditPopupWindow? = null

    override fun getContentView(): Int {
        return R.layout.activity_device_details
    }

    override fun getPresenter(): IPresenter? {
        return presenter
    }

    override fun initView() {
        presenter = DeviceDetailPresenter(this)
        deviceEntity = get("device")
        tv_title.text = getString(R.string.device_details)
        deviceEntity?.run {
            tv_device_alias_name.text = AliasName
        }
    }

    override fun setListener() {
        iv_back.setOnClickListener {
            finish()
        }
        tv_device_alias_title.setOnClickListener {
            showEditPopup()
        }
        tv_device_info_title.setOnClickListener {
            jumpActivity(DeviceInfoActivity::class.java)
        }
        tv_device_room_setting.setOnClickListener {
            if (get<RoomEntity>("select_room") == null)
                put("select_room", App.data.getCurrentRoom())
            jumpActivity(SelectRoomActivity::class.java)
        }
        tv_device_share.setOnClickListener {
            jumpActivity(ShareUserListActivity::class.java)
        }
        tv_device_delete.setOnClickListener { showPopup() }
    }

    override fun deleteSuccess() {
        App.data.setRefreshLevel(2)
        backToMain()
    }

    override fun fail(message: String) {
        T.show(message)
    }

    private fun showPopup() {
        if (commonPopupWindow == null) {
            commonPopupWindow = CommonPopupWindow(this)
        }
        commonPopupWindow?.setCommonParams(
            getString(R.string.delete_toast_title),
            getString(R.string.delete_toast_content)
        )
        commonPopupWindow?.setMenuText("", getString(R.string.delete))
        commonPopupWindow?.setBg(device_detail_bg)
        commonPopupWindow?.show(device_detail)
        commonPopupWindow?.onKeyListener = object : CommonPopupWindow.OnKeyListener {
            override fun confirm(popupWindow: CommonPopupWindow) {
                popupWindow.dismiss()
                deviceEntity?.run {
                    presenter.deleteDevice(ProductId, DeviceName)
                }
                Utils.sendRefreshBroadcast(this@DeviceDetailsActivity)
            }

            override fun cancel(popupWindow: CommonPopupWindow) {
                popupWindow.dismiss()
            }
        }
    }

    private fun showEditPopup() {
//        if (editPopupWindow == null) {
            editPopupWindow = EditPopupWindow(this)
//        }
//        deviceEntity?.run {
//            editPopupWindow?.setShowData(getString(R.string.device_name), AliasName)
//        }
//        editPopupWindow?.setBg(device_detail_bg)
//        editPopupWindow?.show(device_detail)
//        editPopupWindow?.onVerifyListener = object : EditPopupWindow.OnVerifyListener {
//            override fun onVerify(text: String) {
//                commitAlias(text)
//            }
//        }
        var intent = Intent(this@DeviceDetailsActivity, EditNameActivity::class.java)
        var editNameValue = EditNameValue()
        editNameValue.name = deviceEntity!!.getAlias()
        editNameValue.title = getString(R.string.device_name)
        editNameValue.tipName = getString(R.string.device_name)
        editNameValue.btn = getString(R.string.save)
        editNameValue.errorTip = getString(R.string.toast_name_length)
        intent.putExtra(CommonField.EXTRA_INFO, JSON.toJSONString(editNameValue))
        startActivityForResult(intent, CommonField.EDIT_NAME_REQ_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CommonField.EDIT_NAME_REQ_CODE &&
            resultCode == Activity.RESULT_OK && data != null) {
            var extraInfo = data?.getStringExtra(CommonField.EXTRA_TEXT)
            commitAlias(extraInfo)
        }
    }

    /**
     * 提交aliasName
     */
    private fun commitAlias(aliasName: String) {
        if (TextUtils.isEmpty(aliasName)) return
        deviceEntity?.let {
            HttpRequest.instance.modifyDeviceAliasName(it.ProductId, it.DeviceName, aliasName,
                object : MyCallback {
                    override fun fail(msg: String?, reqCode: Int) {
                        L.e(msg ?: "")
                    }

                    override fun success(response: BaseResponse, reqCode: Int) {
                        if (response.isSuccess()) {
                            deviceEntity?.AliasName = aliasName
                            tv_device_alias_name.text = aliasName
                        } else {
                            if (!TextUtils.isEmpty(response.msg))
                                T.show(response.msg)
                        }
                        editPopupWindow?.dismiss()
                    }
                }
            )
        }
    }


    override fun onBackPressed() {
        commonPopupWindow?.run {
            if (isShowing) {
                dismiss()
                return
            }
        }
        editPopupWindow?.run {
            if (isShowing) {
                dismiss()
                return
            }
        }
        super.onBackPressed()
    }

    override fun onDestroy() {
        commonPopupWindow?.run {
            if (isShowing) {
                dismiss()
            }
        }
        editPopupWindow?.run {
            if (isShowing) {
                dismiss()
            }
        }
        super.onDestroy()
    }

}
