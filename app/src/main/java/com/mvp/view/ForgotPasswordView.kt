package com.mvp.view

import com.mvp.ParentView

interface ForgotPasswordView : ParentView {

    fun agreement(isAgree: Boolean)

    fun unselectedAgreement()

    fun sendSmsCodeSuccess(phone: String, countryCode: String)

    fun sendEmailCodeSuccess(email: String)

    fun sendCodeFail(msg: String)

    fun showCountryCode(countryCode: String, countryName: String)

}