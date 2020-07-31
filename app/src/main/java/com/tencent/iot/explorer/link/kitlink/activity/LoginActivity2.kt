package com.tencent.iot.explorer.link.kitlink.activity

import android.Manifest
import android.content.Intent
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import com.google.firebase.analytics.FirebaseAnalytics
import com.tencent.iot.explorer.link.App
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.kitlink.consts.CommonField
import com.tencent.iot.explorer.link.kitlink.entity.User
import com.tencent.iot.explorer.link.kitlink.response.BaseResponse
import com.tencent.iot.explorer.link.kitlink.response.UserInfoResponse
import com.tencent.iot.explorer.link.kitlink.util.AppData
import com.tencent.iot.explorer.link.kitlink.util.HttpRequest
import com.tencent.iot.explorer.link.kitlink.util.MyCallback
import com.tencent.iot.explorer.link.kitlink.util.WeChatLogin
import com.tencent.iot.explorer.link.mvp.IPresenter
import com.tencent.iot.explorer.link.mvp.presenter.LoginPresenter
import com.tencent.iot.explorer.link.mvp.view.LoginView
import com.tencent.iot.explorer.link.util.SharePreferenceUtil
import com.tencent.iot.explorer.link.util.T
import kotlinx.android.synthetic.main.activity_login2.*
import kotlinx.android.synthetic.main.layout_account_passwd_login.view.*
import kotlinx.android.synthetic.main.layout_phone_forgot_pwd.view.*
import kotlinx.android.synthetic.main.layout_verify_code_login.view.*
import kotlinx.android.synthetic.main.menu_back_layout.*

class LoginActivity2  : PActivity(), LoginView, View.OnClickListener, WeChatLogin.OnLoginListener  {

    private lateinit var presenter: LoginPresenter

    private lateinit var accoutPasswdLoginView: View
    private lateinit var verifyCodeLoginView: View
    private var mFirebaseAnalytics: FirebaseAnalytics? = null
    private var fromTag = ""

    private val permissions = arrayOf(
        Manifest.permission.RECEIVE_SMS,
        Manifest.permission.READ_SMS,
        Manifest.permission.SEND_SMS
    )

    override fun onResume() {
        super.onResume()
        if (!TextUtils.isEmpty(fromTag) && fromTag.equals(CommonField.WAY_SOURCE)) {
            return
        }
        logout(this)
    }

    override fun getPresenter(): IPresenter? {
        return null
    }

    override fun getContentView(): Int {
        return R.layout.activity_login2
    }

    override fun initView() {
        if (!checkPermissions(permissions)) {
            requestPermission(permissions)
        } else {
            permissionAllGranted()
        }
        intent.getStringExtra("from")?.let {
            fromTag = it
        }
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this@LoginActivity2)
        presenter = LoginPresenter(this)
        iv_back.setColorFilter(resources.getColor(R.color.black_333333))
        tv_title.text = getString(R.string.account_passwd_login)
        initViewPager()

