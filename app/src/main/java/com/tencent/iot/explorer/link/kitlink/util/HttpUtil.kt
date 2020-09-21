package com.tencent.iot.explorer.link.kitlink.util

import android.text.TextUtils
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.util.T
import java.io.*
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import kotlin.concurrent.thread

/**
 * HttpURLConnection请求工具
 */
object HttpUtil {

    /**
     * get请求
     */
    fun get(url: String, listener: HttpCallBack) {
        Thread(
            Runnable {
                val connection: HttpURLConnection
                try {
                    val url = URL(url)
                    connection = url.openConnection() as HttpURLConnection
                    connection.requestMethod = "GET"
                    connection.connectTimeout = 8000
                    connection.readTimeout = 8000
                    connection.connect()
                    if (HttpURLConnection.HTTP_OK == connection.responseCode) {
                        val reader = BufferedReader(InputStreamReader(connection.inputStream))
                        val response = StringBuilder()
                        var line = reader.readLine()
                        while (!TextUtils.isEmpty(line)) {
                            response.append(line)
                            line = reader.readLine()
                        }
                        listener.onSuccess(response.toString())
                    } else {
                        listener.onError(T.getContext().getString(R.string.server_error)) //"服务器出错"
                    }
                } catch (e: MalformedURLException) {
                    listener.onError(e.message ?: "")
                    e.printStackTrace()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }).start()
    }

    /**
     * post请求
     */
    fun post(url: String, params: Map<String, Any>, listener: HttpCallBack) {
        thread(start = true) {
            try {
                (URL(url).openConnection() as? HttpURLConnection)?.run {
                    requestMethod = "POST"
                    connectTimeout = 8000
                    readTimeout = 8000
                    doOutput = true
                    outputStream?.run {
                        PrintWriter(this).run {
                            write(getPostParams(params))
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
                        listener.onSuccess(response.toString())
                    } else {
                        listener.onError(T.getContext().getString(R.string.server_error)) //"服务器出错"
                    }
                }
            } catch (e: MalformedURLException) {
                listener.onError(e.message ?: "")
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    /**
     * postJson请求
     */
    fun postJson(url: String, json: String, listener: HttpCallBack) {
        thread(start = true) {
            try {
                (URL(url).openConnection() as? HttpURLConnection)?.run {
                    requestMethod = "POST"
                    connectTimeout = 8000
                    readTimeout = 8000
                    doOutput = true
                    doInput = true
                    addRequestProperty("Charset", "UTF-8")
                    addRequestProperty("Content-Type","application/json")
                    outputStream?.run {
                        PrintWriter(this).run {
                            print(json)
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
                        listener.onSuccess(response.toString())
                    } else {
                        listener.onError(T.getContext().getString(R.string.server_error)) //"服务器出错"
                    }
                }
            } catch (e: MalformedURLException) {
                listener.onError(e.message ?: "")
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 将Map集合中的数据转换成post提交的格式
     *
     * @param params
     * @return
     */
    private fun getPostParams(params: Map<String, Any>): String {
        val sb = StringBuilder()
        params.keys.forEach {
            if (sb.isEmpty())
                sb.append(it).append("=").append(params[it])
            else
                sb.append("&").append(it).append("=").append(params[it])
        }
        return sb.toString()
    }

}

interface HttpCallBack {
    fun onSuccess(response: String)
    fun onError(error: String)
}