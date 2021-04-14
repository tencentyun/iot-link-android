package com.tencent.iot.explorer.link.kitlink.activity

import android.os.Handler
import android.text.TextUtils
import android.view.View
import com.tencent.iot.explorer.link.ErrorMessage
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.mvp.IPresenter
import com.tencent.iot.explorer.link.mvp.presenter.ModifyEmailPresenter
import com.tencent.iot.explorer.link.mvp.view.ModifyEmailView
import com.tencent.iot.explorer.link.T
import com.tencent.iot.explorer.link.kitlink.util.AutomicUtils
import kotlinx.android.synthetic.main.activity_modify_email.*
import kotlinx.android.synthetic.main.activity_modify_email.btn_confirm_to_modify
import kotlinx.android.synthetic.main.activity_modify_email.tv_get_verify_code
import kotlinx.android.synthetic.main.activity_modify_phone.*
import kotlinx.android.synthetic.main.menu_back_layout.*

class ModifyEmailActivity : PActivity(), ModifyEmailView, View.OnClickListener  {

    private lateinit var presenter: ModifyEmailPresenter
    private var hanlder = Handler()

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
//        btn_confirm_to_modify.addEditText(
//            et_modify_email,
//            tv_modify_email_hint,
//            "email"
//        )
    }

    override fun setListener() {
        iv_back.setOnClickListener { finish() }
        btn_confirm_to_modify.setOnClickListener(this)
        tv_get_verify_code.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v) {
            tv_get_verify_code -> {
                val account = et_modify_email.text.trim().toString()
                if (!TextUtils.isEmpty(account)) {
                    presenter.setEmail(account)
                    presenter.requestEmailVerifyCode()
                    AutomicUtils.automicChangeStatus(this, hanlder, tv_get_verify_code, 60)

                } else {
                    T.show(getString(R.string.email_empty))
                }
            }
            btn_confirm_to_modify -> {
                val account = et_modify_email.text.trim().toString()
                val verifyCode = et_modify_email_verifycode.text.trim().toString()
                presenter.setEmail(account)
                presenter.setVerifyCode(verifyCode)
                if (!TextUtils.isEmpty(verifyCode)) {
                    presenter.modifyEmail()
                } else {
                    T.show(getString(R.string.email_verifycode_empty))
                }
            }
        }
    }

    override fun sendVerifyCodeFail(msg: ErrorMessage) {
        T.show(msg.Message)
    }

    override fun sendVerifyCodeSuccess() {
        T.show(getString(R.string.send_verifycode_success))
    }

    override fun updateEmailFail(msg: ErrorMessage) {
        T.show(msg.Message)
    }

    override fun updateEmailSuccess() {
        T.show(getString(R.string.update_email_success))
        finish()
    }
}