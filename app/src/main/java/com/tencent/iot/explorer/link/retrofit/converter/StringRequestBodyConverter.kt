package com.tencent.iot.explorer.link.retrofit.converter

import com.tencent.iot.explorer.link.core.log.L

import okhttp3.MediaType
import okhttp3.RequestBody
import retrofit2.Converter

import java.io.IOException

/**
 * 自定义RequestBodyConverter
 */
class StringRequestBodyConverter : Converter<String, RequestBody> {

    @Throws(IOException::class)
    override fun convert(s: String): RequestBody {
        L.e("请求数据json：$s")
        return RequestBody.create(MEDIA_TYPE, s)
    }

    companion object {

        private val MEDIA_TYPE = MediaType.parse("application/json; charset=UTF-8")
    }
}
