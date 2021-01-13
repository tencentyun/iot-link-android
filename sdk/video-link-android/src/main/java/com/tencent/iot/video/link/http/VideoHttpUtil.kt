package com.tencent.iot.video.link.http

import android.text.TextUtils
import com.tencent.iot.video.link.util.JsonManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL

/**
 * HttpURLConnection请求工具
 */
object VideoHttpUtil {

    var EXPLORER_SERVICE = "iotexplorer" // explorer
    var VIDEO_SERVICE = "iotvideo" // video
    val REST_HOST_URL = ".tencentcloudapi.com"

    private fun success(listener: HttpCallBack, response: String) {
        CoroutineScope(Dispatchers.Main).launch {
            listener.onSuccess(response)
        }
    }

    private fun fail(listener: HttpCallBack, msg: String) {
        CoroutineScope(Dispatchers.Main).launch {
            listener.onError(msg)
        }
    }

    /**
     * post请求
     */
    fun post(url: String, params: Map<String, Any>, headerParams: Map<String, Any>, listener: HttpCallBack) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                (URL("https://$url").openConnection() as? HttpURLConnection)?.run {
                    requestMethod = "POST"
                    connectTimeout = 8000
                    readTimeout = 8000
                    doOutput = true
                    doInput = true
                    addRequestProperty("Host",
                        url
                    )
                    addRequestProperty("Content-Type", "application/json; charset=utf-8")

                    // 添加通用header参数
                    for ((key, value) in headerParams.entries) {
                        addRequestProperty(key, value.toString() + "")
                    }

                    outputStream?.run {
                        PrintWriter(this).run {
                            write(
                                JsonManager.toJson(params)
                            )
                            close()
                        }
                        flush()
                        close()
                    }
                    connect()
                    if (HttpURLConnection.HTTP_OK == responseCode) {
                        val reader = BufferedReader(InputStreamReader(inputStream))
                        val response = StringBuilder()
                        var line = reader.readLine()
                        while (!TextUtils.isEmpty(line)) {
                            response.append(line)
                            line = reader.readLine()
                        }
                        success(
                            listener,
                            response.toString()
                        )
                    } else {
                        fail(
                            listener,
                            "服务器出错：$url"
                        )
                    }
                }
            } catch (e: MalformedURLException) {
                fail(
                    listener,
                    e.message
                        ?: "请求URL不正确：$url"
                )
                e.printStackTrace()
            } catch (e: IOException) {
                fail(
                    listener,
                    e.message
                        ?: "数据传输时发生错误：$url"
                )
                e.printStackTrace()
            }
        }
    }

}

interface HttpCallBack {
    fun onSuccess(response: String)
    fun onError(error: String)
}