package com.mvp.presenter

import com.kitlink.activity.SetPasswordActivity
import com.mvp.model.SetPasswordModel
import com.mvp.ParentPresenter
import com.mvp.view.SetPasswordView

/**
 * 设置密码
 */
class SetPasswordPresenter(view: SetPasswordView) :
    ParentPresenter<SetPasswordModel, SetPasswordView>(view) {

    override fun getIModel(view: SetPasswordView): SetPasswordModel {
        return SetPasswordModel(view)
    }

    fun setAction(action: Int) {
        model?.action = action
    }

    fun setPhoneData(countryCode: String, phoneNumber: String, verificationCode: String) {
        model?.setPhoneData(countryCode, phoneNumber, verificationCode)
    }

    fun setEmailData(email: String, verificationCode: String) {
        model?.setEmailData(email, verificationCode)
    }

    fun setPassword(password: String, verifyPassword: String) {
        model?.setPassword(password,verifyPassword)
    }

    fun commit() {
        model?.let {
            when (it.action) {
                SetPasswordActivity.REGISTER_PHONE -> {
                    it.registerPhonePassword()
                }
                SetPasswordActivity.REGISTER_EMAIL -> {
                    it.registerEmailPassword()
                }
                SetPasswordActivity.RESET_PWD_PHONE -> {
                    it.resetPhonePassword()
                }
                SetPasswordActivity.RESET_PWD_EMAIL -> {
                    it.resetEmailPassword()
                }
            }
        }
    }

}