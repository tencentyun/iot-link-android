package com.tencent.iot.explorer.link.kitlink.activity

import android.content.Intent
import android.os.Handler
import android.text.TextUtils
import android.view.View
import com.tencent.iot.explorer.link.ErrorMessage
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.T
import com.tencent.iot.explorer.link.core.auth.entity.User
import com.tencent.iot.explorer.link.core.auth.response.BaseResponse
import com.tencent.iot.explorer.link.kitlink.consts.CommonField
import com.tencent.iot.explorer.link.kitlink.util.AutomicUtils
import com.tencent.iot.explorer.link.mvp.IPresenter
import com.tencent.iot.explorer.link.mvp.presenter.LoginPresenter
import com.tencent.iot.explorer.link.mvp.presenter.ModifyPhonePresenter
import com.tencent.iot.explorer.link.mvp.view.LoginView
import com.tencent.iot.explorer.link.mvp.view.ModifyPhoneView
import kotlinx.android.synthetic.main.activity_modify_phone.*
import kotlinx.android.synthetic.main.menu_back_layout.*

class ModifyPhoneActivity : PActivity(), ModifyPhoneView, View.OnClickListener  {

    private lateinit var presenter: ModifyPhonePresenter
    private lateinit var loginPresenter: LoginPresenter
    private var hanlder = Handler()

    override fun getPresenter(): IPresenter? {
        return presenter
    }

    override fun getContentView(): Int {
        return R.layout.activity_modify_phone
    }

    override fun onResume() {
        super.onResume()
//        btn_confirm_to_modify.btn2Click.add(tv_get_verify_code)
        btn_confirm_to_modify.addEditText(et_modify_phone, tv_modify_phone_hint, loginPresenter.getCountryCode())
    }

    override fun initView() {
        iv_back.setColorFilter(resources.getColor(R.color.black_15161A))
        tv_title.text = getString(R.string.modify_phone)
        et_modify_phone.addClearImage(iv_modify_phone_clear)
        presenter = ModifyPhonePresenter(this)
    }

    override fun setListener() {
        iv_back.setOnClickListener { finish() }
        iv_login_to_country.setOnClickListener(this)
        tv_login_to_country.setOnClickListener(this)
        tv_get_verify_code.setOnClickListener(this)
        btn_confirm_to_modify.setOnClickListener(this)
        loginPresenter = LoginPresenter(loginView)
        tv_login_to_country.text = loginPresenter.getCountry() + getString(R.string.conutry_code_num, loginPresenter.getCountryCode())
    }

    var loginView = object : LoginView {
        override fun loginSuccess(user: User) {}
        override fun loginFail(msg: String) {}
        override fun loginFail(response: BaseResponse) {}
        override fun sendVerifyCodeSuccess() {}
        override fun sendVerifyCodeFail(msg: ErrorMessage) {}
        override fun showCountryCode(countryName: String, countryCode: String) {
            tv_login_to_country.text = countryName + getString(R.string.conutry_code_num, countryCode)
        }
    }

    override fun onClick(v: View?) {
        when (v) {
            tv_login_to_country, iv_login_to_country -> {// 选择国家
                startActivityForResult(Intent(this, RegionActivity::class.java), 100)
            }
            tv_get_verify_code -> {// 获取验证码
                val account = et_modify_phone.text.trim().toString()
                if (!TextUtils.isEmpty(account)) {
                    presenter.setPhone(account)
                    presenter.requestPhoneCode()
                    AutomicUtils.automicChangeStatus(this, hanlder, tv_get_verify_code, 60)
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
                it.getStringExtra(CommonField.REGION_ID)?.run {
                    loginPresenter.setCountry(this)
                }
            }
        }
    }

    override fun showCountryCode(code: String, name: String) {}

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