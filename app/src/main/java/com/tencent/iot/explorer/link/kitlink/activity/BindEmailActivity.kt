package com.tencent.iot.explorer.link.kitlink.activity

import android.os.Handler
import android.text.TextUtils
import android.view.View
import com.tencent.iot.explorer.link.App
import com.tencent.iot.explorer.link.ErrorMessage
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.mvp.IPresenter
import com.tencent.iot.explorer.link.mvp.presenter.BindEmailPresenter
import com.tencent.iot.explorer.link.mvp.view.BindEmailView
import com.tencent.iot.explorer.link.T
import com.tencent.iot.explorer.link.kitlink.util.AutomicUtils
import kotlinx.android.synthetic.main.activity_bind_email.*
import kotlinx.android.synthetic.main.activity_bind_email.tv_get_verify_code
import kotlinx.android.synthetic.main.activity_modify_email.*
import kotlinx.android.synthetic.main.menu_back_layout.*

class BindEmailActivity : PActivity(), BindEmailView, View.OnClickListener  {

    private lateinit var presenter: BindEmailPresenter
    private var hanlder = Handler()


    override fun getPresenter(): IPresenter? {
        return presenter
    }

    override fun getContentView(): Int {
        return R.layout.activity_bind_email
    }

    override fun initView() {
        presenter = BindEmailPresenter(this)
        iv_back.setColorFilter(resources.getColor(R.color.black_15161A))
        tv_title.text = getString(R.string.bind_email_number)
        et_bind_email.addClearImage(iv_clear_bind_email)
        et_set_password.addClearImage(iv_clear_password)
        et_verify_set_password.addClearImage(iv_clear_verify_password)
//        btn_bind_get_code.btn2Click.add(tv_get_verify_code)
        btn_bind_get_code.addEditText(et_bind_email, tv_bind_email_hint, "email")
        if (App.data.userInfo.HasPassword != "0") {//有密码则不显示设置密码的输入框
            hidePasswordInput()
            btn_bind_get_code.removeEditText(et_set_password)
            btn_bind_get_code.removeEditText(et_verify_set_password)
        } else {
            btn_bind_get_code.addEditText(et_set_password, tv_set_password_hint)
            btn_bind_get_code.addEditText(et_verify_set_password, tv_set_verify_password_hint)
        }

    }

    override fun setListener() {
        iv_back.setOnClickListener { finish() }
        tv_get_verify_code.setOnClickListener(this)
        btn_bind_get_code.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v) {
            tv_get_verify_code -> {// 获取验证码
                val account = et_bind_email.text.trim().toString()
                if (!TextUtils.isEmpty(account)) {
                    presenter.setEmail(account)
                    presenter.requestEmailVerifyCode()
                    AutomicUtils.automicChangeStatus(this, hanlder, tv_get_verify_code, 60)

                } else {
                    T.show(getString(R.string.email_empty))
                }
            }
            btn_bind_get_code -> {// 确认绑定
                val account = et_bind_email.text.trim().toString()
                val verifyCode = et_bind_email_verifycode.text.trim().toString()
                val passwd1 = et_set_password.text.trim().toString()
                val passwd2 = et_verify_set_password.text.trim().toString()
                if (!TextUtils.isEmpty(verifyCode)) {
                    if (passwd1 == passwd2) {
                        presenter.setEmail(account)
                        presenter.setVerifyCode(verifyCode)
                        presenter.setPassword(passwd2)
                        presenter.bindEmail()
                    } else {
                        T.show(getString(R.string.two_password_not_same))
                    }
                } else {
                    T.show(getString(R.string.email_verifycode_empty))
                }
            }
        }
    }

    private fun hidePasswordInput() {
        et_set_password.visibility = View.GONE
        iv_clear_password.visibility = View.GONE
        line_set_pwd.visibility = View.GONE
        et_verify_set_password.visibility = View.GONE
        iv_clear_verify_password.visibility = View.GONE
        line2_set_pwd.visibility = View.GONE
        tv_set_password_hint.visibility = View.GONE
        line_input_verifycode.visibility = View.GONE
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
}