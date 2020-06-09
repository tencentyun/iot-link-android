package com.mvp.view

import com.mvp.ParentView

interface SetPasswordView : ParentView {

    fun phoneRegisterSuccess(phoneNumber: String)

    fun emailRegisterSuccess(email: String)

    fun phoneResetSuccess(phoneNumber: String)

    fun emailResetSuccess(email: String)

    fun fail(message: String)

}