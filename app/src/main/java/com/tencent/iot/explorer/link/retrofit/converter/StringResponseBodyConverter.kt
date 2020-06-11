package com.tencent.iot.explorer.link.retrofit.converter

import java.io.IOException

import okhttp3.ResponseBody
import retrofit2.Converter

/**
 * 自定义ResponseBodyConverter
 */
class StringResponseBodyConverter : Converter<ResponseBody, String> {
    @Throws(IOException::class)
    override fun convert(responseBody: ResponseBody): String {
        return responseBody.string()
    }
}
