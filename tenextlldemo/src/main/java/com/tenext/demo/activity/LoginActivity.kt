package com.tenext.demo.activity

import android.text.TextUtils
import android.widget.Toast
import com.tenext.auth.IoTAuth
import com.tenext.auth.callback.LoginCallback
import com.tenext.auth.entity.User
import com.tenext.demo.R
import kotlinx.android.synthetic.main.activity_login.*

/**
 * 登录
 */
class LoginActivity : BaseActivity(), LoginCallback {

    private var account = ""
    private var pwd = ""
    private val countryCode = "86"

    override fun getContentView(): Int {
        return R.layout.activity_login
    }

    override fun initView() {
    }

    override fun setListener() {
        btn_login_commit.setOnClickListener {
            login()
        }
        btn_to_register.setOnClickListener {
            jumpActivity(RegisterActivity::class.java)
        }
        btn_to_forgot.setOnClickListener {
            jumpActivity(ForgotPasswordActivity::class.java)
        }
    }

    private fun login() {
        account = et_login_account.text.toString().trim()
        if (TextUtils.isEmpty(account)) {
            Toast.makeText(this, "请求输入手机号/邮箱", Toast.LENGTH_LONG).show()
            return
        }
        pwd = et_login_pwd.text.toString().trim()
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
        jumpActivity(MainActivity::class.java, true)
    }

    override fun fail(msg: String) {
        runOnUiThread {
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
        }
    }
}
