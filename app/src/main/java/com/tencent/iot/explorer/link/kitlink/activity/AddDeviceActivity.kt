package com.tencent.iot.explorer.link.kitlink.activity

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.text.TextUtils
import com.example.qrcode.Constant
import com.example.qrcode.ScannerActivity
import com.tencent.iot.explorer.link.App
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.core.link.entity.DeviceInfo
import com.tencent.iot.explorer.link.core.log.L
import com.tencent.iot.explorer.link.kitlink.consts.CommonField
import com.tencent.iot.explorer.link.kitlink.response.BaseResponse
import com.tencent.iot.explorer.link.kitlink.util.HttpRequest
import com.tencent.iot.explorer.link.kitlink.util.MyCallback
import com.tencent.iot.explorer.link.mvp.IPresenter
import com.tencent.iot.explorer.link.util.T
import kotlinx.android.synthetic.main.activity_add_device.*
import kotlinx.android.synthetic.main.menu_back_layout.*

/**
 * 添加设备界面
 */
class AddDeviceActivity : PActivity(), MyCallback {

    private var permissions = arrayOf(
        Manifest.permission.ACCESS_WIFI_STATE,
        Manifest.permission.CHANGE_WIFI_STATE,
        Manifest.permission.CHANGE_WIFI_MULTICAST_STATE,
        Manifest.permission.CAMERA,
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    private var type = 0

    override fun getContentView(): Int {
        return R.layout.activity_add_device
    }

    override fun initView() {
        tv_title.text = getString(R.string.add_device)
        iv_back.setColorFilter(resources.getColor(R.color.black_333333))
    }

    override fun permissionAllGranted() {
        jump()
    }

    override fun permissionDenied(permission: String) {
        requestPermission(arrayOf(permission))
    }

    private fun request(type: Int) {
        this.type = type
        if (checkPermissions(permissions)) {
            jump()
        } else {
            requestPermission(permissions)
        }
    }

    private fun jump() {
        when (type) {
            0 -> {
                var intent = Intent(this, ScannerActivity::class.java)
                intent.putExtra(Constant.EXTRA_IS_ENABLE_SCAN_FROM_PIC,true)
                startActivityForResult(intent, CommonField.QR_CODE_REQUEST_CODE)
            }
            1 -> {
                jumpActivity(SmartConnectActivity::class.java)
            }
            2 -> {
                jumpActivity(SoftApActivity::class.java)
            }
        }
    }

    override fun setListener() {
        iv_back.setOnClickListener { finish() }
        tv_add_device_scan_qrcode.setOnClickListener {
            request(0)
        }
        tv_add_device_smart_config.setOnClickListener {
            request(1)
        }
        tv_add_device_soft_ap.setOnClickListener {
            request(2)
        }
    }

    override fun getPresenter(): IPresenter? {
        return null
    }

    /**
     * 绑定虚拟设备
     */
    private fun bindDevice(signature: String) {
        HttpRequest.instance.scanBindDevice(App.data.getCurrentFamily().FamilyId, App.data.getCurrentRoom().RoomId, signature, this)
    }

    override fun fail(msg: String?, reqCode: Int) {
        L.e(msg ?: "")
    }

    override fun success(response: BaseResponse, reqCode: Int) {
        runOnUiThread {
            if (response.isSuccess()) {
                T.show(getString(R.string.add_sucess)) //添加成功
                App.data.setRefreshLevel(2)
                finish()
            } else {
                T.show(response.msg)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        data?.let {
            if (resultCode == Activity.RESULT_OK) {
                val type = it.getStringExtra(Constant.EXTRA_RESULT_CODE_TYPE)
                it.getStringExtra(Constant.EXTRA_RESULT_CONTENT)?.run {
                    L.d("type=$type,content=$this")
                    when {
                        contains("signature=") -> {//虚拟设备
                            bindDevice(this.substringAfterLast("signature="))
                        }
                        contains("\"DeviceName\"") and contains("\"Signature\"") -> {//真实设备
                            val deviceInfo = DeviceInfo(this)
                            if (!TextUtils.isEmpty(deviceInfo.productId)) {
//                                wifiBindDevice(deviceInfo)
                                bindDevice(deviceInfo.signature)
                            }
                        }
                        else -> {//之前旧版本虚拟设备二维码只有签名
                            bindDevice(this)
                        }
                    }
                }
            }
        }
    }

}
