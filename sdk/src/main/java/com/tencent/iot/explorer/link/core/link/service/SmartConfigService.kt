package com.tencent.iot.explorer.link.core.link.service

import android.content.Context
import android.text.TextUtils
import com.espressif.iot.esptouch.EsptouchTask
import com.espressif.iot.esptouch.IEsptouchResult
import com.tencent.iot.explorer.link.core.link.entity.DeviceInfo
import com.tencent.iot.explorer.link.core.link.entity.DeviceTask
import com.tencent.iot.explorer.link.core.link.exception.TCLinkException
import com.tencent.iot.explorer.link.core.link.listener.SmartConfigListener
import com.tencent.iot.explorer.link.core.log.L
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.Socket

class SmartConfigService(context: Context, task: DeviceTask) : DeviceService(context, task) {

    private val port = 8266
    private var socket: Socket? = null
    private val deviceReplyKey = "deviceReply"
    private var esptouchTask: EsptouchTask? = null

    var listener: SmartConfigListener? = null

    private lateinit var mainJob: Job


    /**
     * 停止配网
     */
    override fun stop() {
        try {
            esptouchTask?.let {
                it.interrupt()
                esptouchTask = null
            }
            socket?.close()
            socket = null
            mainJob.cancel()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 开始配网
     */
    override fun start() {
        val ssid = mTask.mSsid.replace("\"", "")
        if (TextUtils.isEmpty(ssid)) {
            L.d("SmartConfigService", "task.mSsid不能为空")
            return
        }
        mainJob = CoroutineScope(Dispatchers.IO).launch {
            esptouchTask = EsptouchTask(
                ssid,
                mTask.mBssid,
                mTask.mPassword,
                context!!
            )
            L.d("SmartConfigService", "正在连接到$ssid,${mTask.mBssid},${mTask.mPassword}")
            esptouchTask?.let {
                it.setEsptouchListener { result ->
                    CoroutineScope(Dispatchers.Main).launch {
                        listener?.connectedToWifi(
                            result.isSuc, result.bssid, result.isCancelled, result.inetAddress
                        )
                    }
                    requestDeviceInfo(result)
                }
                val result = it.executeForResult()
                if (!result.isSuc) {
                    CoroutineScope(Dispatchers.Main).launch {
                        listener?.connectFailed()
                    }
                    stop()
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
     * "productId": "*********",
     * "deviceName":   "wifi",
     * "connId":   "happy",
     * "signature":    "****************************",
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
    private fun requestDeviceInfo(result: IEsptouchResult) {
        try {
            socket = Socket(result.inetAddress.hostAddress, port)
            val bufferedReader =
                BufferedReader(InputStreamReader(socket!!.getInputStream(), "utf-8"))
            sendMessage(genRequestDeviceInfoString())
            var jsonStr = ""
            var errorStr = ""
            try {
                val nowStr = bufferedReader.readLine()
                if (!TextUtils.isEmpty(nowStr)) {
                    jsonStr += nowStr
                }
            } catch (e: Exception) {
            }
            val msgArray = processReceivedJSONString(jsonStr)
            var hasCurrentError = false
            var deviceInfoObj = JSONObject()
            val connectionErrorArray = JSONArray()
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
            CoroutineScope(Dispatchers.Main).launch {
                listener?.onSuccess(DeviceInfo(deviceInfoObj))
            }
        } catch (e: Exception) {
            CoroutineScope(Dispatchers.Main).launch {
                listener?.onFail("GET_DEVICE_INFO_FAILURE", "获取设备签名失败")
            }
        }
        //停止配网
        stop()
    }

    @Throws(IOException::class)
    private fun sendMessage(message: String) {
        socket?.let {
            val outputStream = it.getOutputStream()
            outputStream.write(message.toByteArray(charset("utf-8")))
            outputStream.flush()
        }
    }

    @Throws(JSONException::class)
    private fun genRequestDeviceInfoString(): String {
        val jsonObject = JSONObject()
        jsonObject.put("cmdType", 0)
        jsonObject.put("timestamp", System.currentTimeMillis() / 1000)
        return jsonObject.toString()
    }

    @Throws(JSONException::class)
    private fun processReceivedJSONString(jsonString: String): List<JSONObject> {
        val json = jsonString.replace("}{", "}&Split&{")
        val jsonArr = json.split("&Split&")
        val jsonObjectArray = ArrayList<JSONObject>()

        jsonArr.forEach {
            jsonObjectArray.add(JSONObject(it))
        }
        return jsonObjectArray
    }

}