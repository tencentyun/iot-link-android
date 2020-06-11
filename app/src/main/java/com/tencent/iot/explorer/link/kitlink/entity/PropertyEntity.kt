package com.tencent.iot.explorer.link.kitlink.entity

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.tencent.iot.explorer.link.kitlink.util.JsonManager
import com.util.L

/**
 * 产品属性
 */
class PropertyEntity {

    var id = ""
    var name = ""
    var desc = ""
    var required = true
    var mode = ""
    var define: JSONObject? = null

    fun getType(): String {
        define?.let {
            return it.getString("type")
        }
        return ""
    }

    fun isNumberType(): Boolean {
        if (getType() == "int" || getType() == "float") {
            return true
        }
        return false
    }

    fun isStringType(): Boolean {
        if (getType() == "string") {
            return true
        }
        return false
    }

    fun isTimestampType(): Boolean {
        if (getType() == "timestamp") {
            return true
        }
        return false
    }

    fun isEnumType(): Boolean {
        if (getType() == "enum") {
            return true
        }
        return false
    }

    fun isBoolType(): Boolean {
        if (getType() == "bool") {
            return true
        }
        return false
    }

    fun getNumberEntity(): NumberEntity? {
        val json = JSON.toJSONString(define)
        L.d("json=$json")
        return JsonManager.parseJson(json, NumberEntity::class.java)
    }

    fun getStringEntity(): StringEntity {
        val json = JSON.toJSONString(define)
        L.d("json=$json")
        return JsonManager.parseJson(json, StringEntity::class.java)
    }

    fun getEnumEntity(): EnumEntity {
        val json = JSON.toJSONString(define)
        L.d("json=$json")
        return JsonManager.parseJson(json, EnumEntity::class.java)
    }

    fun getBoolEntity(): BoolEntity {
        val json = JSON.toJSONString(define)
        L.d("json=$json")
        return JsonManager.parseJson(json, BoolEntity::class.java)
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