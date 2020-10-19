package com.tencent.iot.explorer.link.kitlink.entity

import com.tencent.iot.explorer.link.core.auth.util.JsonManager

/**
 * 设备产品信息实体
 */
class DeviceProductEntity {

    var ProductId = ""
    var Name = ""
    var Description = ""
    var State = ""
    var DataTemplate = ""
    var AppTemplate = ""
    var UpdateTime = 0L

    var myTemplate: Template? = null

    /**
     * 解析DataTemplate数据
     */
    fun parseTemplate(): Template? {
        JsonManager.parseJson(DataTemplate, Template::class.java)?.run {
            myTemplate = this
        }
        return myTemplate
    }

    class Template {
        var version = ""
        var profile = Profile()
        var properties = arrayListOf<PropertyEntity>()
        var events = Any()
        var actions = Any()


    }

    class Profile {
        var ProductId = ""
        var CategoryId = ""
    }
}

