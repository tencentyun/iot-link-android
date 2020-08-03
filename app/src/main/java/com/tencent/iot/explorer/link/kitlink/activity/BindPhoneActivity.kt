package com.tencent.iot.explorer.link.kitlink.activity

import android.content.Intent
import android.view.View
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.kitlink.consts.CommonField
import com.tencent.iot.explorer.link.mvp.IPresenter
import com.tencent.iot.explorer.link.mvp.presenter.BindPhonePresenter
import com.tencent.iot.explorer.link.mvp.view.BindPhoneView
import com.tencent.iot.explorer.link.util.T
import kotlinx.android.synthetic.main.activity_bind_email.*
import kotlinx.android.synthetic.main.activity_bind_phone.*
import kotlinx.android.synthetic.main.activity_bind_phone.et_verify_set_password
import kotlinx.android.synthetic.main.activity_bind_phone.tv_get_verify_code
import kotlinx.android.synthetic.main.activity_modify_phone.*
import kotlinx.android.synthetic.main.menu_back_layout.*

class BindPhoneActivity : PActivity(), BindPhoneView, View.OnClickListener  {

    private lateinit var presenter: BindPhonePresenter

    override fun getPresenter(): IPresenter? {
        return presenter
    }

    override fun getContentView(): Int {
        return R.layout.activity_bind_phone
    }

    override fun initView() {
        iv_back.setColorFilter(resources.getColor(R.color.black_333333))
        tv_title.text = getString(R.string.bind_phone_number)
        et_bind_phone.addClearImage(iv_clear_bind_phone)
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
                T.show("选择国家")
                startActivityForResult(Intent(this, CountryCodeActivity::class.java), 100)
            }

            tv_get_verify_code -> {// 获取验证码
                T.show("获取验证码")
                val account = et_bind_phone.text.trim().toString()
                presenter.setPhone(account)
                presenter.requestPhoneCode()
            }

            btn_confirm_to_bind -> {// 绑定
                T.show("确认绑定")
                val account = et_bind_phone.text.trim().toString()
                val verifyCode = et_bind_phone_verifycode.text.trim().toString()
                val password = et_verify_set_password.text.trim().toString()
                presenter.setPhone(account)
                presenter.setVerifyCode(verifyCode)
                presenter.setPassword(password)
                presenter.bindPhone()
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
}