        if (!TextUtils.isEmpty(App.data.getToken())) {
            var userId = SharePreferenceUtil.getString(this@LoginActivity2, App.CONFIG, CommonField.USER_ID)
            mFirebaseAnalytics!!.setUserId(userId);
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }
    }

    private fun initViewPager() {
        accoutPasswdLoginView = LayoutInflater.from(this).inflate(R.layout.layout_account_passwd_login, null)
        verifyCodeLoginView = LayoutInflater.from(this).inflate(R.layout.layout_verify_code_login, null)
        accoutPasswdLoginView.et_login_phone_or_email.addClearImage(accoutPasswdLoginView.iv_login_phone_or_email_clear)
        accoutPasswdLoginView.et_login_phone_or_email_passwd.addClearImage(accoutPasswdLoginView.iv_login_phone_or_email_passwd_clear)
        verifyCodeLoginView.et_login_phone_or_email_byverifycode.addClearImage(verifyCodeLoginView.iv_login_phone_or_email_clear_byverifycode)
        vp_login2.addViewToList(accoutPasswdLoginView)
        vp_login2.addViewToList(verifyCodeLoginView)
    }

    override fun setListener() {
        iv_back.setOnClickListener { finish() }
        accoutPasswdLoginView.tv_use_verify_code_to_login.setOnClickListener(this)
        accoutPasswdLoginView.btn_account_passwd_login.setOnClickListener(this)
        accoutPasswdLoginView.tv_account_passwd_forget_passwd.setOnClickListener(this)

        verifyCodeLoginView.tv_use_passwd_to_login.setOnClickListener(this)
        verifyCodeLoginView.btn_account_verifycode_login.setOnClickListener(this)
        verifyCodeLoginView.tv_get_verify_code.setOnClickListener(this)

        iv_wechat_login.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v) {
            verifyCodeLoginView.tv_use_passwd_to_login -> {// 显示密码登录界面
                showPasswdLogin()
            }
            accoutPasswdLoginView.tv_use_verify_code_to_login -> {// 显示验证码登录界面
                showVerifyCodeLogin()
            }
            iv_wechat_login -> {// 微信登录
                WeChatLogin.getInstance().login(this, this)
            }
            accoutPasswdLoginView.btn_account_passwd_login -> {// 密码登录
                T.show("密码登录")
                val account = accoutPasswdLoginView.et_login_phone_or_email.text.trim().toString()
                val passwd = accoutPasswdLoginView.et_login_phone_or_email_passwd.text.trim().toString()
                if (!account.contains("@")) { // 手机登录
                    presenter.setPhoneData(account, passwd)
                    presenter.phoneCommit()
                } else { // 邮箱登录
                    presenter.setEmailData(account, passwd)
                    presenter.emailCommit()
                }
            }
            verifyCodeLoginView.btn_account_verifycode_login -> {// 验证码登录
                T.show("验证码登录")
                val account = verifyCodeLoginView.et_login_phone_or_email_byverifycode.text.trim().toString()
                val passwd = verifyCodeLoginView.et_login_phone_or_email_verifycode.text.trim().toString()
                if (!account.contains("@")) {
                    T.show(account)
                } else {
                    T.show(account)
                }
            }
            accoutPasswdLoginView.tv_account_passwd_forget_passwd -> {// 忘记密码
                T.show("忘记密码")
                val account = accoutPasswdLoginView.et_login_phone_or_email.text.trim().toString()
                var accountType = false //true为手机号，false为邮箱
                if (!account.contains("@")) {
                    accountType = true
                }
                Intent(this, ForgotPasswordActivity::class.java).run {
                    putExtra(RegisterActivity.ACCOUNT_TYPE, accountType)
                    startActivity(this)
                }
            }
            verifyCodeLoginView.tv_get_verify_code -> {// 获取验证码
                T.show("获取验证码")
                val account = verifyCodeLoginView.et_login_phone_or_email_byverifycode.text.trim().toString()
                if (!account.contains("@")) {
                    presenter.setPhone(account)
                    presenter.requestPhoneCode()
                } else {
                    presenter.setEmail(account)
                    presenter.requestEmailCode()
                }

                // TODO 判断账号是否注册过，若未注册则跳到注册页面
            }
        }
    }

    private fun showVerifyCodeLogin() {
        tv_title.text = getString(R.string.verify_code_login)
        vp_login2.setCurrentItem(1, true)
    }

    private fun showPasswdLogin() {
        tv_title.text = getString(R.string.account_passwd_login)
        vp_login2.setCurrentItem(0, true)
    }

    private fun getUserId(user: User) {
        HttpRequest.instance.userInfo(object : MyCallback {
            override fun fail(msg: String?, reqCode: Int) {
                if (TextUtils.isEmpty(msg)) {
                    this@LoginActivity2.loginFail(getString(R.string.get_userId_failed))
                    return
                }
                this@LoginActivity2.loginFail(msg!!)
            }
            override fun success(response: BaseResponse, reqCode: Int) {
                if (response.isSuccess()) {
                    response.parse(UserInfoResponse::class.java)?.Data?.run {
                        App.data.userInfo = this
                        SharePreferenceUtil.saveString(this@LoginActivity2, App.CONFIG, CommonField.USER_ID, App.data.userInfo.UserID)
                        mFirebaseAnalytics!!.setUserId(App.data.userInfo.UserID)
                        saveUser(user)
                        T.show(getString(R.string.login_success))
                        if (TextUtils.isEmpty(fromTag)) {
                            startActivity(Intent(this@LoginActivity2, MainActivity::class.java))
                        } else {
                            val data = Intent()
                            data.putExtra("data", AppData.instance.getToken())
                            setResult(CommonField.H5_REQUEST_LOGIN_CODE)
                        }
                        finish()
                    }
                } else {
                    this@LoginActivity2.loginFail(getString(R.string.get_userId_failed))
                }
            }
        })
    }

    // Wechat login callback method start
    override fun onSuccess(reqCode: String) {
        presenter.wechatLogin(reqCode)
    }

    override fun cancel() {
        T.show(getString(R.string.cancel_wechat))
    }

    override fun onFail(msg: String) {
        T.show(msg)
    }
    // Wechat login callback method end

    // LoginView callback method start
    override fun loginSuccess(user: User) {
        getUserId(user)
    }

    override fun loginFail(msg: String) {
        T.show(msg)
    }

    override fun showCountryCode(countryName: String, countryCode: String) {
        TODO("Not yet implemented")
    }
    // LoginView callback method end
}