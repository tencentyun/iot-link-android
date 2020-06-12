package com.tencent.iot.explorer.link.kitlink.activity

import android.content.Intent
import com.tencent.iot.explorer.link.kitlink.R
import com.tencent.iot.explorer.link.mvp.IPresenter
import com.tencent.iot.explorer.link.mvp.presenter.SetPasswordPresenter
import com.tencent.iot.explorer.link.mvp.view.SetPasswordView
import com.tencent.iot.explorer.link.util.T
import com.tencent.iot.explorer.link.util.keyboard.KeyBoardUtils
import kotlinx.android.synthetic.main.activity_set_password.*
import kotlinx.android.synthetic.main.menu_back_layout.*

/**
 * 设置密码界面
 */
class SetPasswordActivity : PActivity(), SetPasswordView {

    private lateinit var presenter: SetPasswordPresenter
    private var pwd = ""

    companion object {
        const val ACTION = "action"
        const val VERIFICATION_CODE = "verify_code"
        const val REGISTER_PHONE = 0
        const val REGISTER_EMAIL = 1
        const val RESET_PWD_PHONE = 2
        const val RESET_PWD_EMAIL = 3
        //绑定手机号
        const val BIND_PHONE = 4
    }

    override fun getContentView(): Int {
        return R.layout.activity_set_password
    }

    override fun getPresenter(): IPresenter? {
        return presenter
    }

    override fun initView() {
        iv_back.setColorFilter(resources.getColor(R.color.black_333333))
        presenter = SetPasswordPresenter(this)
        tv_title.text = getString(R.string.set_password)
        et_set_password.addClearImage(iv_clear_password)
        et_verify_set_password.addClearImage(iv_clear_verify_password)
        tv_set_password_commit.addEditText(et_set_password, null)
        tv_set_password_commit.addEditText(et_verify_set_password, null)
        /*tv_set_password_commit.addEditText(et_set_password, tv_set_password_hint)
        tv_set_password_commit.addEditText(et_verify_set_password, tv_set_verify_password_hint)*/
        getIntentData()
    }

    override fun setListener() {
        iv_back.setOnClickListener { finish() }
        tv_set_password_commit.setOnClickListener {
            pwd = et_set_password.text.trim().toString()
            presenter.setPassword(
                et_set_password.text.trim().toString(),
                et_verify_set_password.text.trim().toString()
            )
            presenter.commit()
        }
        set_password.setOnClickListener { KeyBoardUtils.hideKeyBoard(this, et_set_password) }
        iv_clear_password.setOnClickListener { et_set_password.setText("") }
    }

    private fun getIntentData() {
        intent?.let {
            when (it.getIntExtra(ACTION, -1)) {
                REGISTER_PHONE, RESET_PWD_PHONE -> {
                    presenter.setAction(it.getIntExtra(ACTION, -1))
                    presenter.setPhoneData(
                        it.getStringExtra(GetCodeActivity.COUNTRY_CODE)!!,
                        it.getStringExtra(GetCodeActivity.PHONE)!!,
                        it.getStringExtra(VERIFICATION_CODE)!!
                    )
                }
                REGISTER_EMAIL, RESET_PWD_EMAIL -> {
                    presenter.setAction(it.getIntExtra(ACTION, -1))
                    presenter.setEmailData(
                        it.getStringExtra(GetCodeActivity.EMAIL)!!,
                        it.getStringExtra(VERIFICATION_CODE)!!
                    )
                }
            }
        }
    }

    override fun phoneRegisterSuccess(phoneNumber: String) {
        T.show(getString(R.string.set_password_success))
        val intent = Intent(this, LoginActivity::class.java)
        intent.putExtra(GetCodeActivity.PHONE, phoneNumber)
        intent.putExtra("account", phoneNumber)
        intent.putExtra("password", pwd)
        startActivity(intent)
        finish()
    }

    override fun emailRegisterSuccess(email: String) {
        T.show(getString(R.string.set_password_success))
        val intent = Intent(this, LoginActivity::class.java)
        intent.putExtra(GetCodeActivity.EMAIL, email)
        intent.putExtra("account", email)
        intent.putExtra("password", pwd)
        startActivity(intent)
        finish()
    }

    override fun phoneResetSuccess(phoneNumber: String) {
        T.show(getString(R.string.set_password_success))
        val intent = Intent(this, LoginActivity::class.java)
        intent.putExtra(GetCodeActivity.PHONE, phoneNumber)
        startActivity(intent)
        finish()
    }

    override fun emailResetSuccess(email: String) {
        T.show(getString(R.string.set_password_success))
        val intent = Intent(this, LoginActivity::class.java)
        intent.putExtra(GetCodeActivity.EMAIL, email)
        startActivity(intent)
        finish()
    }

    override fun fail(message: String) {
        T.show(message)
    }

}
