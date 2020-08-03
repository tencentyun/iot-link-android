package com.tencent.iot.explorer.link.kitlink.activity

import android.view.View
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.mvp.IPresenter
import com.tencent.iot.explorer.link.mvp.presenter.BindEmailPresenter
import com.tencent.iot.explorer.link.mvp.view.BindEmailView
import com.tencent.iot.explorer.link.util.T
import kotlinx.android.synthetic.main.activity_bind_email.*
import kotlinx.android.synthetic.main.layout_verify_code_login.view.*
import kotlinx.android.synthetic.main.menu_back_layout.*

class BindEmailActivity : PActivity(), BindEmailView, View.OnClickListener  {

    private lateinit var presenter: BindEmailPresenter

    override fun getPresenter(): IPresenter? {
        return presenter
    }

    override fun getContentView(): Int {
        return R.layout.activity_bind_email
    }

    override fun initView() {
        presenter = BindEmailPresenter(this)
        iv_back.setColorFilter(resources.getColor(R.color.black_333333))
        tv_title.text = getString(R.string.bind_email_number)
        et_bind_email.addClearImage(iv_clear_bind_email)
        et_set_password.addClearImage(iv_clear_password)
        et_verify_set_password.addClearImage(iv_clear_verify_password)
    }

    override fun setListener() {
        iv_back.setOnClickListener { finish() }
        tv_get_verify_code.setOnClickListener(this)
        btn_bind_get_code.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v) {
            tv_get_verify_code -> {// 获取验证码
                T.show("获取验证码")
                val account = et_bind_email.text.trim().toString()
                presenter.setEmail(account)
                presenter.requestEmailVerifyCode()
            }
            btn_bind_get_code -> {// 确认绑定
                T.show("确认绑定")
                val account = et_bind_email.text.trim().toString()
                val verifyCode = et_bind_email_verifycode.text.trim().toString()
                val password = et_verify_set_password.text.trim().toString()
                presenter.setEmail(account)
                presenter.setVerifyCode(verifyCode)
                presenter.setPassword(password)
                presenter.bindEmail()
            }
        }
    }

    override fun bindSuccess(msg: String) {
        TODO("Not yet implemented")
    }

    override fun bindFail(msg: String) {
        TODO("Not yet implemented")
    }
}