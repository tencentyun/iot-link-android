package com.tencent.iot.explorer.link.kitlink.activity

import android.content.Intent
import android.view.View
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.kitlink.consts.CommonField
import com.tencent.iot.explorer.link.mvp.IPresenter
import com.tencent.iot.explorer.link.mvp.presenter.ModifyPhonePresenter
import com.tencent.iot.explorer.link.mvp.view.ModifyPhoneView
import com.tencent.iot.explorer.link.util.T
import kotlinx.android.synthetic.main.activity_modify_phone.*
import kotlinx.android.synthetic.main.activity_modify_phone.tv_get_verify_code
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
                T.show("获取验证码")
                val account = et_modify_phone.text.trim().toString()
                presenter.setPhone(account)
                presenter.requestPhoneCode()
            }
            btn_confirm_to_modify -> {// 确认修改
                T.show("确认修改")
                val account = et_modify_phone.text.trim().toString()
                val verifyCode = et_modify_phone_verifycode.text.trim().toString()
                presenter.setPhone(account)
                presenter.setVerifyCode(verifyCode)
                presenter.modifyPhone()
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
}