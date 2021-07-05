package com.tencent.iot.explorer.link.demo.core.activity

import android.Manifest
import android.text.TextUtils
import com.tencent.iot.explorer.link.core.auth.IoTAuth
import com.tencent.iot.explorer.link.core.auth.callback.MyCallback
import com.tencent.iot.explorer.link.core.auth.response.BaseResponse
import com.tencent.iot.explorer.link.core.auth.response.BindDeviceTokenResponse
import com.tencent.iot.explorer.link.demo.App
import com.tencent.iot.explorer.link.core.demo.R
import com.tencent.iot.explorer.link.demo.core.response.UserInfoResponse
import kotlinx.android.synthetic.main.activity_add_device.*
import kotlinx.android.synthetic.main.menu_back_layout.*

/**
 * 添加设备界面
 */
class AddDeviceActivity : BaseActivity() {

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

    /**
     * 跳转
     */
    private fun jump() {
        when (type) {
            0 -> {
                jumpActivity(ScanBindActivity::class.java)
            }
            1, 2 -> {
                IoTAuth.userImpl.userInfo(object: MyCallback {
                    override fun fail(msg: String?, reqCode: Int) {
                        TODO("Not yet implemented")
                    }

                    override fun success(response: BaseResponse, reqCode: Int) {
                        if (response.isSuccess()) {
                            response.parse(UserInfoResponse::class.java)?.Data.let {
                                App.data.userInfo = it!!
                                getBindDevToken(App.data.userInfo.UserID)
                            }
                        }

                    }
                })
            }
        }
    }

    private fun getBindDevToken(userId: String) {
        IoTAuth.deviceImpl.getBindDevToken(userId, object: MyCallback {
            override fun fail(msg: String?, reqCode: Int) {
                TODO("Not yet implemented")
            }

            override fun success(response: BaseResponse, reqCode: Int) {
                if (response.isSuccess()) {
                    response.parse(BindDeviceTokenResponse::class.java)?.Token.let {
                        if (!TextUtils.isEmpty(it)) {
                            App.data.bindDeviceToken = it!!
                            put("type", type)
                            jumpActivity(ConnectDeviceActivity::class.java)
                        }
                    }
                }
            }

        })
    }

}
