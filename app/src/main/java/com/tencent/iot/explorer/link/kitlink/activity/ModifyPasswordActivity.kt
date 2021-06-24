package com.tencent.iot.explorer.link.kitlink.activity

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Handler
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import com.tencent.iot.explorer.link.App
import com.tencent.iot.explorer.link.ErrorMessage
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.T
import com.tencent.iot.explorer.link.core.auth.entity.User
import com.tencent.iot.explorer.link.core.auth.response.BaseResponse
import com.tencent.iot.explorer.link.kitlink.consts.CommonField
import com.tencent.iot.explorer.link.kitlink.util.AutomicUtils
import com.tencent.iot.explorer.link.mvp.IPresenter
import com.tencent.iot.explorer.link.mvp.presenter.LoginPresenter
import com.tencent.iot.explorer.link.mvp.presenter.ModifyPasswordPresenter
import com.tencent.iot.explorer.link.mvp.view.LoginView
import com.tencent.iot.explorer.link.mvp.view.ModifyPasswordView
import kotlinx.android.synthetic.main.activity_modify_password.*
import kotlinx.android.synthetic.main.activity_modify_password.btn_confirm_to_modify
import kotlinx.android.synthetic.main.activity_modify_phone.*
import kotlinx.android.synthetic.main.layout_modify_passwd_use_email.view.*
import kotlinx.android.synthetic.main.layout_modify_passwd_use_email.view.tv_get_verify_code
import kotlinx.android.synthetic.main.layout_modify_passwd_use_phone.view.*
import kotlinx.android.synthetic.main.layout_modify_passwd_use_phone.view.et_set_password
import kotlinx.android.synthetic.main.layout_modify_passwd_use_phone.view.et_verify_set_password
import kotlinx.android.synthetic.main.layout_modify_passwd_use_phone.view.iv_clear_password
import kotlinx.android.synthetic.main.layout_modify_passwd_use_phone.view.iv_clear_verify_password
import kotlinx.android.synthetic.main.layout_modify_passwd_use_phone.view.tv_set_password_hint
import kotlinx.android.synthetic.main.layout_modify_passwd_use_phone.view.tv_set_verify_password_hint
import kotlinx.android.synthetic.main.layout_verify_code_login.view.*
import kotlinx.android.synthetic.main.menu_back_layout.*

class ModifyPasswordActivity : PActivity(), ModifyPasswordView, View.OnClickListener {

    private lateinit var presenter: ModifyPasswordPresenter
    private lateinit var loginPresenter: LoginPresenter
    private lateinit var modifyPasswdUsePhoneView: View
    private lateinit var modifyPasswdUseEmailView: View
    private var handler: Handler = Handler()

    override fun getPresenter(): IPresenter? {
        return presenter
    }

    override fun getContentView(): Int {
        return R.layout.activity_modify_password
    }

    override fun onResume() {
        super.onResume()
        btn_confirm_to_modify.addEditText(
            modifyPasswdUsePhoneView.et_modify_passwd_byphone,
            modifyPasswdUsePhoneView.tv_phone_hint,
            loginPresenter.getCountryCode()
        )
        btn_confirm_to_modify.addEditText(
            modifyPasswdUsePhoneView.et_set_password,
            modifyPasswdUsePhoneView.tv_set_password_hint
        )
        btn_confirm_to_modify.addEditText(
            modifyPasswdUsePhoneView.et_verify_set_password,
            modifyPasswdUsePhoneView.tv_set_verify_password_hint
        )
    }

    override fun initView() {
        tv_title.text = getString(R.string.modify_password)
        initViewPager()
        presenter = ModifyPasswordPresenter(this)
        loginPresenter = LoginPresenter(loginView)
        modifyPasswdUsePhoneView.tv_country.text = loginPresenter.getCountry() + getString(R.string.conutry_code_num, loginPresenter.getCountryCode())
    }

    var loginView = object : LoginView {
        override fun loginSuccess(user: User) {}
        override fun loginFail(msg: String) {}
        override fun loginFail(response: BaseResponse) {}
        override fun sendVerifyCodeSuccess() {}
        override fun sendVerifyCodeFail(msg: ErrorMessage) {}
        override fun showCountryCode(countryName: String, countryCode: String) {
            modifyPasswdUsePhoneView.tv_country.text = countryName + getString(R.string.conutry_code_num, countryCode)
        }
    }

