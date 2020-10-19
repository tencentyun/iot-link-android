package com.tencent.iot.explorer.link.kitlink.util

import android.content.Context
import com.alibaba.fastjson.JSON
import com.tencent.iot.explorer.link.BuildConfig
import com.tencent.iot.explorer.link.kitlink.entity.WeatherInfo
import com.tencent.iot.explorer.link.core.utils.LocationUtil
import com.tencent.map.geolocation.TencentLocation
import com.tencent.map.geolocation.TencentLocationListener
import okhttp3.*
import java.io.IOException

object WeatherUtils {

    private var key = BuildConfig.WeatherKey
    private val okHttpClient = OkHttpClient()
    private var defaultLang = "zh"

    // 默认中文获取天气
    fun getWeatherInfo(lat: Double, lon: Double, callback: Callback) {
        getWeatherInfo(lat, lon, defaultLang, callback)
    }

    // 根据语言获取天气
    fun getWeatherInfo(lat: Double, lon: Double, lang: String, callback: Callback) {

        var url = "https://api.heweather.net/v7/weather/now?location=${lon},${lat}&key=$key&lang=$lang"
        val request = Request.Builder().url(url).get().build() //添加头部信息
        okHttpClient.newCall(request).enqueue(callback)
    }

    fun getLocalCurrentWeatherInfo(context: Context, lang: String, listener: OnWeatherListener) {
        LocationUtil.getCurrentLocation(context, object : TencentLocationListener {
            override fun onStatusUpdate(p0: String?, p1: Int, p2: String?) {}

            override fun onLocationChanged(p0: TencentLocation?, p1: Int, p2: String?) {
                if (p0 == null || p1 != 0) {
                    if (listener != null) {
                        listener.onWeatherFailed(p1)
                    }
                    return
                }
                getWeatherInfo(p0!!.latitude, p0!!.longitude, lang, object : Callback{
                    override fun onFailure(call: Call, e: IOException) {
                        if (listener != null) {
                            listener.onWeatherFailed(5)
                        }
                    }

                    override fun onResponse(call: Call, response: Response) {
                        if (response.body() != null) {
                            var respStr = response.body()!!.string()
                            var json = JSON.parseObject(respStr)
                            if (json.containsKey("now")) {
                                var nowWeatherInfo = json.getString("now")
                                var weatherInfo = JSON.parseObject(nowWeatherInfo, WeatherInfo::class.java)
                                if (weatherInfo != null && listener != null) {
                                    listener.onWeatherSuccess(weatherInfo)
                                    return
                                }
                            }
                        }


                        if (listener != null) {
                            listener.onWeatherFailed(6)
                        }
                    }

                })
            }

        })
    }


    fun getLocalCurrentWeatherInfo(context: Context, listener: OnWeatherListener) {
        getLocalCurrentWeatherInfo(context, defaultLang, listener)
    }

}