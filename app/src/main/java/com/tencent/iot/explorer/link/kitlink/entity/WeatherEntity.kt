package com.tencent.iot.explorer.link.kitlink.entity

import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.T

class WeatherEntity {

    var number = "20"
    var unit = "℃"
    var city = T.getContext().getString(R.string.city_shenzhen) //"深圳"
    var weather = T.getContext().getString(R.string.default_weather) //"多云转晴"

}