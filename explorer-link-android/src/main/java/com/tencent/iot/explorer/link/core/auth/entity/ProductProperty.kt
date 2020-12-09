package com.tencent.iot.explorer.link.core.auth.entity

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.tencent.iot.explorer.link.core.auth.util.JsonManager
import com.tencent.iot.explorer.link.core.log.L

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

    fun getNumberEntity(): NumberEntity? {
        L.d("json=$define")
        return JsonManager.parseJson(define, NumberEntity::class.java)
    }

    fun getStringEntity(): StringEntity {
        L.d("json=$define")
        return JsonManager.parseJson(define, StringEntity::class.java)
    }

    fun getEnumEntity(): EnumEntity {
        L.d("json=$define")
        return JsonManager.parseJson(define, EnumEntity::class.java)
    }

    fun getBoolEntity(): BoolEntity {
        L.d("json=$define")
        return JsonManager.parseJson(define, BoolEntity::class.java)
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

    class NumberEntity {
        var type = ""
        var min = "0"
        var max = "100"
        var start = ""
        var step = ""
        var unit = ""

        fun getNumUnit(): String {
            return if (unit == "oC") {
                "℃"
            } else if (unit == "oF") {
                "℉"
            } else {
                unit
            }
        }
    }

    class StringEntity {
        var type = ""
        var min = "0"
        var max = "0"
    }

    class EnumEntity {
        var type = ""
        var mapping: JSONObject? = null

        fun getValueText(value: String): String {
            return mapping?.getString(value) ?: ""
        }

        fun parseList(): ArrayList<MappingEntity> {
            val list = arrayListOf<MappingEntity>()
            mapping?.keys?.forEachIndexed { _, k ->
                list.add(MappingEntity(mapping!!.getString(k), k))
            }
            return list
        }
    }

    class BoolEntity {
        var type = ""
        var mapping: JSONObject? = null

        fun getValueText(value: String): String {
            return mapping!!.getString(value)
        }
    }

    class MappingEntity(k: String, v: String) {
        var key = k
        var value = v
    }
}