package com.tencent.iot.explorer.link.kitlink.activity

import android.content.Intent
import android.text.TextUtils
import android.view.View
import com.tencent.iot.explorer.link.ErrorMessage
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.kitlink.consts.CommonField
import com.tencent.iot.explorer.link.mvp.IPresenter
import com.tencent.iot.explorer.link.mvp.presenter.ModifyPhonePresenter
import com.tencent.iot.explorer.link.mvp.view.ModifyPhoneView
import com.tencent.iot.explorer.link.util.T
import kotlinx.android.synthetic.main.activity_modify_phone.*
import kotlinx.android.synthetic.main.activity_modify_phone.tv_get_verify_code
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.android.synthetic.main.layout_phone_register.view.*
import kotlinx.android.synthetic.main.menu_back_layout.*

class ModifyPhoneActivity : PActivity(), ModifyPhoneView, View.OnClickListener  {

    private lateinit var presenter: ModifyPhonePresenter

    override fun getPresenter(): IPresenter? {
        return presenter
    }

    override fun getContentView(): Int {
        return R.layout.activity_modify_phone
    }

    override fun initView() {
        iv_back.setColorFilter(resources.getColor(R.color.black_333333))
        tv_title.text = getString(R.string.modify_phone)
        et_modify_phone.addClearImage(iv_modify_phone_clear)
        presenter = ModifyPhonePresenter(this)
        btn_confirm_to_modify.addEditText(
            et_modify_phone,
            tv_modify_phone_hint,
            presenter.getCountryCode()
        )
    }

    override fun setListener() {
        iv_back.setOnClickListener { finish() }
        iv_login_to_country.setOnClickListener(this)
        tv_get_verify_code.setOnClickListener(this)
        btn_confirm_to_modify.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v) {
            iv_login_to_country -> {// 选择国家
                startActivityForResult(Intent(this, CountryCodeActivity::class.java), 100)
            }
            tv_get_verify_code -> {// 获取验证码
                val account = et_modify_phone.text.trim().toString()
                if (!TextUtils.isEmpty(account)) {
                    presenter.setPhone(account)
                    presenter.requestPhoneCode()
                } else {
                    T.show(getString(R.string.phone_empty))
                }
            }
            btn_confirm_to_modify -> {// 确认修改
                val account = et_modify_phone.text.trim().toString()
                val verifyCode = et_modify_phone_verifycode.text.trim().toString()
                presenter.setPhone(account)
                presenter.setVerifyCode(verifyCode)
                if (!TextUtils.isEmpty(verifyCode)) {
                    presenter.modifyPhone()
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
        tv_login_to_country.text = name
    }

    override fun sendVerifyCodeFail(msg: ErrorMessage) {
        T.show(msg.Message)
    }

    override fun sendVerifyCodeSuccess() {
        T.show(getString(R.string.send_verifycode_success))
    }

    override fun updatePhoneFail(msg: ErrorMessage) {
        T.show(msg.Message)
    }

    override fun updatePhoneSuccess() {
        T.show(getString(R.string.update_phone_success))
        finish()
    }
}