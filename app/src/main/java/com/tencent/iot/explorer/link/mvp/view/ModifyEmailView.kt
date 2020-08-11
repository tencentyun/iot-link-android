package com.tencent.iot.explorer.link.mvp.view

import com.tencent.iot.explorer.link.ErrorMessage
import com.tencent.iot.explorer.link.mvp.ParentView

interface ModifyEmailView : ParentView {
    fun sendVerifyCodeFail(msg: ErrorMessage)
    fun sendVerifyCodeSuccess()
    fun updateEmailFail(msg: ErrorMessage)
    fun updateEmailSuccess()
}