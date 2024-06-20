package com.tencent.iot.explorer.link.kitlink.activity

import android.app.Activity
import android.bluetooth.BluetoothGatt
import android.content.Intent
import android.text.TextUtils
import android.util.Log
import com.alibaba.fastjson.JSON
import com.tencent.iot.explorer.link.App
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.T
import com.tencent.iot.explorer.link.core.auth.callback.MyCallback
import com.tencent.iot.explorer.link.core.auth.entity.DeviceEntity
import com.tencent.iot.explorer.link.core.auth.entity.RoomEntity
import com.tencent.iot.explorer.link.core.auth.response.BaseResponse
import com.tencent.iot.explorer.link.core.link.entity.*
import com.tencent.iot.explorer.link.core.link.exception.TCLinkException
import com.tencent.iot.explorer.link.core.link.listener.BleDeviceConnectionListener
import com.tencent.iot.explorer.link.core.link.service.BleConfigService
import com.tencent.iot.explorer.link.core.log.L
import com.tencent.iot.explorer.link.kitlink.consts.CommonField
import com.tencent.iot.explorer.link.kitlink.entity.BleConfig
import com.tencent.iot.explorer.link.kitlink.entity.EditNameValue
import com.tencent.iot.explorer.link.kitlink.entity.ProductEntity
import com.tencent.iot.explorer.link.kitlink.entity.ProductsEntity
import com.tencent.iot.explorer.link.kitlink.popup.CommonPopupWindow
import com.tencent.iot.explorer.link.kitlink.popup.EditPopupWindow
import com.tencent.iot.explorer.link.kitlink.util.HttpRequest
import com.tencent.iot.explorer.link.kitlink.util.Utils
import com.tencent.iot.explorer.link.kitlink.util.safe
import com.tencent.iot.explorer.link.mvp.IPresenter
import com.tencent.iot.explorer.link.mvp.presenter.DeviceDetailPresenter
import com.tencent.iot.explorer.link.mvp.view.DeviceDetailView
import kotlinx.android.synthetic.main.activity_device_details.*
import kotlinx.android.synthetic.main.menu_back_layout.*
import kotlinx.coroutines.*

class DeviceDetailsActivity : PActivity(), CoroutineScope by MainScope(), DeviceDetailView {

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
        BleConfigService.get().bluetoothGatt?.let {
            BleConfigService.get().sendUnbindResult(it, true)
        }
        App.data.refresh = true
        App.data.setRefreshLevel(2)
        Utils.sendRefreshBroadcast(this@DeviceDetailsActivity)
        backToMain()
    }

    override fun fail(message: String) {
        BleConfigService.get().bluetoothGatt?.let {
            BleConfigService.get().sendUnbindResult(it, false)
        }
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
                    getDeviceType(ProductId, object: OnTypeGeted {
                        override fun onType(type: String) {
                            if (type == "ble") {
                                BleConfigService.get()?.let {
                                    BleConfigService.get().connetionListener = bleDeviceConnectionListener
                                    if (BleConfigService.get().unbind(it.bluetoothGatt)) {
                                        T.show(getString(R.string.delete_success))
                                    }
                                }
                            } else {
                                deleteDevice()
                            }
                        }
                    })
                }
            }

            override fun cancel(popupWindow: CommonPopupWindow) {
                popupWindow.dismiss()
            }
        }
    }

    private fun deleteDevice() {
        launch (Dispatchers.Main) {
            deviceEntity?.run {
                presenter.deleteDevice(ProductId, DeviceName)
            }
            Utils.sendRefreshBroadcast(this@DeviceDetailsActivity)
        }
    }

    private fun showEditPopup() {
        editPopupWindow = EditPopupWindow(this)
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

    private var bleDeviceConnectionListener = object: BleDeviceConnectionListener {
        override fun onBleDeviceFounded(bleDevice: BleDevice) {}
        override fun onBleDeviceConnected() {}
        override fun onBleDeviceDisconnected(exception: TCLinkException) {}
        override fun onBleDeviceInfo(bleDeviceInfo: BleDeviceInfo) {}
        override fun onBleSetWifiModeResult(success: Boolean) {}
        override fun onBleSendWifiInfoResult(success: Boolean) {}
        override fun onBleWifiConnectedInfo(wifiConnectInfo: BleWifiConnectInfo) {}
        override fun onBlePushTokenResult(success: Boolean) {}
        override fun onMtuChanged(mtu: Int, status: Int) {}
        override fun onBleBindSignInfo(bleDevBindCondition: BleDevBindCondition) {}
        override fun onBleSendSignInfo(bleDevSignResult: BleDevSignResult) {}
        override fun onBleUnbindSignInfo(signInfo: String) {
            deleteDevice()
        }
        override fun onBlePropertyValue(bleDeviceProperty: BleDeviceProperty) {}
        override fun onBleControlPropertyResult(result: Int) {}
        override fun onBleRequestCurrentProperty() {}
        override fun onBleNeedPushProperty(eventId: Int, bleDeviceProperty: BleDeviceProperty) {}
        override fun onBleReportActionResult(reason: Int, actionId: Int, bleDeviceProperty: BleDeviceProperty) {}
        override fun onBleDeviceFirmwareVersion(firmwareVersion: BleDeviceFirmwareVersion) {}
        override fun onBleDevOtaUpdateResponse(otaUpdateResponse: BleDevOtaUpdateResponse) {}
        override fun onBleDevOtaUpdateResult(success: Boolean, errorCode: Int) {}

        override fun onBleDevOtaReceivedProgressResponse(progress: Int) {}

        override fun onBleDeviceMtuSize(size: Int) {}
        override fun onBleDeviceTimeOut(timeLong: Int) {}
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CommonField.EDIT_NAME_REQ_CODE &&
            resultCode == Activity.RESULT_OK && data != null) {
            val extraInfo = data.getStringExtra(CommonField.EXTRA_TEXT).safe()
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
        cancel()
        super.onDestroy()
    }

}
