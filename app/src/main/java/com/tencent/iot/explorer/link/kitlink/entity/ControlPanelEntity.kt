package com.tencent.iot.explorer.link.kitlink.entity

import android.text.TextUtils
import com.tencent.iot.explorer.link.core.auth.util.JsonManager

/**
 * 面板实体
 */
class ControlPanelEntity {

    var ProductId = ""
    var Config = ""

    var configEntity = ConfigEntity()

    fun parse(): ControlPanelEntity {
        JsonManager.parseJson(Config, ConfigEntity::class.java)?.let {
            configEntity = it
        }
        return this
    }

    /**
     * 是否显示导航 NavBar
     */
    fun getNavBar(): NavBar {
        return configEntity.Panel.standard.navBar
    }

    /**
     * 是否显示云端定时长按钮
     */
    fun isTimingProject(): Boolean {
        return configEntity.Panel.standard.timingProject
    }

    /**
     * 获得面板列表
     */
    fun getUIList(): ArrayList<Property> {
        return configEntity.Panel.standard.properties
    }
}

class ConfigEntity {
    var profile = Profile()
    var Global = Any()
    var Panel = PanelEntity()
    var ShortCut = Any()
    var WifiSoftAP = Any()
    var DeviceInfo = Any()
}

class Profile {
    var ProductId = ""
}

class PanelEntity {
    var type = "standard"
    var standard = Standard()
}

/**
 * 面板主题
 */
class Standard {
    var navBar = NavBar()
    var properties = arrayListOf<Property>()
    var timingProject = false
}

/**
 * 导航栏
 */
class NavBar {
    var visible = false
    var templateId = ""
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

    /**
     * 是否显示导航 timingProject
     */
    fun isShowTimingProject(): Boolean {
        return timingProject
    }
}

class Property {
    var big = false
    var id = ""
    var ui = UI()

    /**
     *  是否是大按钮
     */
    fun isBig(): Boolean {
        return ui.type == "btn-big"
    }
}

/**
 * 面板UI
 */
class UI {
    var type = ""
    var icon = ""
}

