package com.tencent.iot.explorer.link.kitlink.activity

import android.Manifest
import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.Intent
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import com.tencent.iot.explorer.link.kitlink.App
import com.tencent.iot.explorer.link.kitlink.R
import com.tencent.iot.explorer.link.kitlink.consts.CommonField
import com.tencent.iot.explorer.link.kitlink.entity.User
import com.tencent.iot.explorer.link.kitlink.util.StatusBarUtil
import com.tencent.iot.explorer.link.kitlink.util.WeChatLogin
import com.tencent.iot.explorer.link.mvp.IPresenter
import com.tencent.iot.explorer.link.mvp.presenter.LoginPresenter
import com.tencent.iot.explorer.link.mvp.view.LoginView
import com.util.L
import com.util.T
import com.util.keyboard.KeyBoardUtils
import com.util.keyboard.OnSoftKeyBoardListener
import com.util.keyboard.SoftKeyBoard
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.layout_email_login.view.*
import kotlinx.android.synthetic.main.layout_phone_login.view.*
import kotlin.math.absoluteValue

/**
 * 手机号登录界面
 */
class LoginActivity : PActivity(), LoginView, View.OnClickListener, WeChatLogin.OnLoginListener {

    private lateinit var presenter: LoginPresenter
    private lateinit var phoneView: View
    private lateinit var emailView: View
    private lateinit var showAnim: ObjectAnimator
    private lateinit var hideAnim: ObjectAnimator
    private lateinit var showAnim1: ObjectAnimator
    private lateinit var hideAnim1: ObjectAnimator

    //true为手机号，false为邮箱
    private var loginType = true
    private var keyBoard: SoftKeyBoard? = null

    private val permissions = arrayOf(
        Manifest.permission.RECEIVE_SMS,
        Manifest.permission.READ_SMS,
        Manifest.permission.SEND_SMS
    )

    override fun getContentView(): Int {
        return R.layout.activity_login
    }

    override fun getPresenter(): IPresenter? {
        return presenter
    }

    override fun onResume() {
        super.onResume()
        logout(this)
    }

    override fun onPause() {
        KeyBoardUtils.hideKeyBoard(this, login)
        L.e("onPause")
        super.onPause()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
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

    override fun initView() {
        if (!checkPermissions(permissions)) {
            requestPermission(permissions)
        } else {
            permissionAllGranted()
        }

        presenter = LoginPresenter(this)
        keyBoard = SoftKeyBoard(this)
        initViewPager()
        //设置白色状态栏
        StatusBarUtil.setStatusBarDarkTheme(this, false)
        if (!TextUtils.isEmpty(App.data.getToken())) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }
        getIntentData()
        addLoginAnim()
        addRegisterAnim()
    }

