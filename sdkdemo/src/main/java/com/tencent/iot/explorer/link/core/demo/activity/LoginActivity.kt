package com.tencent.iot.explorer.link.core.demo.activity

import android.text.TextUtils
import android.widget.Toast
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.tencent.iot.explorer.link.core.auth.IoTAuth
import com.tencent.iot.explorer.link.core.auth.callback.LoginCallback
import com.tencent.iot.explorer.link.core.auth.callback.MyCallback
import com.tencent.iot.explorer.link.core.auth.entity.User
import com.tencent.iot.explorer.link.core.auth.response.BaseResponse
import com.tencent.iot.explorer.link.core.demo.App
import com.tencent.iot.explorer.link.core.demo.R
import com.tencent.iot.explorer.link.core.demo.response.UserInfoResponse
import com.tencent.iot.explorer.link.core.test.callback.VideoCallback
import com.tencent.iot.explorer.link.core.test.service.VideoBaseService
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
//            jumpActivity(ForgotPasswordActivity::class.java)
        }
        tv_config_net.setOnClickListener {
            VideoBaseService().describeDevices("", true, 10, 0, object:
                VideoCallback {
                override fun fail(msg: String?, reqCode: Int) {

                }

                override fun success(response: String?, reqCode: Int) {

                }

            })
            jumpActivity(ConfigNetActivity::class.java)
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
        IoTAuth.userImpl.userInfo(object: MyCallback{
            override fun fail(msg: String?, reqCode: Int) {

            }

            override fun success(response: BaseResponse, reqCode: Int) {
                response.parse(UserInfoResponse::class.java)?.Data?.run {
                    App.data.userInfo = this
                    FirebaseCrashlytics.getInstance().setUserId(App.data.userInfo.UserID)
                    FirebaseAnalytics.getInstance(this@LoginActivity).setUserId(App.data.userInfo.UserID)
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
