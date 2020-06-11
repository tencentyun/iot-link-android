package com.tencent.iot.explorer.link.kitlink.activity

import com.tencent.iot.explorer.link.kitlink.App
import com.tencent.iot.explorer.link.kitlink.R
import com.tencent.iot.explorer.link.mvp.IPresenter
import com.tencent.iot.explorer.link.mvp.presenter.ResetPasswordPresenter
import com.tencent.iot.explorer.link.mvp.view.ResetPasswordView
import com.util.T
import com.util.keyboard.KeyBoardUtils
import com.tencent.iot.explorer.link.kitlink.activity.PActivity
import kotlinx.android.synthetic.main.activity_reset_password.*
import kotlinx.android.synthetic.main.menu_back_layout.*

/**
 * 通过旧密码修改密码
 */
class ResetPasswordActivity : PActivity(), ResetPasswordView {

    private lateinit var presenter: ResetPasswordPresenter

    override fun getContentView(): Int {
        return R.layout.activity_reset_password
    }

    override fun getPresenter(): IPresenter? {
        return presenter
    }

    override fun initView() {
        presenter = ResetPasswordPresenter(this)
        iv_back.setColorFilter(resources.getColor(R.color.black_333333))
        tv_title.text = getString(R.string.modify_password)

        et_reset_pwd.addClearImage(iv_clear_password)
        et_reset_old_pwd.addClearImage(iv_clear_old_password)
        et_reset_verify_pwd.addClearImage(iv_clear_verify_pwd)

        tv_reset_pwd_commit.addEditText(et_reset_old_pwd, tv_reset_old_pwd_hint)
        tv_reset_pwd_commit.addEditText(et_reset_pwd, tv_reset_pwd_hint)
        tv_reset_pwd_commit.addEditText(et_reset_verify_pwd, tv_reset_verify_pwd_hint)
    }

    override fun setListener() {
        iv_back.setOnClickListener { finish() }
        tv_reset_pwd_commit.setOnClickListener {
            val oldPwd = et_reset_old_pwd.text.trim().toString()
            val newPwd = et_reset_pwd.text.trim().toString()
            if (oldPwd == newPwd) {
                T.show(getString(R.string.new_password_equals_old))
                return@setOnClickListener
            }
            presenter.setPassword(
                oldPwd,
                newPwd,
                et_reset_verify_pwd.text.trim().toString()
            )
            presenter.commit()
        }
        reset_password.setOnClickListener {
            KeyBoardUtils.hideKeyBoard(
                this,
                et_reset_old_pwd
            )
        }
    }

    override fun verifyPwdFail() {
        T.show(getString(R.string.toast_password_not_verify))
    }

    override fun success() {
        saveUser(null)
        App.data.clear()
        jumpActivity(LoginActivity::class.java)
        finish()
    }

    override fun fail(message: String) {
        T.show(message)
    }
}
