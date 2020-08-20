package com.tencent.iot.explorer.link.core.link

import org.json.JSONException
import org.json.JSONObject
import java.net.DatagramSocket

open class ConfigService {
    var host = ""
    val port = 8266
    var socket: DatagramSocket? = null

    //第一步发送 wifi 信息到设备
    @Volatile
    var sendWifiInfoSuccess = false
    //是否执行 run，存在线程停止的可能，需要使用并发关键字
    @Volatile
    var hasRun = false
    val maxTimes2Try = 10

    @Throws(JSONException::class)
    fun genLinkString(ssid: String, password: String, token: String, region: String): String {
        val jsonObject = JSONObject()
        jsonObject.put("cmdType", 1)
        jsonObject.put("ssid", ssid)
        jsonObject.put("password", password)
        jsonObject.put("token", token)
        jsonObject.put("region", region)
//        jsonObject.put("token", App.data.bindDeviceToken)
        return jsonObject.toString()
    }

    fun closeSocket() {
        if (socket != null && !socket!!.isClosed) {
            socket?.close()
            socket = null
        }
    }
}