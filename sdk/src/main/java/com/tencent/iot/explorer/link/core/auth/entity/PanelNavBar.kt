package com.tencent.iot.explorer.link.core.auth.entity

import android.text.TextUtils


/**
 * 导航栏
 */
class PanelNavBar {

    var visible = false
    var templateId = ""
    //是否显示导航 timingProject
    var timingProject = false

    /**
     * 是否显示导航 NavBar
     */
    fun isShowNavBar(): Boolean {
        return visible && (!TextUtils.isEmpty(templateId) || timingProject)
    }

    /**
     * 是否显示导航 templateId
     */
    fun isShowTemplate(): Boolean {
        return !TextUtils.isEmpty(templateId)
    }

}