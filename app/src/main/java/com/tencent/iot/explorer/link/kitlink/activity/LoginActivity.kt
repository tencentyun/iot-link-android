package com.tencent.iot.explorer.link.kitlink.activity

import android.Manifest
import android.content.Intent
import android.os.Handler
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.core.text.isDigitsOnly
import com.alibaba.fastjson.JSONObject
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.tencent.iot.explorer.link.*
import com.tencent.iot.explorer.link.core.utils.Utils
import com.tencent.iot.explorer.link.customview.check.VerifyEdit
import com.tencent.iot.explorer.link.kitlink.consts.CommonField
import com.tencent.iot.explorer.link.kitlink.response.UserInfoResponse
import com.tencent.iot.explorer.link.kitlink.util.*
import com.tencent.iot.explorer.link.mvp.IPresenter
import com.tencent.iot.explorer.link.mvp.presenter.LoginPresenter
import com.tencent.iot.explorer.link.mvp.view.LoginView
import com.tencent.iot.explorer.link.core.utils.SharePreferenceUtil
import com.tencent.iot.explorer.link.T
import com.tencent.iot.explorer.link.core.auth.callback.MyCallback
import com.tencent.iot.explorer.link.core.auth.entity.User
import com.tencent.iot.explorer.link.core.auth.response.BaseResponse
import com.tencent.iot.explorer.link.core.utils.KeyBoardUtils
import kotlinx.android.synthetic.main.activity_login2.*
import kotlinx.android.synthetic.main.layout_account_passwd_login.view.*
import kotlinx.android.synthetic.main.layout_verify_code_login.view.*
import kotlinx.android.synthetic.main.menu_back_layout.*

class LoginActivity  : PActivity(), LoginView, View.OnClickListener, WeChatLogin.OnLoginListener  {

    private lateinit var presenter: LoginPresenter

    private lateinit var accoutPasswdLoginView: View
    private lateinit var verifyCodeLoginView: View
    private var mFirebaseAnalytics: FirebaseAnalytics? = null
    private var fromTag = ""
    private var accountType = false //true为手机号，false为邮箱
    private var accountForAutoFill = ""
    @Volatile
    private var canGetCode = true
    private var handler: Handler = Handler()

    private val permissions = arrayOf(
        Manifest.permission.RECEIVE_SMS,
        Manifest.permission.READ_SMS,
        Manifest.permission.SEND_SMS
    )

    override fun onResume() {
        super.onResume()
        if (!TextUtils.isEmpty(fromTag) && fromTag == CommonField.WAY_SOURCE) {
            return
        }
//        logout(this)
    }

    override fun getPresenter(): IPresenter? {
        return presenter
    }

    override fun getContentView(): Int {
        return R.layout.activity_login2
    }

    override fun initView() {
        App.data.regionId = "1"
        App.data.region = "ap-guangzhou"
        if (!checkPermissions(permissions)) {
            requestPermission(permissions)
        } else {
            permissionAllGranted()
        }
        intent.getStringExtra("from")?.let {
            fromTag = it
        }
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this@LoginActivity)
        presenter = LoginPresenter(this)
        tv_title.text = getString(R.string.verify_code_login)
        initViewPager()

        if (!TextUtils.isEmpty(App.data.getToken())) {
            val userId = SharePreferenceUtil.getString(this@LoginActivity, App.CONFIG, CommonField.USER_ID)
            FirebaseCrashlytics.getInstance().setUserId(userId)
            mFirebaseAnalytics!!.setUserId(userId)
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }
        onNewIntentIn()
        accoutPasswdLoginView.tv_login_to_country_bypsswd.text = getString(R.string.country_china) + getString(R.string.conutry_code_num, presenter.getCountryCode())
        verifyCodeLoginView.tv_login_to_country_byverifycode.text = getString(R.string.country_china) + getString(R.string.conutry_code_num, presenter.getCountryCode())

