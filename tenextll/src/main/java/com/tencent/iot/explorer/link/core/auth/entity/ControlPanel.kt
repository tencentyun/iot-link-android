package com.tenext.auth.entity

/**
 * 控制面板UI
 */
class ControlPanel {

    var id = ""
    var name = ""

    //面板类型:btn-big
    var big = false
    var type = ""
    var icon = ""

    //数值类型：Int/Float
    var valueType = ""
    var desc = ""
    var required = false
    var mode = ""

    var define: ProductDefine? = null

    var value = ""
    var LastUpdate = 0L

    fun isNumberType(): Boolean {
        return when (valueType) {
            "int", "float" -> true
            else -> false
        }
    }

    fun isStringType(): Boolean {
        return when (valueType) {
            "string" -> true
            else -> false
        }
    }

    fun isTimestampType(): Boolean {
        return when (valueType) {
            "timestamp" -> true
            else -> false
        }
    }

    fun isEnumType(): Boolean {
        return when (valueType) {
            "enum" -> true
            else -> false
        }
    }

    fun isBoolType(): Boolean {
        return when (valueType) {
            "bool" -> true
            else -> false
        }
    }

}