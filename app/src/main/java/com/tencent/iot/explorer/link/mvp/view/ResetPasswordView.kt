package com.tencent.iot.explorer.link.mvp.view

import com.tencent.iot.explorer.link.mvp.ParentView

interface ResetPasswordView : ParentView {

    fun verifyPwdFail()
    fun success()
    fun fail(message: String)

}