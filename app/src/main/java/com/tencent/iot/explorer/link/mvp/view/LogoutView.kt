package com.tencent.iot.explorer.link.mvp.view

import com.tencent.iot.explorer.link.mvp.ParentView

interface LogoutView : ParentView {
    fun agreement(isAgree: Boolean)
    fun unselectedAgreement()
    fun cancelAccountSuccess()
    fun cancelAccountFail(msg: String)
}