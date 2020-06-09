package com.tenext.auth.http

import android.text.TextUtils
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
object HttpUtil {

    /**
     * get请求
     */
    fun get(url: String, listener: HttpCallBack) {
        CoroutineScope(Dispatchers.IO).launch {
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
                    success(listener, response.toString())
                } else {
                    fail(listener, "服务器出错：$url")
                }
            } catch (e: MalformedURLException) {
                fail(listener, e.message ?: "请求URL不正确：$url")
                e.printStackTrace()
            } catch (e: IOException) {
                fail(listener, e.message ?: "数据传输时发生错误：$url")
                e.printStackTrace()
            }
        }
    }

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
    fun post(url: String, params: Map<String, Any>, listener: HttpCallBack) {
        CoroutineScope(Dispatchers.IO).launch {
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
                        success(listener, response.toString())
                    } else {
                        fail(listener, "服务器出错：$url")
                    }
                }
            } catch (e: MalformedURLException) {
                fail(listener, e.message ?: "请求URL不正确：$url")
                e.printStackTrace()
            } catch (e: IOException) {
                fail(listener, e.message ?: "数据传输时发生错误：$url")
                e.printStackTrace()
            }
        }
    }

    /**
     * postJson请求
     */
    fun postJson(url: String, json: String, listener: HttpCallBack) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                (URL(url).openConnection() as? HttpURLConnection)?.run {
                    requestMethod = "POST"
                    connectTimeout = 8000
                    readTimeout = 8000
                    doOutput = true
                    doInput = true
                    addRequestProperty("Charset", "UTF-8")
                    addRequestProperty("Content-Type", "application/json")
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
                        success(listener, response.toString())
                    } else {
                        fail(listener, "服务器出错：$url")
                    }
                }
            } catch (e: MalformedURLException) {
                fail(listener, e.message ?: "请求URL不正确：$url")
                e.printStackTrace()
            } catch (e: IOException) {
                fail(listener, e.message ?: "数据传输时发生错误：$url")
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