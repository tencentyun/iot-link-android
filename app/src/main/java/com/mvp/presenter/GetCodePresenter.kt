package com.mvp.presenter

import com.mvp.model.GetCodeModel
import com.mvp.ParentPresenter
import com.mvp.view.GetCodeView

class GetCodePresenter(view: GetCodeView) : ParentPresenter<GetCodeModel, GetCodeView>(view) {
    override fun getIModel(view: GetCodeView): GetCodeModel {
        return GetCodeModel(view)
    }

    fun setCommonData(type: String, action: Int) {
        model?.setCommonData(type, action)
    }

    fun setPhone(phone:String){
        model?.phone = phone
    }

    fun setEmail(email:String){
        model?.email = email
    }

    fun getAction():Int{
        return model?.getAction()!!
    }

    fun setCountryCode(countryCode: String) {
        model?.setCountryCode(countryCode)
    }

    fun setVerificationCode(verificationCode: String) {
        model?.setVerificationCode(verificationCode)
    }

    fun lockResend() {
        model?.lockResend()
    }

    fun resendCode() {
        model?.let {
            if (it.isLockTime()) return
            if (it.hasMobileNumber()) {
                it.resendMobileCode()
            } else {
                it.resendEmailCode()
            }
        }
    }

    fun next() {
        model?.let {
            if (it.hasMobileNumber()) {
                it.checkMobileCode()
            } else {
                it.checkEmailCode()
            }
        }
    }

}