package com.tencent.iot.explorer.link.kitlink.entity

import com.tencent.iot.explorer.link.App
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.core.utils.TemperatureUtils
import com.tencent.iot.explorer.link.kitlink.util.DateUtils
import com.tencent.iot.explorer.link.T
import java.lang.Exception

/**
 * 设备属性实体
 */
class DevicePropertyEntity {

    var id = ""
    var name = ""

    //面板类型:btn-big
    var big = false
    var type = ""
    var icon = ""

    //产品属性
    var numberEntity: PropertyEntity.NumberEntity? = null
    var stringEntity: PropertyEntity.StringEntity? = null
    var enumEntity: PropertyEntity.EnumEntity? = null
    var boolEntity: PropertyEntity.BoolEntity? = null
    var timestamp = false

    //数值类型：Int/Float
    var valueType = ""
    var desc = ""
    var required = true
    var mode = ""

    private var value: Any? = null
    var LastUpdate = 0L

    fun setValue(v: String) {
        value = if (v == "null" || v == "") {
            "0"
        } else {
            v
        }
    }

    fun getValueText(): String {
        return when {
            isEnumType() ->
                enumEntity!!.getValueText(getValue())
            isNumberType() -> {
                if (id == "Temperature") {
                    val unitOfTemperature = numberEntity!!.getNumUnit().trim()
                    if (unitOfTemperature.contains("摄氏") ||
                        unitOfTemperature.contains("℃") ||
                        unitOfTemperature.contains("oC")) {
                        if (App.data.userSetting.TemperatureUnit == "F") {
                            // 摄氏度转成华氏度
                            return TemperatureUtils.celsiusToFahrenheit("${getValue()}${numberEntity!!.getNumUnit()}") + "℉"
                        }
                    }

                    if (unitOfTemperature.contains("华氏") ||
                        unitOfTemperature.contains("℉") ||
                        unitOfTemperature.contains("oF")) {
                        if (App.data.userSetting.TemperatureUnit == "C") {
                            // 华氏度转成华氏度摄氏度
                            return TemperatureUtils.fahrenheitToCelsius("${getValue()}${numberEntity!!.getNumUnit()}") + "℃"
                        }
                    }
                }
                "${getValue()}${numberEntity!!.getNumUnit()}"
            }
            isTimestampType() -> getTimestampText(getValue())
            else -> getValue()
        }
    }

    fun getValue(): String {
        return when {
            isEnumType() -> value?.toString() ?: "0"
            isNumberType() -> value?.toString()?.toDouble()?.toInt()?.toString()
                ?: numberEntity!!.min.toDouble().toInt().toString()
            isTimestampType() -> value?.toString() ?: "0"
            else -> value?.toString() ?: ""
        }
    }

    private fun getTimestampText(value: String): String {
        try {
            return DateUtils.forString(value.toLong())
        } catch (e: Exception) {
            e.toString()
        }
        return T.getContext().getString(R.string.unset)//"未设置"
    }

    /**
     * 获取单位
     */
    fun getUnit(): String {
        return when {
            isNumberType() -> {
                if (numberEntity?.unit ?: "" == "oC") {
                    "℃"
                } else {
                    numberEntity!!.unit
                }
            }
            else -> ""
        }
    }

    fun isNumberType(): Boolean {
        if (valueType == "int" || valueType == "float") {
            return true
        }
        return false
    }

    fun isStringType(): Boolean {
        if (valueType == "string") {
            return true
        }
        return false
    }

    fun isTimestampType(): Boolean {
        if (valueType == "timestamp") {
            return true
        }
        return false
    }

    fun isEnumType(): Boolean {
        if (valueType == "enum") {
            return true
        }
        return false
    }

    fun isBoolType(): Boolean {
        if (valueType == "bool") {
            return true
        }
        return false
    }

    fun clone(): DevicePropertyEntity {
        DevicePropertyEntity().let {
            it.id = id
            it.name = name
            it.big = big
            it.type = type
            it.icon = icon
            it.numberEntity = numberEntity
            it.stringEntity = stringEntity
            it.enumEntity = enumEntity
            it.boolEntity = boolEntity
            it.boolEntity = boolEntity
            it.timestamp = timestamp
            it.valueType = valueType
            it.desc = desc
            it.required = required
            it.mode = mode
            it.value = value
            it.LastUpdate = LastUpdate
            return it
        }
    }
}