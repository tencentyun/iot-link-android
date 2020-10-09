package com.tencent.iot.explorer.link.kitlink.util

import com.tencent.iot.explorer.link.kitlink.entity.WeatherInfo

interface OnWeatherListener {
    fun onWeatherSuccess(weatherInfo: WeatherInfo)
    // 0    定位成功
    // 1    网络问题引起的定位失败
    // 2    GPS, Wi-Fi 或基站错误引起的定位失败：
    //   2.1、用户的手机确实采集不到定位凭据，比如偏远地区比如地下车库电梯内等;
    //   2.2、开关跟权限问题，比如用户关闭了位置信息，关闭了Wi-Fi，未授予app定位权限等。
    // 4    无法将WGS84坐标转换成GCJ-02坐标时的定位失败
    // 5    获取天气信息失败
    // 6    未知错误
    // 404  未知原因引起的定位失败
    fun onWeatherFailed(reason: Int)
}