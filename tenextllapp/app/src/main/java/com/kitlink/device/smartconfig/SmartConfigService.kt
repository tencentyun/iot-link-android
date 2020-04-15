package com.kitlink.device.smartconfig

import android.content.Context
import android.text.TextUtils
import com.espressif.iot.esptouch.EsptouchTask
import com.espressif.iot.esptouch.IEsptouchResult
import com.kitlink.device.DeviceInfo
import com.kitlink.device.TCLinkException
import com.util.L
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.Socket
import kotlin.concurrent.thread

class SmartConfigService(context: Context) {

    private var context: Context? = null
    private val port = 8266
    private var socket: Socket? = null
    private val deviceReplyKey = "deviceReply"
    private var esptouchTask: EsptouchTask? = null

    init {
        this.context = context.applicationContext
    }

    fun stopConnect() {
        esptouchTask?.let {
            it.interrupt()
            esptouchTask = null
        }
    }

    fun startConnect(task: SmartConfigTask, listener: SmartConfigListener) {
        thread(start = true) {
            try {
                listener.onStep(SmartConfigStep.STEP_LINK_START)
                esptouchTask = EsptouchTask(
                    task.mSsid,
                    task.mBssid,
                    task.mPassword,
                    false,
                    context!!
                )
                esptouchTask?.let {
                    it.setEsptouchListener { result ->
                        L.e("lurs", "连接成功:" + result.inetAddress)
                        if (result.isSuc) {
                            listener.deviceConnectToWifi(result)
                            listener.onStep(SmartConfigStep.STEP_DEVICE_CONNECTED_TO_WIFI)
                            requestDeviceInfo(result, listener)
                        } else {
                            L.e("lurs", "设备未联网1:" + result.inetAddress)
                        }
                    }
                    listener.onStep(SmartConfigStep.STEP_DEVICE_CONNECTING)
                    val result = it.executeForResult()
                    if (!result.isSuc) {
                        L.e("lurs", "设备未联网2:" + result.inetAddress)
                        listener.deviceConnectToWifiFail()
                    }
                }
            } catch (e: Exception) {
                L.e(e.message)
                listener.onFail(
                    TCLinkException(
                        "CONNECT_TO_DEVICE_FAILURE",
                        "connect to device failure",
                        e
                    )
                )
                try {
                    socket?.close()
                    stopConnect()
                } catch (e: IOException) {
                }
            }
        }
    }

    /**
     * requestDeviceInfo
     * <p>
     * 成功响应：
     * {
     * "cmdType":  2,
     * "productId": "HH92MED4FI",
     * "deviceName":   "wifi",
     * "connId":   "happy",
     * "signature":    "f3e3b550dd0a63c63e69661498693fbab989215c",
     * "timestamp":    1562127932,
     * "wifiState":    "connected"
     * }
     * <p>
     * 若wifi已连接，且连接出错，立刻响应错误：
     * {
     * "cmdType":  2,
     * "deviceReply":  "Current_Error",
     * "log":  "MQTT connect error! (1, -28928)"
     * }
     * <p>
     * 新连接上，且上次有错误日志，则先响应若干条：
     * {
     * "cmdType":  2,
     * "deviceReply":  "Previous_Error",
     * "log":  "TCP socket bind error!(16, 112)"
     * }
     */
    private fun requestDeviceInfo(result: IEsptouchResult, listener: SmartConfigListener) {
        try {
            L.e("start create socket,host=${result.inetAddress.hostAddress},port=$port")
            L.e("start create socket,hostName=${result.inetAddress.hostName},port=$port")
            L.e("start create socket,canonicalHostName=${result.inetAddress.canonicalHostName},port=$port")
            socket = Socket(result.inetAddress.hostAddress, port)
            L.e("create socket success")
            val bufferedReader =
                BufferedReader(InputStreamReader(socket!!.getInputStream(), "utf-8"))
            L.e("create bufferedReader success")
            sendMessage(genRequestDeviceInfoString())
            var jsonStr = ""
            var errorStr = ""
            try {
                val nowStr = bufferedReader.readLine()
                L.e("on response: $nowStr")
                if (!TextUtils.isEmpty(nowStr)) {
                    jsonStr += nowStr
                }
            } catch (e: Exception) {
            }
            L.e("response:$jsonStr")
            val msgArray = processReceivedJSONString(jsonStr)
            var hasCurrentError = false
            var deviceInfoObj = JSONObject()
            val connectionErrorArray = JSONArray()
            L.e("msgArray:$msgArray")
            for (msgInfo in msgArray) {
                if (msgInfo.has(deviceReplyKey)) {
                    val deviceReplyType = msgInfo.getString(deviceReplyKey)
                    if (deviceReplyType == "Current_Error") {
                        hasCurrentError = true
                        errorStr = errorStr + msgInfo.getString("log") + ';'.toString()
                        connectionErrorArray.put(msgInfo)
                    } else if (deviceReplyType == "Previous_Error") {
                        errorStr = errorStr + msgInfo.getString("log") + ';'.toString()
                        connectionErrorArray.put(msgInfo)
                    }
                } else if (msgInfo.has("wifiState")) {
                    deviceInfoObj = msgInfo
                }
            }
            deviceInfoObj.put("errorLogs", connectionErrorArray)
            if (hasCurrentError) {
                throw TCLinkException("CONNECT_DEVICE_FAILURE", errorStr)
            }
            listener.onStep(SmartConfigStep.STEP_GOT_DEVICE_INFO)
            listener.onSuccess(DeviceInfo(deviceInfoObj))
        } catch (e: Exception) {
            L.e(e.message)
            listener.onFail(
                TCLinkException(
                    "GET_DEVICE_INFO_FAILURE",
                    "get device info failure",
                    e
                )
            )
        }
        try {
            socket?.close()
        } catch (e: IOException) {
        }

    }

    @Throws(IOException::class)
    private fun sendMessage(message: String) {
        L.e("start sendMessage:$message")
        socket?.let {
            val outputStream = it.getOutputStream()
            outputStream.write(message.toByteArray(charset("utf-8")))
            outputStream.flush()
            L.e("start sendMessage success")
        }
    }

    @Throws(JSONException::class)
    private fun genRequestDeviceInfoString(): String {
        L.e("create message")
        val jsonObject = JSONObject()
        jsonObject.put("cmdType", 0)
        jsonObject.put("timestamp", System.currentTimeMillis() / 1000)
        return jsonObject.toString()
    }

    @Throws(JSONException::class)
    private fun processReceivedJSONString(jsonString: String): List<JSONObject> {
        L.e("start parse response")
        val json = jsonString.replace("}{", "}&Split&{")
        val jsonArr = json.split("&Split&")
        val jsonObjectArray = ArrayList<JSONObject>()

        jsonArr.forEach {
            jsonObjectArray.add(JSONObject(it))
        }
        L.e("start parse response success")
        return jsonObjectArray
    }

}