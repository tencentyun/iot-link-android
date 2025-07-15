package com.tencent.iot.explorer.link.demo.core.activity

import android.text.TextUtils
import android.widget.Toast
import com.tencent.iot.explorer.link.core.auth.IoTAuth
import com.tencent.iot.explorer.link.core.auth.callback.MyCallback
import com.tencent.iot.explorer.link.core.auth.consts.RequestCode
import com.tencent.iot.explorer.link.core.auth.response.BaseResponse
import com.tencent.iot.explorer.link.demo.BaseActivity
import com.tencent.iot.explorer.link.demo.R
import com.tencent.iot.explorer.link.demo.databinding.ActivityForgotPasswordBinding

/**
 * 注册
 */
class ForgotPasswordActivity : BaseActivity<ActivityForgotPasswordBinding>(), MyCallback {

    private var account = ""
    private var pwd = ""
    private val countryCode = "86"
    private var code = ""

    override fun getViewBinding(): ActivityForgotPasswordBinding = ActivityForgotPasswordBinding.inflate(layoutInflater)

    override fun initView() {
    }

    override fun setListener() {
        with(binding) {
            btnForgotGetCode.setOnClickListener {
                getCode()
            }
            btnForgotCommit.setOnClickListener {
                checkCode()
            }
        }
    }

    private fun getCode() {
        account = binding.etForgotAccount.text.toString().trim()
        if (TextUtils.isEmpty(account)) {
            Toast.makeText(this, "请求输入手机号/邮箱", Toast.LENGTH_LONG).show()
            return
        }
        when (account.contains("@")) {
            true -> IoTAuth.passwordImpl.sendEmailCode(account, this)
            else -> IoTAuth.passwordImpl.sendPhoneCode(countryCode, account, this)
        }
    }

    private fun checkCode() {
        pwd = binding.etForgotPwd.text.toString().trim()
        if (pwd.length < 8) {
            Toast.makeText(this, "密码长度至少为8位", Toast.LENGTH_LONG).show()
            return
        }
        code = binding.etForgotCode.text.toString().trim()
        if (code.length != 6) {
            Toast.makeText(this, "验证码长为6位数字", Toast.LENGTH_LONG).show()
            return
        }

        when (account.contains("@")) {
            true -> IoTAuth.passwordImpl.checkEmailCode(account, code, this)
            else -> IoTAuth.passwordImpl.checkPhoneCode(countryCode, account, code, this)
        }
    }

    private fun setPassword() {
        when (account.contains("@")) {
            true -> IoTAuth.passwordImpl.resetEmailPassword(account, code, pwd, this)
            else -> IoTAuth.passwordImpl.resetPhonePassword(countryCode, account, code, pwd, this)
        }
    }

    override fun fail(msg: String?, reqCode: Int) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }

    override fun success(response: BaseResponse, reqCode: Int) {
        if (response.isSuccess()) {
            when (reqCode) {
                RequestCode.send_phone_code, RequestCode.send_email_code ->
                    show("验证码发送成功")
                RequestCode.check_phone_code, RequestCode.check_email_code ->
                    setPassword()
                RequestCode.phone_reset_pwd, RequestCode.email_reset_pwd -> {
                    show("密码重置成功")
                    finish()
                }
            }
        } else {
            show(response.msg)
        }
    }

}
