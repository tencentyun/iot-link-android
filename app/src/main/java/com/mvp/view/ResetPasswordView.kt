package com.mvp.view

import com.mvp.ParentView

interface ResetPasswordView : ParentView {

    fun verifyPwdFail()
    fun success()
    fun fail(message: String)

}