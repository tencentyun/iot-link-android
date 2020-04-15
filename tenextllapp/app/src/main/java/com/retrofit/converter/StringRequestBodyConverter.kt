package com.retrofit.converter

import android.util.Log

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
        Log.e("lurs", "请求数据json：$s")
        return RequestBody.create(MEDIA_TYPE, s)
    }

    companion object {

        private val MEDIA_TYPE = MediaType.parse("application/json; charset=UTF-8")
    }
}
