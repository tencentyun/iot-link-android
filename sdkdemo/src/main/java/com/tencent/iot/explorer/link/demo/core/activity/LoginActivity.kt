package com.tencent.iot.explorer.link.demo.core.activity

import android.text.TextUtils
import android.widget.Toast
import com.tencent.iot.explorer.link.core.auth.IoTAuth
import com.tencent.iot.explorer.link.core.auth.callback.LoginCallback
import com.tencent.iot.explorer.link.core.auth.callback.MyCallback
import com.tencent.iot.explorer.link.core.auth.entity.User
import com.tencent.iot.explorer.link.core.auth.response.BaseResponse
import com.tencent.iot.explorer.link.demo.App
import com.tencent.iot.explorer.link.demo.BaseActivity
import com.tencent.iot.explorer.link.demo.R
import com.tencent.iot.explorer.link.demo.core.response.UserInfoResponse
import com.tencent.iot.explorer.link.demo.databinding.ActivityLoginBinding

/**
 * 登录
 */
class LoginActivity : BaseActivity<ActivityLoginBinding>(), LoginCallback {

    private var account = ""
    private var pwd = ""
    private val countryCode = "86"

    override fun getViewBinding(): ActivityLoginBinding = ActivityLoginBinding.inflate(layoutInflater)

    override fun initView() {
    }

    override fun setListener() {
        with(binding) {
            btnLoginCommit.setOnClickListener {
                login()
            }
            btnToRegister.setOnClickListener {
                jumpActivity(RegisterActivity::class.java)
            }
            btnToForgot.setOnClickListener {
                jumpActivity(ForgotPasswordActivity::class.java)
            }
        }
    }

    private fun login() {
        account = binding.etLoginAccount.text.toString().trim()
        if (TextUtils.isEmpty(account)) {
            Toast.makeText(this, "请求输入手机号/邮箱", Toast.LENGTH_LONG).show()
            return
        }
        pwd = binding.etLoginPwd.text.toString().trim()
        if (pwd.length < 8) {
            Toast.makeText(this, "密码长度至少为8位", Toast.LENGTH_LONG).show()
            return
        }
        when (account.contains("@")) {
            true -> IoTAuth.loginImpl.loginEmail(account, pwd, this)
            else -> IoTAuth.loginImpl.loginPhone(countryCode, account, pwd, this)
        }
    }

    override fun success(user: User) {
        //成功跳转
        IoTAuth.userImpl.userInfo(object: MyCallback{
            override fun fail(msg: String?, reqCode: Int) {

            }

            override fun success(response: BaseResponse, reqCode: Int) {
                response.parse(UserInfoResponse::class.java)?.Data?.run {
                    App.data.userInfo = this
                    jumpActivity(MainActivity::class.java, true)
                }
            }

        })
    }

    override fun fail(msg: String) {
        runOnUiThread {
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
        }
    }
}
