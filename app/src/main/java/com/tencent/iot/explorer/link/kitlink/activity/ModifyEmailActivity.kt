package com.tencent.iot.explorer.link.kitlink.activity

import android.view.View
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.mvp.IPresenter
import com.tencent.iot.explorer.link.mvp.presenter.AccountAndSafetyPresenter
import com.tencent.iot.explorer.link.mvp.presenter.ModifyEmailPresenter
import com.tencent.iot.explorer.link.mvp.view.ModifyEmailView
import com.tencent.iot.explorer.link.util.T
import kotlinx.android.synthetic.main.activity_bind_email.*
import kotlinx.android.synthetic.main.activity_modify_email.*
import kotlinx.android.synthetic.main.activity_modify_email.tv_get_verify_code
import kotlinx.android.synthetic.main.menu_back_layout.*

class ModifyEmailActivity : PActivity(), ModifyEmailView, View.OnClickListener  {

    private lateinit var presenter: ModifyEmailPresenter

    override fun getPresenter(): IPresenter? {
        return presenter
    }

    override fun getContentView(): Int {
        return R.layout.activity_modify_email
    }

    override fun initView() {
        iv_back.setColorFilter(resources.getColor(R.color.black_333333))
        tv_title.text = getString(R.string.modify_email)
        et_modify_email.addClearImage(iv_modify_email_clear)
        presenter = ModifyEmailPresenter(this)
    }

    override fun setListener() {
        iv_back.setOnClickListener { finish() }
        btn_confirm_to_modify.setOnClickListener(this)
        tv_get_verify_code.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v) {
            tv_get_verify_code -> {
                T.show("获取验证码")
                val account = et_modify_email.text.trim().toString()
                presenter.setEmail(account)
                presenter.requestEmailVerifyCode()
            }
            btn_confirm_to_modify -> {
                T.show("确认修改")
                val account = et_modify_email.text.trim().toString()
                val verifyCode = et_modify_email_verifycode.text.trim().toString()
                presenter.setEmail(account)
                presenter.setVerifyCode(verifyCode)
                presenter.modifyEmail()
            }
        }
    }
}