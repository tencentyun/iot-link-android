package com.tencent.iot.explorer.link.core.auth.entity

import com.tencent.iot.explorer.link.core.auth.util.JsonManager

/**
 * 设备归属的产品
 */
class ProductEntity {

    var ProductId = ""
    var Name = ""
    var Description = ""
    var State = ""
    var DataTemplate = ""
    var AppTemplate = ""
    var NetType = ""
    var Services = arrayListOf<String>()
    var ProductType = 0
    var UpdateTime = 0L

    var myTemplate: Template? = null

    /**
     * 解析DataTemplate数据
     */
    fun parseTemplate(): Template? {
        myTemplate?.run {
            return this
        }
        JsonManager.parseJson(DataTemplate, Template::class.java)?.run {
            myTemplate = this
        }
        return myTemplate
    }

    class Template {
        var version = ""
        var profile = Profile()
        var properties = arrayListOf<ProductProperty>()
        var events = arrayListOf<ProductEvent>()
        var actions = Any()

        /**
         * 获得id对应的名称
         */
        fun getTemplateName(id: String): String {
            properties.forEach {
                if (it.id == id) {
                    return it.name
                }
            }
            return ""
        }
    }

    class Profile {
        var ProductId = ""
        var CategoryId = ""
    }

}