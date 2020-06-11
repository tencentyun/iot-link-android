package com.tencent.iot.explorer.link.mvp.view

import com.tencent.iot.explorer.link.mvp.ParentView

interface DeviceDetailView : ParentView {

    fun deleteSuccess()

    fun fail(message: String)

}