        loadLastCountryInfo()
    }

    private fun loadLastCountryInfo() {
        var countryInfo = Utils.getStringValueFromXml(T.getContext(), CommonField.COUNTRY_INFO, CommonField.COUNTRY_INFO)
        if (TextUtils.isEmpty(countryInfo)) return

        presenter.setCountry(countryInfo!!)
    }

    private fun initViewPager() {
        accoutPasswdLoginView = LayoutInflater.from(this).inflate(R.layout.layout_account_passwd_login, null)
        verifyCodeLoginView = LayoutInflater.from(this).inflate(R.layout.layout_verify_code_login, null)
        accoutPasswdLoginView.et_login_phone_or_email.addClearImage(accoutPasswdLoginView.iv_login_phone_or_email_clear)
        accoutPasswdLoginView.et_login_phone_or_email_passwd.addClearImage(accoutPasswdLoginView.iv_login_phone_or_email_passwd_clear)
        verifyCodeLoginView.et_login_phone_or_email_byverifycode.addClearImage(verifyCodeLoginView.iv_login_phone_or_email_clear_byverifycode)
        vp_login2.addViewToList(verifyCodeLoginView)
        vp_login2.addViewToList(accoutPasswdLoginView)
    }

    private fun onNewIntentIn() {
        intent?.let {
            val account = intent?.getStringExtra("account")?:""
            val pwd = intent?.getStringExtra("password")?:""
            if (!TextUtils.isEmpty(account) && !TextUtils.isEmpty(pwd)) {
                when (account.contains("@")) {
                    true -> {
                        presenter.setEmailData(account, pwd)
                        presenter.emailCommit()
                    }
                    else -> {
                        presenter.setPhoneData(account, pwd)
                        presenter.phoneCommit()
                    }
                }
            }
        }
    }

    override fun setListener() {
        iv_back.setOnClickListener { finish() }
        accoutPasswdLoginView.tv_use_verify_code_to_login.setOnClickListener(this)
        accoutPasswdLoginView.btn_account_passwd_login.setOnClickListener(this)
        accoutPasswdLoginView.tv_account_passwd_forget_passwd.setOnClickListener(this)
        accoutPasswdLoginView.iv_login_to_country_bypsswd.setOnClickListener(this)
        accoutPasswdLoginView.tv_login_to_country_bypsswd.setOnClickListener(this)
        accoutPasswdLoginView.et_login_phone_or_email.addTextChangedListener(accountPwdTextWatcher)
        accoutPasswdLoginView.et_login_phone_or_email_passwd.addTextChangedListener(accountPwdTextWatcher)
        accoutPasswdLoginView.et_login_phone_or_email.setText("")
        accoutPasswdLoginView.et_login_phone_or_email_passwd.setText("")

        verifyCodeLoginView.tv_use_passwd_to_login.setOnClickListener(this)
        verifyCodeLoginView.btn_account_verifycode_login.setOnClickListener(this)
        verifyCodeLoginView.tv_get_verify_code.setOnClickListener(this)
        verifyCodeLoginView.iv_login_to_country_byverifycode.setOnClickListener(this)
        verifyCodeLoginView.tv_login_to_country_byverifycode.setOnClickListener(this)
        verifyCodeLoginView.et_login_phone_or_email_byverifycode.addTextChangedListener(accountCodeTextWatcher)
        verifyCodeLoginView.et_login_phone_or_email_verifycode.addTextChangedListener(accountCodeTextWatcher)
        verifyCodeLoginView.et_login_phone_or_email_byverifycode.setText("")
        verifyCodeLoginView.et_login_phone_or_email_verifycode.setText("")
        iv_wechat_login.setOnClickListener(this)
    }

    private fun pwd2Check() {
//        accoutPasswdLoginView.btn_account_passwd_login.isEnabled =
//            isAccountOk(accoutPasswdLoginView.et_login_phone_or_email) &&
//                    isPwdOk(accoutPasswdLoginView.et_login_phone_or_email_passwd)
//        if (!isAccountOk(accoutPasswdLoginView.et_login_phone_or_email)) {
//            accoutPasswdLoginView.btn_account_passwd_login.setBackgroundResource(R.drawable.background_grey_dark_cell)
//        } else {
//            accoutPasswdLoginView.btn_account_passwd_login.setBackgroundResource(R.drawable.background_circle_bule_gradient)
//        }
        accoutPasswdLoginView.btn_account_passwd_login.setBackgroundResource(R.drawable.background_circle_bule_gradient)
    }

    private var accountPwdTextWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            pwd2Check()
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    }

    private fun code2Check() {
//        verifyCodeLoginView.btn_account_verifycode_login.isEnabled =
//            isAccountOk(verifyCodeLoginView.et_login_phone_or_email_byverifycode) &&
//                    isVerifyCodeOk(verifyCodeLoginView.et_login_phone_or_email_verifycode)

        //verifyCodeLoginView.tv_get_verify_code.isEnabled = isAccountOk(verifyCodeLoginView.et_login_phone_or_email_byverifycode)
        enableTextView(verifyCodeLoginView.tv_get_verify_code,
            isAccountOk(verifyCodeLoginView.et_login_phone_or_email_byverifycode))
//        if (!isAccountOk(verifyCodeLoginView.et_login_phone_or_email_byverifycode)) {
//            verifyCodeLoginView.btn_account_verifycode_login.setBackgroundResource(R.drawable.background_grey_dark_cell)
//        } else {
//            verifyCodeLoginView.btn_account_verifycode_login.setBackgroundResource(R.drawable.background_circle_bule_gradient)
//        }
        verifyCodeLoginView.btn_account_verifycode_login.setBackgroundResource(R.drawable.background_circle_bule_gradient)

    }

    private var accountCodeTextWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            code2Check()
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    }

    private fun enableTextView(textView: TextView, enable: Boolean) {
        if (textView == null) return
        if (!canGetCode) { // 当前处于无法获取验证码的状态时，不能设置状态
            textView.setTextColor(resources.getColor(R.color.gray_A1A7B2))
            textView.isEnabled = false
            return
        }

        if (enable) {
            textView.setTextColor(resources.getColor(R.color.blue_0066FF))
        } else {
            textView.setTextColor(resources.getColor(R.color.gray_A1A7B2))
        }
        textView.isEnabled = enable
    }

    // 匹配手机号码的规则 11 位且只有数字
    // 匹配邮箱的规则包含 @ 和 .
    private fun isAccountOk(accountVerifyEdit : VerifyEdit): Boolean {
        if (accountVerifyEdit == null) return false

        var accountTxt = accountVerifyEdit.text.toString()
        if ((presenter.getCountryCode() == "86" && accountTxt.length == 11 && accountTxt.isDigitsOnly()) ||
                    accountTxt.matches(Regex("^\\w+@(\\w+\\.)+\\w+$"))) {
            return true
        } else if ((presenter.getCountryCode() == "1" &&  accountTxt.length == 10 && accountTxt.isDigitsOnly()) ||
            accountTxt.matches(Regex("^\\w+@(\\w+\\.)+\\w+$"))) {
            return true
        }
        return false
    }

    // 只包含数字且只有6位
    private fun isVerifyCodeOk(verifyCodeVerifyEdit : VerifyEdit): Boolean {
        if (verifyCodeVerifyEdit == null) return false

        var accountTxt = verifyCodeVerifyEdit.text.toString()
        if ((accountTxt.length == 6 && accountTxt.isDigitsOnly())) {
            return true
        }
        return false
    }

    private fun isPwdOk(pwdVerifyEdit : VerifyEdit): Boolean {
        if (pwdVerifyEdit == null) return false

        var pwdTxt = pwdVerifyEdit.text.toString()
        if (pwdTxt.matches(Regex("^(?![0-9]+\$)(?![a-z]+\$)(?![A-Z]+\$).{8,}\$"))) {
            return true
        }
        return false
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
                val account = accoutPasswdLoginView.et_login_phone_or_email.text.trim().toString()
                val passwd = accoutPasswdLoginView.et_login_phone_or_email_passwd.text.trim().toString()
                if (!account.contains("@")) { // 手机登录
                    presenter.setPhoneData(account, passwd)
                    presenter.phoneCommit()
                } else { // 邮箱登录
                    presenter.setEmailData(account, passwd)
                    presenter.emailCommit()
                }
                KeyBoardUtils.hideKeyBoard(this, accoutPasswdLoginView.et_login_phone_or_email_passwd)
            }
            verifyCodeLoginView.btn_account_verifycode_login -> {// 验证码登录
                val account = verifyCodeLoginView.et_login_phone_or_email_byverifycode.text.trim().toString()
                val verifycode = verifyCodeLoginView.et_login_phone_or_email_verifycode.text.trim().toString()
                if (!account.contains("@")) {// 手机登录
                    presenter.setPhone(account)
                    if (!TextUtils.isEmpty(verifycode)) {
                        presenter.setVerifyCode(verifycode)
                        presenter.phoneVerifyCodeCommit()
                    } else {
                        T.show(getString(R.string.phone_verifycode_empty))
                    }
                } else {// 邮箱登录
                    presenter.setEmail(account)
                    if (!TextUtils.isEmpty(verifycode)) {
                        presenter.setVerifyCode(verifycode)
                        presenter.emailVerifyCodeCommit()
                    } else {
                        T.show(getString(R.string.email_verifycode_empty))
                    }
                }
            }
            accoutPasswdLoginView.tv_account_passwd_forget_passwd -> {// 忘记密码
                val account = accoutPasswdLoginView.et_login_phone_or_email.text.trim().toString()
                if (!account.contains("@")) {
                    accountType = true
                }
                Intent(this, ForgotPasswordActivity::class.java).run {
                    putExtra(RegisterActivity.ACCOUNT_TYPE, accountType)
                    startActivity(this)
                }
            }
            verifyCodeLoginView.tv_get_verify_code -> {// 获取验证码
                if (!canGetCode) {
                    return
                }
                Utils.startCountBySeconds(60, secondsCountDownCallback)
                canGetCode = false
                enableTextView(verifyCodeLoginView.tv_get_verify_code,
                    isAccountOk(verifyCodeLoginView.et_login_phone_or_email_byverifycode))

                val account = verifyCodeLoginView.et_login_phone_or_email_byverifycode.text.trim().toString()
                accountForAutoFill = account
                if (!account.contains("@")) {
                    accountType = true
                    presenter.setPhone(account)
                    presenter.requestPhoneCode()
                } else {
                    accountType = false
                    presenter.setEmail(account)
                    presenter.requestEmailCode()
                }
            }
            accoutPasswdLoginView.tv_login_to_country_bypsswd, accoutPasswdLoginView.iv_login_to_country_bypsswd -> {// 账号密码登录时选则国家
                startActivityForResult(Intent(this, RegionActivity::class.java), 100)
            }
            verifyCodeLoginView.tv_login_to_country_byverifycode, verifyCodeLoginView.iv_login_to_country_byverifycode -> {// 验证码登录时选则国家
                startActivityForResult(Intent(this, RegionActivity::class.java), 100)
            }
        }
    }

    var secondsCountDownCallback = object: Utils.SecondsCountDownCallback {
        override fun currentSeconds(seconds: Int) {
            handler.post(Runnable {
                verifyCodeLoginView.tv_get_verify_code.setText(getString(R.string.to_resend, seconds.toString()))
            })
        }

        override fun countDownFinished() {
            canGetCode = true
            handler.post(Runnable {
                verifyCodeLoginView.tv_get_verify_code.setText(getString(R.string.resend))
                enableTextView(verifyCodeLoginView.tv_get_verify_code, // 根据实际情况设置获取验证码按钮的状态
                    isAccountOk(verifyCodeLoginView.et_login_phone_or_email_byverifycode))
            })
        }

    }

    private fun showVerifyCodeLogin() {
        tv_title.text = getString(R.string.verify_code_login)
        vp_login2.setCurrentItem(0, true)
        verifyCodeLoginView.et_login_phone_or_email_byverifycode.setText(accoutPasswdLoginView.et_login_phone_or_email.text)
        verifyCodeLoginView.et_login_phone_or_email_byverifycode.setSelection(verifyCodeLoginView.et_login_phone_or_email_byverifycode.text.toString().length)
    }

    private fun showPasswdLogin() {
        tv_title.text = getString(R.string.account_passwd_login)
        vp_login2.setCurrentItem(1, true)
        accoutPasswdLoginView.et_login_phone_or_email.setText(verifyCodeLoginView.et_login_phone_or_email_byverifycode.text)
        accoutPasswdLoginView.et_login_phone_or_email.setSelection(accoutPasswdLoginView.et_login_phone_or_email.text.toString().length)
    }

    private fun getUserId(user: User) {
        HttpRequest.instance.userInfo(object : MyCallback {
            override fun fail(msg: String?, reqCode: Int) {
                if (TextUtils.isEmpty(msg)) {
                    this@LoginActivity.loginFail(getString(R.string.get_userId_failed))
                    return
                }
                this@LoginActivity.loginFail(msg!!)
            }
            override fun success(response: BaseResponse, reqCode: Int) {
                if (response.isSuccess()) {
                    response.parse(UserInfoResponse::class.java)?.Data?.run {
                        App.data.userInfo = this
                        SharePreferenceUtil.saveString(this@LoginActivity, App.CONFIG, CommonField.USER_ID, App.data.userInfo.UserID)
                        FirebaseCrashlytics.getInstance().setUserId(App.data.userInfo.UserID)
                        mFirebaseAnalytics?.setUserId(App.data.userInfo.UserID)
                        saveUser(user)
                        T.show(getString(R.string.login_success))
                        if (TextUtils.isEmpty(fromTag)) {
                            // 记录登录账号的国家码
                            var countryCodeJson = JSONObject()
                            countryCodeJson.put(CommonField.COUNTRY_CODE, presenter.getCountryCode())
                            Utils.setXmlStringValue(T.getContext(), CommonField.COUNTRY_CODE,
                                CommonField.COUNTRY_CODE, countryCodeJson.toJSONString())
                            startActivity(Intent(this@LoginActivity, MainActivity::class.java))

                        } else {
                            val data = Intent()
                            data.putExtra("data", AppData.instance.getToken())
                            setResult(CommonField.H5_REQUEST_LOGIN_CODE)
                        }
                        finish()
                    }
                } else {
                    this@LoginActivity.loginFail(getString(R.string.get_userId_failed))
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
    override fun loginFail(response: BaseResponse) {
        T.show(response.msg)
        if (response.code == ErrorCode.REQ_ERROR_CODE) {
            val errMsg = ErrorMessage.parseErrorMessage(response.data.toString())
            if (errMsg.Code == ErrorCode.DATA_MSG.ErrorUserNotExists) {
//                T.show("您的账号暂未注册，请前往腾讯连连小程序或APP绑定账号后重新登录")
                Intent(this, RegisterActivity::class.java).run {
                    putExtra(RegisterActivity.ACCOUNT_TYPE, accountType)
                    putExtra(RegisterActivity.ACCOUNT_NUMBER, accountForAutoFill)
                    startActivity(this)
                }
            }
        }
    }
    override fun showCountryCode(countryName: String, countryCode: String) {
        accoutPasswdLoginView.tv_login_to_country_bypsswd.text = countryName + getString(R.string.conutry_code_num, presenter.getCountryCode())
        verifyCodeLoginView.tv_login_to_country_byverifycode.text = countryName + getString(R.string.conutry_code_num, presenter.getCountryCode())
    }

    override fun sendVerifyCodeSuccess() {
        T.show(getString(R.string.send_verifycode_success))
    }

    override fun sendVerifyCodeFail(msg: ErrorMessage) {
        T.show(msg.Message)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100) {
            data?.let {
                it.getStringExtra(CommonField.REGION_ID)?.run {
                    presenter.setCountry(this)
                    Utils.setXmlStringValue(T.getContext(), CommonField.COUNTRY_INFO, CommonField.COUNTRY_INFO, this)
                }
            }
            code2Check()
            pwd2Check()
        }
    }
}