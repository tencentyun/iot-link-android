package com.mvp.presenter

import com.mvp.model.ForgotPasswordModel
import com.mvp.ParentPresenter
import com.mvp.view.ForgotPasswordView

class ForgotPasswordPresenter(view: ForgotPasswordView) :
    ParentPresenter<ForgotPasswordModel, ForgotPasswordView>(view) {

    override fun getIModel(view: ForgotPasswordView): ForgotPasswordModel {
        return ForgotPasswordModel(view)
    }

    fun getCountryCode(): String {
        return model!!.getCountryCode()
    }

    fun setAgreement() {
        model?.setAgreement()
    }

    fun setPhone(phone: String) {
        model?.phone = phone
    }

    fun setEmail(email: String) {
        model?.email = email
    }

    fun setCountryCode(countryCode: String) {
        model?.setCountryCode(countryCode)
    }

    fun requestPhoneCode() {
        model?.let {
            if (!it.isAgreement()) return
            it.requestPhoneCode()
        }
    }

    fun requestEmailCode() {
        model?.let {
            if (!it.isAgreement()) return
            it.requestEmailCode()
        }
    }

}