package com.tencent.iot.explorer.link.mvp.presenter

import com.tencent.iot.explorer.link.mvp.model.ForgotPasswordModel
import com.tencent.iot.explorer.link.mvp.ParentPresenter
import com.tencent.iot.explorer.link.mvp.view.ForgotPasswordView

class ForgotPasswordPresenter(view: ForgotPasswordView) :
    ParentPresenter<ForgotPasswordModel, ForgotPasswordView>(view) {

    override fun getIModel(view: ForgotPasswordView): ForgotPasswordModel {
        return ForgotPasswordModel(view)
    }

    fun getCountryName(): String {
        return model!!.getCountryName()
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