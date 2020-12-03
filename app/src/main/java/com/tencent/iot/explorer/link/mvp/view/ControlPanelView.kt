package com.tencent.iot.explorer.link.mvp.view

import com.tencent.iot.explorer.link.mvp.ParentView
import com.tencent.iot.explorer.link.core.auth.entity.NavBar
import com.tencent.iot.explorer.trtc.model.RoomKey

interface ControlPanelView : ParentView {

    fun showControlPanel(navBar: NavBar?, timingProject: Boolean)
}