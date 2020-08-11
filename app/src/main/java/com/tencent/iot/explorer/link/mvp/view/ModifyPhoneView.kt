package com.tencent.iot.explorer.link.mvp.view

import com.tencent.iot.explorer.link.ErrorMessage
import com.tencent.iot.explorer.link.mvp.ParentView

interface ModifyPhoneView : ParentView {
    fun showCountryCode(code: String, name: String)
    fun sendVerifyCodeFail(msg: ErrorMessage)
    fun sendVerifyCodeSuccess()
    fun updatePhoneFail(msg: ErrorMessage)
    fun updatePhoneSuccess()
}