package com.tencent.iot.explorer.link.mvp.view

import com.tencent.iot.explorer.link.mvp.ParentView

interface GetCodeView : ParentView {

    fun checkVerificationCodeFail(message: String)

    fun getCodeFail(message: String)

    fun phoneAction(countryCode: String, phoneNumber: String, verificationCode: String)

    fun bindPhoneSuccess()

    fun bindPhoneFail(msg: String)

    fun emailAction(email: String, verificationCode: String)

    fun lockResendShow(time: Int)

    fun unlock()

}