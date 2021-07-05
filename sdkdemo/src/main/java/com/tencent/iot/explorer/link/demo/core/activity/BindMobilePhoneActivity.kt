package com.tencent.iot.explorer.link.demo.core.activity

import com.tencent.iot.explorer.link.core.auth.IoTAuth
import com.tencent.iot.explorer.link.core.auth.callback.MyCallback
import com.tencent.iot.explorer.link.core.auth.response.BaseResponse
import com.tencent.iot.explorer.link.core.demo.R
import com.tencent.iot.explorer.link.demo.common.log.L
import kotlinx.android.synthetic.main.activity_bind_mobile_phone.*
import kotlinx.android.synthetic.main.menu_back_layout.*

class BindMobilePhoneActivity : BaseActivity() {

    private var account = ""
    private val countryCode = "86"
    private var code = ""

    override fun getContentView(): Int {
        return R.layout.activity_bind_mobile_phone
    }

    override fun initView() {
    }

    override fun setListener() {
        iv_back.setOnClickListener { finish() }
        btn_bind_phone_get_code.setOnClickListener { getCode() }
        btn_bind_phone_commit.setOnClickListener { commit() }
    }

    private fun getCode() {
        account = et_bind_phone.text.trim().toString()
        if (account.length != 11) {
            show("手机号不正确")
            return
        }
        IoTAuth.userImpl.sendBindPhoneCode(countryCode, account, object : MyCallback {
            override fun fail(msg: String?, reqCode: Int) {
                L.d(msg ?: "")
            }

            override fun success(response: BaseResponse, reqCode: Int) {
                if (response.isSuccess()) {
                    show("验证码发送成功")
                } else {
                    show(response.msg)
                }
            }

        })
    }

    private fun commit() {
        code = et_bind_code.text.trim().toString()
        if (code.length != 6) {
            show("验证码为6位数字")
            return
        }
        IoTAuth.userImpl.checkBindPhoneCode(countryCode, account, code, object : MyCallback {
            override fun fail(msg: String?, reqCode: Int) {
                L.d(msg ?: "")
            }

            override fun success(response: BaseResponse, reqCode: Int) {
                if (response.isSuccess()) {
                    bindPhone()
                } else {
                    show(response.msg)
                }
            }
        })
    }

    private fun bindPhone() {
        IoTAuth.userImpl.bindPhone(countryCode, account, code, object : MyCallback {
            override fun fail(msg: String?, reqCode: Int) {
                L.d(msg ?: "")
            }

            override fun success(response: BaseResponse, reqCode: Int) {
                if (response.isSuccess()) {
                    show("手机号绑定成功")
                    finish()
                } else {
                    show(response.msg)
                }
            }
        })
    }

}
