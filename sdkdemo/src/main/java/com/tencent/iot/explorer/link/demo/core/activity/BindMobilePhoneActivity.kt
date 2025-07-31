package com.tencent.iot.explorer.link.demo.core.activity

import com.tencent.iot.explorer.link.core.auth.IoTAuth
import com.tencent.iot.explorer.link.core.auth.callback.MyCallback
import com.tencent.iot.explorer.link.core.auth.response.BaseResponse
import com.tencent.iot.explorer.link.demo.BaseActivity
import com.tencent.iot.explorer.link.demo.R
import com.tencent.iot.explorer.link.demo.common.log.L
import com.tencent.iot.explorer.link.demo.databinding.ActivityBindMobilePhoneBinding

class BindMobilePhoneActivity : BaseActivity<ActivityBindMobilePhoneBinding>() {

    private var account = ""
    private val countryCode = "86"
    private var code = ""

    override fun getViewBinding(): ActivityBindMobilePhoneBinding = ActivityBindMobilePhoneBinding.inflate(layoutInflater)

    override fun initView() {
    }

    override fun setListener() {
        with(binding) {
            menuBindPhone.ivBack.setOnClickListener { finish() }
            btnBindPhoneGetCode.setOnClickListener { getCode() }
            btnBindPhoneCommit.setOnClickListener { commit() }
        }
    }

    private fun getCode() {
        account = binding.etBindPhone.text.trim().toString()
        if (account.length != 11) {
            show("手机号不正确")
            return
        }
        IoTAuth.userImpl.sendBindPhoneCode(countryCode, account, object : MyCallback {
            override fun fail(msg: String?, reqCode: Int) {
                L.d { msg ?: "" }
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
        code = binding.etBindPhone.text.trim().toString()
        if (code.length != 6) {
            show("验证码为6位数字")
            return
        }
        IoTAuth.userImpl.checkBindPhoneCode(countryCode, account, code, object : MyCallback {
            override fun fail(msg: String?, reqCode: Int) {
                L.d { msg ?: "" }
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
                L.d { msg ?: "" }
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
