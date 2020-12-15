package com.tencent.iot.explorer.link.kitlink.activity

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.core.log.L
import com.tencent.iot.explorer.link.kitlink.consts.SocketConstants
import com.tencent.iot.explorer.link.kitlink.consts.CommonField
import com.tencent.iot.explorer.link.mvp.IPresenter
import com.tencent.iot.explorer.link.mvp.presenter.ForgotPasswordPresenter
import com.tencent.iot.explorer.link.mvp.view.ForgotPasswordView
import com.tencent.iot.explorer.link.T
import com.tencent.iot.explorer.link.core.utils.KeyBoardUtils
import kotlinx.android.synthetic.main.activity_forgot_password.*
import kotlinx.android.synthetic.main.layout_email_forgot_pwd.view.*
import kotlinx.android.synthetic.main.layout_phone_forgot_pwd.view.*
import kotlinx.android.synthetic.main.menu_back_layout.*

/**
 * 忘记密码界面
 */
class ForgotPasswordActivity : PActivity(), ForgotPasswordView, View.OnClickListener {

    private lateinit var presenter: ForgotPasswordPresenter
    //
    private var forgotType = true

    private lateinit var phoneView: View
    private lateinit var emailView: View


    override fun getPresenter(): IPresenter? {
        return presenter
    }

    override fun getContentView(): Int {
        return R.layout.activity_forgot_password
    }

    override fun initView() {
        presenter = ForgotPasswordPresenter(this)
        btn_forgot_get_code.setForgotPasswordPresenter(presenter)
        iv_back.setColorFilter(resources.getColor(R.color.black_333333))
        tv_title.text = getString(R.string.find_bank_the_password)

        initViewPager()

        intent.getBooleanExtra(RegisterActivity.ACCOUNT_TYPE, true).let {
            forgotType = it
            when (forgotType) {
                true -> showPhoneForgot()
                false -> showEmailForgot()
            }
        }
    }

    private fun initViewPager() {
        phoneView = LayoutInflater.from(this).inflate(R.layout.layout_phone_forgot_pwd, null)
        emailView = LayoutInflater.from(this).inflate(R.layout.layout_email_forgot_pwd, null)
        phoneView.et_forgot_phone.addClearImage(phoneView.iv_forgot_phone_clear)
        emailView.et_forgot_email.addClearImage(emailView.iv_forgot_email_clear)
        vp_forgot.addViewToList(phoneView)
        vp_forgot.addViewToList(emailView)
    }

    override fun setListener() {
        iv_back.setOnClickListener { finish() }
        forgot_pwd.setOnClickListener { KeyBoardUtils.hideKeyBoard(this, forgot_pwd) }

        phoneView.tv_forgot_to_country.setOnClickListener(this)
        phoneView.iv_forgot_to_country.setOnClickListener(this)

        iv_forgot_agreement.setOnClickListener(this)
        tv_forgot_user_agreement.setOnClickListener(this)
        tv_forgot_privacy_policy.setOnClickListener(this)
        btn_forgot_get_code.setOnClickListener(this)

        phoneView.tv_forgot_to_email.setOnClickListener(this)
        emailView.tv_forgot_to_phone.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v) {
            phoneView.tv_forgot_to_country, phoneView.iv_forgot_to_country -> {
                startActivityForResult(Intent(this, CountryCodeActivity::class.java), 100)
            }
            iv_forgot_agreement -> {
                presenter.setAgreement()
            }
            btn_forgot_get_code -> {
                when (forgotType) {
                    true -> {
                        presenter.setPhone(phoneView.et_forgot_phone.text.trim().toString())
                        presenter.requestPhoneCode()
                    }
                    false -> {
                        presenter.setEmail(emailView.et_forgot_email.text.trim().toString())
                        presenter.requestEmailCode()
                    }
                }
            }
            phoneView.tv_forgot_to_email -> {
                forgotType = false
                showEmailForgot()
            }
            emailView.tv_forgot_to_phone -> {
                forgotType = true
                showPhoneForgot()
            }
            tv_forgot_user_agreement -> {
                val intent = Intent(this, WebActivity::class.java)
                intent.putExtra("title", getString(R.string.register_agree_2))
//                intent.putExtra("text", "user_agreementV1.0.htm")
                intent.putExtra("text", "https://docs.qq.com/doc/DY3ducUxmYkRUd2x2?pub=1&dver=2.1.0")
                startActivity(intent)
            }
            tv_forgot_privacy_policy -> {
                val intent = Intent(this, WebActivity::class.java)
                intent.putExtra("title", getString(R.string.register_agree_4))
                intent.putExtra("text", "https://privacy.qq.com")
                startActivity(intent)
            }
        }
    }

    private fun showPhoneForgot() {
        vp_forgot.setCurrentItem(0, true)
        btn_forgot_get_code.addEditText(
            phoneView.et_forgot_phone,
            phoneView.tv_forgot_phone_hint,
            presenter.getCountryCode()
        )
        btn_forgot_get_code.removeEditText(emailView.et_forgot_email)
    }

    private fun showEmailForgot() {
        vp_forgot.setCurrentItem(1, true)
        btn_forgot_get_code.removeEditText(phoneView.et_forgot_phone)
        btn_forgot_get_code.addEditText(
            emailView.et_forgot_email,
            emailView.tv_forgot_email_hint,
            "email"
        )
    }

    override fun agreement(isAgree: Boolean) {
        iv_forgot_agreement.setImageResource(
            if (isAgree) {
                R.mipmap.icon_selected
            } else {
                R.mipmap.icon_unselected
            }
        )
        btn_forgot_get_code.checkStatus()
    }

    override fun unselectedAgreement() {
        T.show(getString(R.string.toast_register_agreement))
    }

    override fun sendSmsCodeSuccess(phone: String, countryCode: String) {
        val intent = Intent(this, GetCodeActivity::class.java)
        intent.putExtra(GetCodeActivity.TYPE, SocketConstants.reset_pwd)
        intent.putExtra(GetCodeActivity.COUNTRY_CODE, countryCode)
        intent.putExtra(GetCodeActivity.PHONE, phone)
        intent.putExtra(SetPasswordActivity.ACTION, SetPasswordActivity.RESET_PWD_PHONE)
        startActivity(intent)
        finish()
    }

    override fun sendEmailCodeSuccess(email: String) {
        val intent = Intent(this, GetCodeActivity::class.java)
        intent.putExtra(GetCodeActivity.TYPE, SocketConstants.reset_pwd)
        intent.putExtra(GetCodeActivity.EMAIL, email)
        intent.putExtra(SetPasswordActivity.ACTION, SetPasswordActivity.RESET_PWD_EMAIL)
        startActivity(intent)
        finish()
    }

    override fun sendCodeFail(msg: String) {
        T.show(msg)
    }

    override fun showCountryCode(countryCode: String, countryName: String) {
        L.e("countryName=$countryName")
        phoneView.tv_forgot_to_country.text = countryName
        btn_forgot_get_code.changeType(phoneView.et_forgot_phone, countryCode)
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
}
