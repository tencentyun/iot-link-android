package com.tencent.iot.explorer.link.kitlink.activity

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.kitlink.consts.CommonField
import com.tencent.iot.explorer.link.mvp.IPresenter
import com.tencent.iot.explorer.link.mvp.presenter.ModifyPasswordPresenter
import com.tencent.iot.explorer.link.mvp.view.ModifyPasswordView
import com.tencent.iot.explorer.link.util.T
import kotlinx.android.synthetic.main.activity_modify_password.*
import kotlinx.android.synthetic.main.layout_modify_passwd_use_email.view.*
import kotlinx.android.synthetic.main.layout_modify_passwd_use_email.view.tv_get_verify_code
import kotlinx.android.synthetic.main.layout_modify_passwd_use_phone.*
import kotlinx.android.synthetic.main.layout_modify_passwd_use_phone.view.*
import kotlinx.android.synthetic.main.layout_modify_passwd_use_phone.view.et_set_password
import kotlinx.android.synthetic.main.layout_modify_passwd_use_phone.view.et_verify_set_password
import kotlinx.android.synthetic.main.layout_modify_passwd_use_phone.view.iv_clear_password
import kotlinx.android.synthetic.main.layout_modify_passwd_use_phone.view.iv_clear_verify_password
import kotlinx.android.synthetic.main.menu_back_layout.*
import kotlinx.android.synthetic.main.popup_common.view.*

class ModifyPasswordActivity : PActivity(), ModifyPasswordView, View.OnClickListener {

    private lateinit var presenter: ModifyPasswordPresenter
    private lateinit var modifyPasswdUsePhoneView: View
    private lateinit var modifyPasswdUseEmailView: View

    override fun getPresenter(): IPresenter? {
        return presenter
    }

    override fun getContentView(): Int {
        return R.layout.activity_modify_password
    }

    override fun initView() {
        iv_back.setColorFilter(resources.getColor(R.color.black_333333))
        tv_title.text = getString(R.string.modify_password)
        initViewPager()
        presenter = ModifyPasswordPresenter(this)
    }

    override fun setListener() {
        iv_back.setOnClickListener { finish() }
        modifyPasswdUsePhoneView.tv_use_email_verify_code_to_modify.setOnClickListener(this)
        modifyPasswdUsePhoneView.tv_get_verify_code.setOnClickListener(this)
        modifyPasswdUsePhoneView.iv_country.setOnClickListener(this)

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
                T.show("获取手机验证码")
                val account = modifyPasswdUsePhoneView.et_modify_passwd_byphone.text.trim().toString()
                presenter.setPhone(account)
                presenter.requestPhoneCode()
            }
            modifyPasswdUseEmailView.tv_get_verify_code -> {// 获取邮箱验证码
                T.show("获取邮箱验证码")
                val account = modifyPasswdUseEmailView.et_modify_passwd_byemail.text.trim().toString()
                presenter.setEmail(account)
                presenter.requestEmailCode()
            }
            btn_confirm_to_modify -> {
                if (vp_modify_passwd.currentItem == 0) {// 手机验证码修改密码界面
                    T.show("手机验证码修改密码界面")
                    val account = modifyPasswdUsePhoneView.et_modify_passwd_byphone.text.trim().toString()
                    val verifyCode = modifyPasswdUsePhoneView.et_phone_verifycode.text.trim().toString()
                    val passwd = modifyPasswdUsePhoneView.et_verify_set_password.text.trim().toString()
                    presenter.setPhone(account)
                    presenter.setVerifyCode(verifyCode)
                    presenter.setPassword(passwd)
                    presenter.modifyPasswordByPhone()
                } else {// 邮箱验证码修改密码界面
                    T.show("邮箱验证码修改密码界面")
                    val account = modifyPasswdUseEmailView.et_modify_passwd_byemail.text.trim().toString()
                    val verifyCode = modifyPasswdUseEmailView.et_email_verifycode.text.trim().toString()
                    val passwd = modifyPasswdUseEmailView.et_verify_set_password.text.trim().toString()
                    presenter.setEmail(account)
                    presenter.setVerifyCode(verifyCode)
                    presenter.setPassword(passwd)
                    presenter.modifyPasswordByEmail()
                }
            }

            modifyPasswdUsePhoneView.iv_country -> {
                T.show("选择国家")
                startActivityForResult(Intent(this, CountryCodeActivity::class.java), 100)
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
    }

    private fun showModifyPasswdByEmail() {
        vp_modify_passwd.setCurrentItem(1, true)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100) {
            data?.let {
                it.getStringExtra(CommonField.COUNTRY_CODE)?.run {
                    presenter.setCountryCode(this)
                }
            }
        }
    }

    override fun showCountryCode(code: String, name: String) {
        modifyPasswdUsePhoneView.tv_country.text = name
    }
}