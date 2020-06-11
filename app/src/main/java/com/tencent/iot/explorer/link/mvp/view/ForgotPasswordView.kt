package com.tencent.iot.explorer.link.mvp.view

import com.tencent.iot.explorer.link.mvp.ParentView

interface ForgotPasswordView : ParentView {

    fun agreement(isAgree: Boolean)

    fun unselectedAgreement()

    fun sendSmsCodeSuccess(phone: String, countryCode: String)

    fun sendEmailCodeSuccess(email: String)

    fun sendCodeFail(msg: String)

    fun showCountryCode(countryCode: String, countryName: String)

}