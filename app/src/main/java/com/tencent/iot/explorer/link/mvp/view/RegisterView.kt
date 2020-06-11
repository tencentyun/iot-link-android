package com.tencent.iot.explorer.link.mvp.view

import com.tencent.iot.explorer.link.mvp.ParentView

interface RegisterView : ParentView {

    fun sendSmsCodeSuccess()

    fun sendEmailCodeSuccess()

    fun sendCodeFail(msg: String)

    fun agreement(isAgree: Boolean)

    fun unselectedAgreement()

    fun showCountryCode(countryCode: String, countryName: String)

}