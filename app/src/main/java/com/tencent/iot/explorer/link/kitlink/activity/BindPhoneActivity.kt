package com.tencent.iot.explorer.link.kitlink.activity

import android.content.Intent
import android.text.TextUtils
import android.view.View
import com.tencent.iot.explorer.link.App
import com.tencent.iot.explorer.link.ErrorMessage
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.kitlink.consts.CommonField
import com.tencent.iot.explorer.link.mvp.IPresenter
import com.tencent.iot.explorer.link.mvp.presenter.BindPhonePresenter
import com.tencent.iot.explorer.link.mvp.view.BindPhoneView
import com.tencent.iot.explorer.link.util.T
import kotlinx.android.synthetic.main.activity_bind_phone.*
import kotlinx.android.synthetic.main.activity_bind_phone.et_set_password
import kotlinx.android.synthetic.main.activity_bind_phone.et_verify_set_password
import kotlinx.android.synthetic.main.activity_bind_phone.iv_clear_password
import kotlinx.android.synthetic.main.activity_bind_phone.iv_clear_verify_password
import kotlinx.android.synthetic.main.activity_bind_phone.line2_set_pwd
import kotlinx.android.synthetic.main.activity_bind_phone.line_set_pwd
import kotlinx.android.synthetic.main.activity_bind_phone.tv_get_verify_code
import kotlinx.android.synthetic.main.menu_back_layout.*

class BindPhoneActivity : PActivity(), BindPhoneView, View.OnClickListener  {

    private lateinit var presenter: BindPhonePresenter

    override fun getPresenter(): IPresenter? {
        return presenter
    }

    override fun getContentView(): Int {
        return R.layout.activity_bind_phone
    }

    override fun onResume() {
        super.onResume()
        btn_confirm_to_bind.addEditText(et_bind_phone, tv_bind_phone_hint, presenter.getCountryCode())
        if (App.data.userInfo.HasPassword != "0") {//有密码则不显示设置密码的输入框
            hidePasswordInput()
            btn_confirm_to_bind.removeEditText(et_set_password)
            btn_confirm_to_bind.removeEditText(et_verify_set_password)
        } else {
            btn_confirm_to_bind.addEditText(et_set_password, tv_set_password_hint)
            btn_confirm_to_bind.addEditText(et_verify_set_password, tv_set_verify_password_hint)
        }
    }

    override fun initView() {
        iv_back.setColorFilter(resources.getColor(R.color.black_333333))
        tv_title.text = getString(R.string.bind_phone_number)
        et_bind_phone.addClearImage(iv_clear_bind_phone)
        et_set_password.addClearImage(iv_clear_password)
        et_verify_set_password.addClearImage(iv_clear_verify_password)
        presenter = BindPhonePresenter(this)
    }

    override fun setListener() {
        iv_back.setOnClickListener { finish() }
        iv_bind_to_country.setOnClickListener(this)
        tv_get_verify_code.setOnClickListener(this)
        btn_confirm_to_bind.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v) {
            iv_bind_to_country -> {// 选择国家
                startActivityForResult(Intent(this, CountryCodeActivity::class.java), 100)
            }

            tv_get_verify_code -> {// 获取验证码
                val account = et_bind_phone.text.trim().toString()
                if (!TextUtils.isEmpty(account)) {
                    presenter.setPhone(account)
                    presenter.requestPhoneCode()
                } else {
                    T.show(getString(R.string.phone_empty))
                }
            }

            btn_confirm_to_bind -> {// 绑定
                val account = et_bind_phone.text.trim().toString()
                val verifyCode = et_bind_phone_verifycode.text.trim().toString()
                val passwd1 = et_set_password.text.trim().toString()
                val passwd2 = et_verify_set_password.text.trim().toString()
                if (!TextUtils.isEmpty(verifyCode)) {
                    if (passwd1 == passwd2) {
                        presenter.setPhone(account)
                        presenter.setVerifyCode(verifyCode)
                        presenter.setPassword(passwd2)
                        presenter.bindPhone()
                    } else {
                        T.show(getString(R.string.two_password_not_same))
                    }
                } else {
                    T.show(getString(R.string.phone_verifycode_empty))
                }
            }
        }
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
        tv_bind_to_country.text = name
    }

    override fun bindSuccess() {
        T.show(getString(R.string.bind_success))
        finish()
    }

    override fun bindFail(msg: ErrorMessage) {
        T.show(msg.Message)
    }

    override fun sendVerifyCodeSuccess() {
        T.show(getString(R.string.send_verifycode_success))
    }

    override fun sendVerifyCodeFail(msg: ErrorMessage) {
        T.show(msg.Message)
    }

    private fun hidePasswordInput() {
        et_set_password.visibility = View.GONE
        iv_clear_password.visibility = View.GONE
        line_set_pwd.visibility = View.GONE
        et_verify_set_password.visibility = View.GONE
        iv_clear_verify_password.visibility = View.GONE
        line2_set_pwd.visibility = View.GONE
    }
}