    private fun addLoginAnim() {
        val mY = dp2px(100).toFloat()
        val duration = 300L
        showAnim = ObjectAnimator.ofFloat(card_login_container, "translationY", 0f, -mY)
        showAnim.duration = duration
        showAnim.addUpdateListener {
            val offset = it.animatedValue.toString().toFloat().absoluteValue
            ll_login_logo.alpha = 1 - offset / mY
        }
        hideAnim = ObjectAnimator.ofFloat(card_login_container, "translationY", -mY, 0f)
        hideAnim.duration = duration
        hideAnim.addUpdateListener {
            val offset = it.animatedValue.toString().toFloat().absoluteValue
            ll_login_logo.alpha = 1 - offset / mY
        }
        hideAnim.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {
            }

            override fun onAnimationEnd(animation: Animator?) {
                phoneView.clearFocus()
                emailView.clearFocus()
            }

            override fun onAnimationCancel(animation: Animator?) {
            }

            override fun onAnimationStart(animation: Animator?) {
            }
        })
    }

    private fun addRegisterAnim() {
        val mY = dp2px(100).toFloat()
        val duration = 300L
        showAnim1 = ObjectAnimator.ofFloat(card_login_register, "translationY", 0f, mY)
        showAnim1.duration = duration
        showAnim1.addUpdateListener {
            val offset = it.animatedValue.toString().toFloat().absoluteValue
            card_login_register.alpha = 1 - offset / mY
        }
        hideAnim1 = ObjectAnimator.ofFloat(card_login_register, "translationY", mY, 0f)
        hideAnim1.duration = duration
        hideAnim1.addUpdateListener {
            val offset = it.animatedValue.toString().toFloat().absoluteValue
            card_login_register.alpha = 1 - offset / mY
        }
    }

    private fun getIntentData() {
        intent?.let {
            if (!TextUtils.isEmpty(intent.getStringExtra(GetCodeActivity.PHONE))) {
                phoneView.et_login_phone.setText(intent.getStringExtra(GetCodeActivity.PHONE))
                showPhoneLogin()
                return
            }
            if (!TextUtils.isEmpty(intent.getStringExtra(GetCodeActivity.EMAIL))) {
                emailView.et_login_email.setText(intent.getStringExtra(GetCodeActivity.EMAIL))
                showEmailLogin()
                return
            }
        }
        showPhoneLogin()
    }

    private fun initViewPager() {
        phoneView = LayoutInflater.from(this).inflate(R.layout.layout_phone_login, null)
        emailView = LayoutInflater.from(this).inflate(R.layout.layout_email_login, null)
        phoneView.run {
            et_login_phone.addClearImage(iv_login_phone_clear)
            et_login_pwd.addShowImage(
                iv_login_show_pwd,
                R.mipmap.icon_visible,
                R.mipmap.icon_invisible
            )
            et_login_pwd.addClearImage(iv_login_pwd_clear)

        }
        emailView.run {
            et_login_email.addClearImage(iv_login_email_clear)
            et_login_email_pwd.addClearImage(iv_login_email_pwd_clear)
            et_login_email_pwd.addShowImage(
                iv_login_email_show_pwd,
                R.mipmap.icon_visible,
                R.mipmap.icon_invisible
            )
        }
        vp_login.addViewToList(phoneView)
        vp_login.addViewToList(emailView)
        vp_login.setScrollDuration(1000)
    }

    override fun setListener() {
        login_back.setOnClickListener(this)
        login.setOnClickListener(this)
        btn_login_commit.setOnClickListener(this)
        tv_login_to_register.setOnClickListener(this)
        phoneView.run {
            tv_login_tab_email.setOnClickListener(this@LoginActivity)
            tv_login_to_forgot.setOnClickListener(this@LoginActivity)
            tv_login_to_country.setOnClickListener(this@LoginActivity)
        }
        emailView.run {
            tv_login_tab_phone.setOnClickListener(this@LoginActivity)
            tv_login_email_to_forgot.setOnClickListener(this@LoginActivity)
        }
        ll_wechat_login.setOnClickListener(this)
        keyBoard?.setSoftKeyBoardShowListener(
            phoneView.et_login_phone,
            object : OnSoftKeyBoardListener {
                override fun onHideSoftKeyboard(myOldY: Int) {
                    L.e("onHideSoftKeyboard")
                    if (hideAnim.isRunning) {
                        hideAnim.end()
                    }
                    if (hideAnim1.isRunning) {
                        hideAnim1.end()
                    }
                    hideAnim.start()
                    hideAnim1.start()
                }

                override fun onShowSoftKeyboard(newY: Int, keyHeight: Int) {
                    if (hideAnim.isRunning) {
                        hideAnim.end()
                    }
                    if (hideAnim1.isRunning) {
                        hideAnim1.end()
                    }
                    showAnim.start()
                    showAnim1.start()
                }
            })
    }

    override fun onClick(v: View?) {
        when (v) {
            login_back -> {
                finish()
            }
            login -> {
                KeyBoardUtils.hideKeyBoard(this, v)
            }

            emailView.tv_login_tab_phone -> {
                changeLoginWay()
            }
            phoneView.tv_login_tab_email -> {
                changeLoginWay()
            }
            ll_wechat_login -> {
                WeChatLogin.getInstance().login(this, this)
            }
            btn_login_commit -> {
                when (loginType) {
                    true -> {
                        presenter.setPhoneData(
                            phoneView.et_login_phone.text.trim().toString(),
                            phoneView.et_login_pwd.text.trim().toString()
                        )
                        presenter.phoneCommit()
                    }
                    false -> {
                        presenter.setEmailData(
                            emailView.et_login_email.text.trim().toString(),
                            emailView.et_login_email_pwd.text.trim().toString()
                        )
                        presenter.emailCommit()
                    }
                }
            }
            tv_login_to_register -> {
                Intent(this, RegisterActivity::class.java).run {
                    putExtra(RegisterActivity.ACCOUNT_TYPE, loginType)
                    startActivity(this)
                }
            }
            phoneView.tv_login_to_forgot, emailView.tv_login_email_to_forgot -> {
                Intent(this, ForgotPasswordActivity::class.java).run {
                    putExtra(RegisterActivity.ACCOUNT_TYPE, loginType)
                    startActivity(this)
                }
            }
            phoneView.tv_login_to_country -> {
                startActivityForResult(Intent(this, CountryCodeActivity::class.java), 100)
            }
        }
    }

    override fun onSuccess(reqCode: String) {
        presenter.wechatLogin(reqCode)
    }

    override fun cancel() {
        T.show(getString(R.string.cancel_wechat))
    }

    override fun onFail(msg: String) {
        T.show(msg)
    }

    /**
     * 切换登录方式
     */
    private fun changeLoginWay() {
        loginType = !loginType
        when (loginType) {
            true -> {
                showPhoneLogin()
            }
            false -> {
                showEmailLogin()
            }
        }
    }

    private fun showPhoneLogin() {
        vp_login.currentItem = 0
        btn_login_commit.removeEditText(emailView.et_login_email)
        btn_login_commit.removeEditText(emailView.et_login_email_pwd)
        btn_login_commit.addEditText(
            phoneView.et_login_phone,
            phoneView.tv_login_phone_hint,
            presenter.getCountryCode()
        )
        btn_login_commit.addEditText(phoneView.et_login_pwd, phoneView.tv_login_pwd_hint)
    }

    private fun showEmailLogin() {
        vp_login.currentItem = 1
        btn_login_commit.removeEditText(phoneView.et_login_phone)
        btn_login_commit.removeEditText(phoneView.et_login_pwd)
        btn_login_commit.addEditText(
            emailView.et_login_email,
            emailView.tv_login_email_hint,
            "email"
        )
        btn_login_commit.addEditText(
            emailView.et_login_email_pwd,
            emailView.tv_login_email_pwd_hint
        )
    }

    override fun loginSuccess(user: User) {
        saveUser(user)
        T.show(getString(R.string.login_success))
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    override fun loginFail(msg: String) {
        T.show(msg)
    }

    override fun showCountryCode(countryName: String, countryCode: String) {
        phoneView.tv_login_to_country.text = "+$countryCode"
        btn_login_commit.changeType(phoneView.et_login_phone, countryCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100) {
            data?.let {
                it.getStringExtra(CommonField.COUNTRY_CODE)?.run {
                    L.e(this)
                    presenter.setCountryCode(this)
                }
            }
        }
    }

    override fun onDestroy() {
        keyBoard?.destroy()
        super.onDestroy()
    }

}
