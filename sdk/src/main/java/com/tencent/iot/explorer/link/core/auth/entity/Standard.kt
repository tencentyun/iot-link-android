package com.tencent.iot.explorer.link.core.auth.entity

/**
 * 面板主题
 */
class Standard {

    var theme = ""
    var bgImgId = ""
    var navBar = PanelNavBar()
    //面板数据
    var properties = arrayListOf<PanelProperty>()
    var timingProject = false

}
