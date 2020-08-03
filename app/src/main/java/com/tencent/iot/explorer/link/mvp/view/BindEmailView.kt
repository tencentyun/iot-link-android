package com.tencent.iot.explorer.link.mvp.view

import com.tencent.iot.explorer.link.mvp.ParentView

interface BindEmailView : ParentView {
    fun bindSuccess(msg: String)
    fun bindFail(msg: String)
}