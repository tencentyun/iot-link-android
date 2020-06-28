package com.tencent.iot.explorer.link.core.link.service

import android.content.Context
import android.net.wifi.WifiManager
import android.text.TextUtils
import com.tencent.iot.explorer.link.core.auth.util.WifiUtil
import com.tencent.iot.explorer.link.core.link.entity.DeviceInfo
import com.tencent.iot.explorer.link.core.link.entity.DeviceTask
import com.tencent.iot.explorer.link.core.link.listener.SoftAPListener
import com.tencent.iot.explorer.link.core.log.L
import kotlinx.coroutines.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

class SoftAPService(context: Context, task: DeviceTask) : DeviceService(context, task) {

    companion object {
        const val SEND_WIFI_FAIL = 0
        const val CONNECT_TO_WIFI_FAIL = 5
        const val SIGNATURE_FAIL = 1
        const val TIMEOUT_FAIL = 2
        const val LISTEN_WIFI_FAIL = 3
        const val LISTEN_SIGNATURE_FAIL = 4
    }


    private lateinit var mainJob: Job
    private var firstSuccess = false
    private var secondSuccess = false

    private var host = ""
    private val port = 8266
    private lateinit var socketClient: DatagramSocket
    private val deviceReplyKey = "deviceReply"

    var listener: SoftAPListener? = null

    init {
        createClient()
    }

    private fun createClient() {
        socketClient = DatagramSocket(port)
        (context!!.getSystemService(Context.WIFI_SERVICE) as WifiManager).let {
            host = intToIp(it.dhcpInfo.gateway)
        }
    }

    /**
     *
     * gateway=17082560 192.168.4.1
     * int转化为ip地址
     */
    private fun intToIp(paramInt: Int): String {
        return ((paramInt and 0xFF).toString() + "." + (0xFF and (paramInt shr 8)) + "." + (0xFF and (paramInt shr 16)) + "."
                + (0xFF and (paramInt shr 24)))
    }

    /**
     * 停止设备配网
     */
    override fun stop() {
        try {
            socketClient.close()
            mainJob.cancel()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 开始soft ap配网
     */
    override fun start() {
        L.d("开始soft ap配网")
        mainJob = CoroutineScope(Dispatchers.IO).launch {
            //第一步：设备联网
            sendWifi()
            val suc = async {
                return@async receiverFirst()
            }.await()
            if (suc) {
                //第二步：获取签名
                L.e("suc = $suc")
                getSignature()
                receiverSecond()
            } else {
                L.e("停止设备配网:第一步失败")
                stop()
            }
        }
    }

    /**
     * 通过UDP广播的方式发送wifi信息到设备(手机已经连接到设备热点)
     */
    private fun sendWifi() {
        CoroutineScope(Dispatchers.Default).launch {
            val firstMsg = genLinkString(mTask.mSsid, mTask.mPassword).toByteArray()
            val datagramPacket =
                DatagramPacket(firstMsg, firstMsg.size, InetAddress.getByName(host), port)
            var count = 0
            try {
                while (!firstSuccess && count < 5) {
                    L.d("sendWifi")
                    socketClient.send(datagramPacket)
                    count++
                    delay(2000)
                }
                if (count >= 5) {
                    fail(CONNECT_TO_WIFI_FAIL, "设备联网失败")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                fail(SEND_WIFI_FAIL, "发送wifi信息到设备失败")
            }
        }
    }

    /**
     * 获取签名
     */
    private fun getSignature() {
        CoroutineScope(Dispatchers.Default).launch {
            val secondMsg = genRequestDeviceInfoString().toByteArray()
            val datagramPacket =
                DatagramPacket(secondMsg, secondMsg.size, InetAddress.getByName(host), port)
            var count = 0
            try {
                while (!secondSuccess && count < 10) {
                    L.d("getSignature")
                    socketClient.send(datagramPacket)
                    count++
                    delay(2000)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                fail(SIGNATURE_FAIL, "设备与热点断开")
                L.e("停止设备配网:获取签名异常退出")
                stop()
            }
            if (count >= 10) {
                fail(SIGNATURE_FAIL, "获取设备签名超时")
            }
        }
    }

    /**
     * 监听发送wifi信息到设备
     */
    private fun receiverFirst(): Boolean {
        for (i in 0..5) {
            val receiver = ByteArray(1024)
            try {
                L.d("监听发送wifi信息到设备")
                socketClient.receive(DatagramPacket(receiver, receiver.size))
                val reader = BufferedReader(ByteArrayInputStream(receiver).reader())
                var resp = ""
                var line = reader.readLine()
                while (line != null) {
                    resp += line
                    line = reader.readLine()
                }
                // 成功返回：{"cmdType":2,"deviceReply":"dataRecived"}
                if (!TextUtils.isEmpty(resp) && JSONObject(resp).has("deviceReply")
                    && JSONObject(resp).getString("deviceReply") == "dataRecived"
                ) {
                    firstSuccess = true
                    L.d("监听发送wifi信息到设备:成功返回")
                    return true
                }
            } catch (e: Exception) {
                e.printStackTrace()
                fail(LISTEN_WIFI_FAIL, "监听设备联网失败")
                L.d("监听设备联网失败")
            }
        }
        return false
    }

    /**
     * 监听获取设备签名
     */
    private fun receiverSecond() {
        for (i in 0..10) {
            val receiver = ByteArray(1024)
            try {
                L.d("监听获取设备签名")
                socketClient.receive(DatagramPacket(receiver, receiver.size))
                val reader = BufferedReader(ByteArrayInputStream(receiver).reader())
                if (reader(reader)) {
                    secondSuccess = true
                    L.d("监听获取设备签名:成功返回")
                    return
                }
                L.d("监听获取设备签名:失败")
            } catch (e: Exception) {
                e.printStackTrace()
                fail(LISTEN_SIGNATURE_FAIL, "监听设备签名失败")
                L.d("监听设备签名失败")
            }
        }
    }

    /**
     * 手机重连wifi
     */
    private fun reconnectedWifi(deviceInfo: DeviceInfo) {
        if (WifiUtil.connect(context!!, mTask.mSsid, mTask.mBssid, mTask.mPassword)) {
            listener?.connectedToWifi(deviceInfo)
        } else {
            listener?.connectFailed(deviceInfo, mTask.mSsid)
        }
    }

    private fun fail(code: Int, message: String) {
        if (!secondSuccess)
            listener?.onFail(code.toString(), message)
        L.e("停止设备配网:fail()")
        stop()
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
    private fun reader(bufferedReader: BufferedReader): Boolean {
        try {
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
            if (hasCurrentError) {//网络连接失败
                fail(SIGNATURE_FAIL, "设备返回结果为联网失败")
            }
            if (deviceInfoObj.getString("wifiState") == "connected") {
                val deviceInfo = DeviceInfo(deviceInfoObj)
                listener?.onSuccess(deviceInfo)
                reconnectedWifi(deviceInfo)
                L.e("停止设备配网:获取签名成功")
                stop()
                return true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    @Throws(JSONException::class)
    private fun genLinkString(ssid: String, password: String): String {
        val jsonObject = JSONObject()
        jsonObject.put("cmdType", 1)
        jsonObject.put("ssid", ssid.replace("\"", ""))
        jsonObject.put("password", password)
        return jsonObject.toString()
    }

    @Throws(IOException::class)
    private fun sendMessage(message: ByteArray) {
        socketClient.let {
            it.send(DatagramPacket(message, message.size, InetAddress.getByName(host), port))
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