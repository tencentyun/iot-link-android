package com.tencent.iot.explorer.link.mvp.view

import com.tencent.iot.explorer.link.ErrorMessage
import com.tencent.iot.explorer.link.mvp.ParentView

interface ModifyPasswordView : ParentView {
    fun showCountryCode(code: String, name: String)
    fun modifyPasswdSuccess()
    fun modifyPasswdFail(msg: ErrorMessage)
    fun sendVerifyCodeSuccess()
    fun sendVerifyCodeFail(msg: ErrorMessage)
}