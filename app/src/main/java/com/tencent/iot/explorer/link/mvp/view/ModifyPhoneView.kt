package com.tencent.iot.explorer.link.mvp.view

import com.tencent.iot.explorer.link.mvp.ParentView

interface ModifyPhoneView : ParentView {
    fun showCountryCode(code: String, name: String)
}