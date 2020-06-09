package com.tencent.iot.explorer.link.core.auth.entity

import com.alibaba.fastjson.JSON
import com.tencent.iot.explorer.link.core.auth.util.JsonManager

/**
 * 产品属性
 */
class ProductProperty {

    var id = ""
    var name = ""
    var desc = ""
    //是否是必要属性
    var required = false
    var mode = ""
    var define = ""

    var productDefine: ProductDefine? = null

    /**
     * 解析define
     */
    fun parseDefine() {
        when (getType()) {
            "int", "float" -> {
                productDefine = JsonManager.parseJson(define, NumberDefine::class.java)
            }
            "enum" -> {
                productDefine = JsonManager.parseJson(define, EnumDefine::class.java)
            }
            "bool" -> {
                productDefine = JsonManager.parseJson(define, BoolDefine::class.java)
            }
            "string" -> {
                productDefine = JsonManager.parseJson(define, StringDefine::class.java)
            }
            "timestamp" -> {
                productDefine = object : ProductDefine() {
                    override fun getText(value: String): String {
                        return value
                    }
                }
                productDefine?.type = "timestamp"
            }
        }
    }

    fun isNumberType(): Boolean {
        return when (getType()) {
            "int", "float" -> true
            else -> false
        }
    }

    fun isStringType(): Boolean {
        return when (getType()) {
            "string" -> true
            else -> false
        }
    }

    fun isTimestampType(): Boolean {
        return when (getType()) {
            "timestamp" -> true
            else -> false
        }
    }

    fun isEnumType(): Boolean {
        return when (getType()) {
            "enum" -> true
            else -> false
        }
    }

    fun isBoolType(): Boolean {
        return when (getType()) {
            "bool" -> true
            else -> false
        }
    }

    /**
     * 获取type
     */
    fun getType(): String {
        productDefine?.let {
            return it.type
        }
        return JSON.parseObject(define)?.getString("type") ?: "timestamp"
    }

}