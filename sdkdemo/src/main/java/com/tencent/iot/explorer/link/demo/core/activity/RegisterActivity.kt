package com.tencent.iot.explorer.link.demo.core.activity

import android.text.TextUtils
import android.widget.Toast
import com.tencent.iot.explorer.link.core.auth.IoTAuth
import com.tencent.iot.explorer.link.core.auth.callback.MyCallback
import com.tencent.iot.explorer.link.core.auth.consts.RequestCode
import com.tencent.iot.explorer.link.core.auth.response.BaseResponse
import com.tencent.iot.explorer.link.demo.BaseActivity
import com.tencent.iot.explorer.link.demo.R
import kotlinx.android.synthetic.main.activity_register.*

/**
 * 注册
 */
class RegisterActivity : BaseActivity(), MyCallback {

    private var account = ""
    private var pwd = ""
    private val countryCode = "86"
    private var code = ""

    override fun getContentView(): Int {
        return R.layout.activity_register
    }

    override fun initView() {
    }

    override fun setListener() {
        btn_register_get_code.setOnClickListener {
            getCode()
        }
        btn_register_commit.setOnClickListener {
            checkCode()
        }
    }

    private fun getCode() {
        account = et_register_account.text.toString().trim()
        if (TextUtils.isEmpty(account)) {
            Toast.makeText(this, "请求输入手机号/邮箱", Toast.LENGTH_LONG).show()
            return
        }
        when (account.contains("@")) {
            true -> IoTAuth.registerImpl.sendEmailCode(account, this)
            else -> IoTAuth.registerImpl.sendPhoneCode(countryCode, account, this)
        }
    }

    private fun checkCode() {
        pwd = et_register_pwd.text.toString().trim()
        if (pwd.length < 8) {
            Toast.makeText(this, "密码长度至少为8位", Toast.LENGTH_LONG).show()
            return
        }
        code = et_register_code.text.toString().trim()
        if (code.length != 6) {
            Toast.makeText(this, "验证码长为6位数字", Toast.LENGTH_LONG).show()
            return
        }
        when (account.contains("@")) {
            true -> IoTAuth.registerImpl.checkEmailCode(account, code, this)
            else -> IoTAuth.registerImpl.checkPhoneCode(countryCode, account, code, this)
        }
    }

    private fun register() {
        when (account.contains("@")) {
            true -> IoTAuth.registerImpl.registerEmail(account, code, pwd, this)
            else -> IoTAuth.registerImpl.registerPhone(countryCode, account, code, pwd, this)
        }
    }

    override fun fail(msg: String?, reqCode: Int) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }

    override fun success(response: BaseResponse, reqCode: Int) {
        if (response.isSuccess()) {
            when (reqCode) {
                RequestCode.send_phone_code, RequestCode.send_email_code ->
                    Toast.makeText(this, "验证码发送成功", Toast.LENGTH_LONG).show()
                RequestCode.check_phone_code, RequestCode.check_email_code ->
                    register()
                RequestCode.phone_register, RequestCode.email_register -> {
                    show("注册成功")
                    finish()
                }
            }
        } else {
            show(response.msg)
        }
    }

}
