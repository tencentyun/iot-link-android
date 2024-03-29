package com.tencent.iot.explorer.link.mvp.view

import com.tencent.iot.explorer.link.mvp.ParentView
import com.tencent.iot.explorer.link.core.auth.entity.NavBar

interface ControlPanelView : ParentView {

    fun showControlPanel(navBar: NavBar?, timingProject: Boolean)

    fun refreshDeviceStatus(isOnline: Boolean)

    fun refreshCategrayId(categoryId: Int)
}