package com.tencent.iot.explorer.link.mvp.view

import com.tencent.iot.explorer.link.mvp.ParentView
import com.tencent.iot.explorer.link.kitlink.entity.NavBar

interface ControlPanelView : ParentView {

    fun showControlPanel(themeTag: String, navBar: NavBar?, timingProject: Boolean)

}