    override fun setListener() {
        iv_back.setOnClickListener { finish() }
        modifyPasswdUsePhoneView.tv_use_email_verify_code_to_modify.setOnClickListener(this)
        modifyPasswdUsePhoneView.tv_get_verify_code.setOnClickListener(this)
        modifyPasswdUsePhoneView.iv_country.setOnClickListener(this)
        modifyPasswdUsePhoneView.tv_country.setOnClickListener(this)

        modifyPasswdUseEmailView.tv_use_phone_verify_code_to_modify.setOnClickListener(this)
        modifyPasswdUseEmailView.tv_get_verify_code.setOnClickListener(this)

        btn_confirm_to_modify.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v) {
            modifyPasswdUsePhoneView.tv_use_email_verify_code_to_modify -> {
                showModifyPasswdByEmail()
            }
            modifyPasswdUseEmailView.tv_use_phone_verify_code_to_modify -> {
                showModifyPasswdByPhone()
            }
            modifyPasswdUsePhoneView.tv_get_verify_code -> {// 获取手机验证码
                val account = modifyPasswdUsePhoneView.et_modify_passwd_byphone.text.trim().toString()
                if (!TextUtils.isEmpty(account)) {
                    presenter.setPhone(account)
                    presenter.requestPhoneCode()
                    AutomicUtils.automicChangeStatus(this@ModifyPasswordActivity,
                        handler, modifyPasswdUsePhoneView.tv_get_verify_code, 60)
                } else {
                    T.show(getString(R.string.phone_empty))
                }
            }
            modifyPasswdUseEmailView.tv_get_verify_code -> {// 获取邮箱验证码
                val account = modifyPasswdUseEmailView.et_modify_passwd_byemail.text.trim().toString()
                if (!TextUtils.isEmpty(account)) {
                    presenter.setEmail(account)
                    presenter.requestEmailCode()

                    AutomicUtils.automicChangeStatus(this@ModifyPasswordActivity,
                        handler, modifyPasswdUseEmailView.tv_get_verify_code, 60)
                } else {
                    T.show(getString(R.string.email_empty))
                }
            }
            btn_confirm_to_modify -> {
                if (vp_modify_passwd.currentItem == 0) {// 手机验证码修改密码界面
                    val account = modifyPasswdUsePhoneView.et_modify_passwd_byphone.text.trim().toString()
                    val verifyCode = modifyPasswdUsePhoneView.et_phone_verifycode.text.trim().toString()
                    val passwd1 = modifyPasswdUsePhoneView.et_set_password.text.trim().toString()
                    val passwd2 = modifyPasswdUsePhoneView.et_verify_set_password.text.trim().toString()
                    if (!TextUtils.isEmpty(verifyCode)) {
                        if (passwd1 == passwd2) {
                            presenter.setPhone(account)
                            presenter.setVerifyCode(verifyCode)
                            presenter.setPassword(passwd2)
                            presenter.modifyPasswordByPhone()
                        } else {
                            T.show(getString(R.string.two_password_not_same))
                        }
                    } else {
                        T.show(getString(R.string.phone_verifycode_empty))
                    }

                } else {// 邮箱验证码修改密码界面
                    val account = modifyPasswdUseEmailView.et_modify_passwd_byemail.text.trim().toString()
                    val verifyCode = modifyPasswdUseEmailView.et_email_verifycode.text.trim().toString()
                    val passwd1 = modifyPasswdUseEmailView.et_set_password.text.trim().toString()
                    val passwd2 = modifyPasswdUseEmailView.et_verify_set_password.text.trim().toString()
                    if (!TextUtils.isEmpty(verifyCode)) {
                        if (passwd1 == passwd2) {
                            presenter.setEmail(account)
                            presenter.setVerifyCode(verifyCode)
                            presenter.setPassword(passwd2)
                            presenter.modifyPasswordByEmail()
                        } else {
                            T.show(getString(R.string.two_password_not_same))
                        }
                    } else {
                        T.show(getString(R.string.email_verifycode_empty))
                    }
                }
            }

            modifyPasswdUsePhoneView.tv_country, modifyPasswdUsePhoneView.iv_country -> {
                startActivityForResult(Intent(this, RegionActivity::class.java), 100)
            }
        }
    }

    private fun initViewPager() {
        modifyPasswdUsePhoneView = LayoutInflater.from(this).inflate(R.layout.layout_modify_passwd_use_phone, null)
        modifyPasswdUseEmailView = LayoutInflater.from(this).inflate(R.layout.layout_modify_passwd_use_email, null)
        modifyPasswdUsePhoneView.et_modify_passwd_byphone.addClearImage(modifyPasswdUsePhoneView.iv_clear_phone)
        modifyPasswdUsePhoneView.et_set_password.addClearImage(modifyPasswdUsePhoneView.iv_clear_password)
        modifyPasswdUsePhoneView.et_verify_set_password.addClearImage(modifyPasswdUsePhoneView.iv_clear_verify_password)

        modifyPasswdUseEmailView.et_modify_passwd_byemail.addClearImage(modifyPasswdUseEmailView.iv_clear_email)
        modifyPasswdUseEmailView.et_set_password.addClearImage(modifyPasswdUseEmailView.iv_clear_password)
        modifyPasswdUseEmailView.et_verify_set_password.addClearImage(modifyPasswdUseEmailView.iv_clear_verify_password)

        vp_modify_passwd.addViewToList(modifyPasswdUsePhoneView)
        vp_modify_passwd.addViewToList(modifyPasswdUseEmailView)
    }

    private fun showModifyPasswdByPhone() {
        vp_modify_passwd.setCurrentItem(0, true)
        btn_confirm_to_modify.removeEditText(modifyPasswdUseEmailView.et_modify_passwd_byemail)
        btn_confirm_to_modify.removeEditText(modifyPasswdUseEmailView.et_set_password)
        btn_confirm_to_modify.removeEditText(modifyPasswdUseEmailView.et_verify_set_password)
        btn_confirm_to_modify.addEditText(
            modifyPasswdUsePhoneView.et_modify_passwd_byphone,
            modifyPasswdUsePhoneView.tv_phone_hint,
            presenter.getCountryCode()
        )
        btn_confirm_to_modify.addEditText(
            modifyPasswdUsePhoneView.et_set_password,
            modifyPasswdUsePhoneView.tv_set_password_hint,
        "pwd"
        )
        btn_confirm_to_modify.addEditText(
            modifyPasswdUsePhoneView.et_verify_set_password,
            modifyPasswdUsePhoneView.tv_set_verify_password_hint
        )
    }

    private fun showModifyPasswdByEmail() {
        vp_modify_passwd.setCurrentItem(1, true)
        btn_confirm_to_modify.removeEditText(modifyPasswdUsePhoneView.et_modify_passwd_byphone)
        btn_confirm_to_modify.removeEditText(modifyPasswdUsePhoneView.et_set_password)
        btn_confirm_to_modify.removeEditText(modifyPasswdUsePhoneView.et_verify_set_password)
        btn_confirm_to_modify.addEditText(
            modifyPasswdUseEmailView.et_modify_passwd_byemail,
            modifyPasswdUseEmailView.tv_email_hint,
            "email"
        )
        btn_confirm_to_modify.addEditText(
            modifyPasswdUseEmailView.et_set_password,
            modifyPasswdUseEmailView.tv_set_password_hint,
            "pwd"
        )
        btn_confirm_to_modify.addEditText(
            modifyPasswdUseEmailView.et_verify_set_password,
            modifyPasswdUseEmailView.tv_set_verify_password_hint
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100) {
            data?.let {
                it.getStringExtra(CommonField.REGION_ID)?.run {
                    loginPresenter.setCountry(this)
                }
            }
        }
    }

    override fun showCountryCode(code: String, name: String) {}

    override fun modifyPasswdSuccess() {
        showModifySuccessDialog()
    }

    override fun modifyPasswdFail(msg: ErrorMessage) {
        T.show(msg.Message)
    }

    override fun sendVerifyCodeSuccess() {
        T.show(getString(R.string.send_verifycode_success))
    }

    override fun sendVerifyCodeFail(msg: ErrorMessage) {
        T.show(msg.Message)
    }

    private fun showModifySuccessDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage(R.string.modify_passwd_success)
            .setCancelable(false)
            .setPositiveButton(R.string.have_known,
                DialogInterface.OnClickListener { dialog, id ->
                    App.toLogin()
                    App.data.activityList.forEach {
                        if (it !is GuideActivity) {
                            it.finish()
                        }
                    }
                    App.data.activityList.clear()
                })
        builder.create()
        builder.show()
    }
}