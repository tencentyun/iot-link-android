package com.mvp.view

import com.mvp.ParentView
import com.alibaba.fastjson.JSONObject
import com.kitlink.entity.NavBar

interface ControlPanelView : ParentView {

    fun showControlPanel(themeTag: String, navBar: NavBar?, timingProject: Boolean)

}