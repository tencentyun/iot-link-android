package com.tencent.iot.explorer.link.kitlink.util

import com.tencent.iot.explorer.link.BuildConfig
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request

object WeatherUtils {

    private var key = BuildConfig.WeatherKey
    private val okHttpClient = OkHttpClient()

    fun getWeatherInfo(lat: Double, lon: Double, callback: Callback?) {

        var url = "https://api.heweather.net/v7/weather/now?location=${lat},${lon}&key=$key"
//        var url = "https://api.heweather.net/v7/weather/now?location=116.41,39.92&key="
        val request = Request.Builder().url(url).get().build() //添加头部信息
        okHttpClient.newCall(request).enqueue(callback)
    }


}