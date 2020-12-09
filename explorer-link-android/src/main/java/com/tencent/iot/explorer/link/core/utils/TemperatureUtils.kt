package com.tencent.iot.explorer.link.core.utils

import java.text.DecimalFormat

object TemperatureUtils {

    /**
     * 摄氏度转华氏度
     * 华氏 = 摄氏 * 1.8 + 32
     */
    fun celsiusToFahrenheit(value: String) : String {
        val format = DecimalFormat("0.#")
        val retVal : Float
        var temperature = value
        temperature = temperature.replace(Regex(" +"),"")
        if (temperature.contains("摄氏")) {
            temperature = temperature.substring(0, temperature.indexOf("摄氏"))
        } else if (temperature.contains("℃")) {
            temperature = temperature.substring(0, temperature.indexOf("℃"))
        }
        retVal = if (temperature.matches(Regex("-?[0-9]+.?[0-9]+"))) {
            val temperatureValue = temperature.toFloat()
            format.format(temperatureValue * 1.8 + 32).toFloat()
        } else {
            0.0f
        }
        return retVal.toString()
    }

    /**
     * 华氏度转摄氏度
     * 摄氏 = (华氏 - 32)/1.8
     */
    fun fahrenheitToCelsius(value: String) : String {
        val format = DecimalFormat("0.#")
        val retVal : Float
        var temperature = value
        temperature = temperature.replace(Regex(" +"),"")
        if (temperature.contains("华氏")) {
            temperature = temperature.substring(0, temperature.indexOf("华氏"))
        } else if (temperature.contains("℉")) {
            temperature = temperature.substring(0, temperature.indexOf("℉"))
        }
        retVal = if (temperature.matches(Regex("-?[0-9]+.?[0-9]+"))) {
            val temperatureValue = temperature.toFloat()
            format.format((temperatureValue - 32) / 1.8).toFloat()
        } else {
            0.0f
        }
        return retVal.toString()
    }
}