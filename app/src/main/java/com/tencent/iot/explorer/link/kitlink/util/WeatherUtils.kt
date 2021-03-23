package com.tencent.iot.explorer.link.kitlink.util

import android.content.Context
import android.util.Log
import com.alibaba.fastjson.JSON
import com.tencent.iot.explorer.link.BuildConfig
import com.tencent.iot.explorer.link.T
import com.tencent.iot.explorer.link.kitlink.entity.WeatherInfo
import com.tencent.iot.explorer.link.core.utils.LocationUtil
import com.tencent.iot.explorer.link.kitlink.consts.CommonField
import com.tencent.iot.explorer.link.kitlink.entity.CityInfo
import com.tencent.map.geolocation.TencentLocation
import com.tencent.map.geolocation.TencentLocationListener
import okhttp3.*
import java.io.IOException

object WeatherUtils {

    private var key = BuildConfig.WeatherKey
    private val okHttpClient = OkHttpClient()
    var defaultLang = "zh"

    fun getWeatherInfo(cityInfo: CityInfo, listener: OnWeatherListener) {
        var openSourcePrefix = ""
        if (!T.getContext().applicationInfo.packageName.equals(CommonField.PUBLISH_TAG)) {
            openSourcePrefix = "dev"
        }
        var url = "https://${openSourcePrefix}api.heweather.net/v7/weather/now?location=${cityInfo.lon},${cityInfo.lat}&key=$key&lang=$defaultLang"
        val request = Request.Builder().url(url).get().build() //添加头部信息
        okHttpClient.newCall(request).enqueue(object : Callback{
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
                            weatherInfo.cityInfo = cityInfo
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

    // 根据经纬度获取城市信息
    fun getCityInfoByLoacation(lat: Double, lon: Double, callback: Callback) {
        var url = "https://geoapi.qweather.com/v2/city/lookup?location=${lon},${lat}&key=$key&lang=$defaultLang"
        val request = Request.Builder().url(url).get().build() //添加头部信息
        okHttpClient.newCall(request).enqueue(callback)
    }

    fun getLocalCurrentWeatherInfo(context: Context, listener: OnWeatherListener) {
        LocationUtil.getCurrentLocation(context, object : TencentLocationListener {
            override fun onStatusUpdate(p0: String?, p1: Int, p2: String?) {}

            override fun onLocationChanged(p0: TencentLocation?, p1: Int, p2: String?) {
                if (p0 == null || p1 != 0) {
                    if (listener != null) {
                        listener.onWeatherFailed(p1)
                    }
                    return
                }

                var cityInfo = CityInfo()
                cityInfo.lat = p0.latitude.toString()
                cityInfo.lon = p0.longitude.toString()
                cityInfo.name = p0.district
                getWeatherInfo(cityInfo, listener)
            }

        })
    }

    fun getWeatherInfoByLocation(lat: Double, lon: Double, listener: OnWeatherListener) {
        getCityInfoByLoacation(lat, lon, object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                if (listener != null) {
                    listener.onWeatherFailed(1)
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.body() != null) {
                    var respStr = response.body()!!.string()
                    var json = JSON.parseObject(respStr)
                    if (json.containsKey("location")) {
                        var jsonArr = json.getJSONArray("location")
                        if (jsonArr != null && jsonArr.size > 0) {
                            var cityInfoJson = jsonArr.get(0).toString()
                            var cityInfo = JSON.parseObject(cityInfoJson, CityInfo::class.java)
                            getWeatherInfo(cityInfo, listener)
                            return
                        }
                    }
                }

                listener.onWeatherFailed(6)
            }
        })
    }

}