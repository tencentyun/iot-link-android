package com.kitlink.device.softap

import android.content.Context
import android.net.wifi.WifiManager
import android.text.TextUtils
import com.kitlink.device.DeviceInfo
import com.kitlink.util.JsonManager
import com.kitlink.util.Weak
import com.kitlink.util.PingUtil
import com.util.L
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import kotlin.concurrent.thread

class SoftAPService(context: Context) {

    companion object {
        const val SEND_WIFI_FAIL = 0
        const val CONNECT_TO_WIFI_FAIL = 5
        const val SIGNATURE_FAIL = 1
        const val TIMEOUT_FAIL = 2
        const val LISTEN_WIFI_FAIL = 3
        const val LISTEN_SIGNATURE_FAIL = 4
    }

    private var context by Weak {
        context.applicationContext
    }
    //    private var host = "192.168.4.1"
    private var host = ""
    private val port = 8266
    private lateinit var socketClient: DatagramSocket
    private val deviceReplyKey = "deviceReply"
    private lateinit var task: SoftApTask

    //第一步发送wifi信息到设备
    private var firstSuccess = false
    private var secondSuccess = false
    //是否执行run
    private var hasRun = false

    private var listener: SoftAPListener? = null

    init {
        createClient()
    }

    private fun createClient() {
        (context?.applicationContext?.getSystemService(Context.WIFI_SERVICE) as? WifiManager)?.let {
            socketClient = DatagramSocket(port)
            L.e("gateway=${it.dhcpInfo.gateway}")
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
    fun stopConnect() {
        firstSuccess = false
        secondSuccess = false
        hasRun = false
        socketClient.close()
    }

    /**
     * 开始soft ap配网
     */
    fun startConnect(task: SoftApTask, listener: SoftAPListener) {
        L.e("开始soft ap配网")
        hasRun = true
        this.task = task
        this.listener = listener
        listener.onStep(SoftAPStep.STEP_LINK_START)
        thread {
            sendWifi()
            if (firstSuccess && hasRun) {
                getSignature()
            }
        }
    }

    private fun sendWifi() {
        val firstMsg = genLinkString(task.mSsid, task.mPassword).toByteArray()
        val datagramPacket =
            DatagramPacket(firstMsg, firstMsg.size, InetAddress.getByName(host), port)
        receiverFirst()
        listener?.onStep(SoftAPStep.STEP_SEND_WIFI_INFO)
        try {
            var c = 0
            while (!firstSuccess && hasRun && c < 10) {
                L.e("正在发送wifi信息")
                socketClient?.send(datagramPacket)
                c++
                Thread.sleep(1000)
            }
            if (!firstSuccess && c >= 10) {
                fail(SEND_WIFI_FAIL, "请检查是否正确连接热点")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            fail(SEND_WIFI_FAIL, "发送wifi信息到设备失败")
        }
    }

    private fun getSignature() {
        val secondMsg = genRequestDeviceInfoString().toByteArray()
        listener?.onStep(SoftAPStep.STEP_DEVICE_CONNECTED_TO_WIFI)
        val datagramPacket =
            DatagramPacket(secondMsg, secondMsg.size, InetAddress.getByName(host), port)
        receiverSecond()
        var count = 0
        try {
            while (!secondSuccess && hasRun && count < 10) {
                L.e("正在获取设备签名:$count")
                socketClient?.send(datagramPacket)
                Thread.sleep(2000)
                count++
            }
        } catch (e: IOException) {
            e.printStackTrace()
            fail(CONNECT_TO_WIFI_FAIL, "设备联网失败")
        } catch (e: Exception) {
            e.printStackTrace()
            fail(SIGNATURE_FAIL, "获取设备签名失败")
            return
        }
        if (!secondSuccess && count >= 10) {
            fail(TIMEOUT_FAIL, "获取设备签名超时")
        }
        stopConnect()
    }

    /**
     * 监听发送wifi信息到设备
     */
    private fun receiverFirst() {
        thread(start = true) {
            val receiver = ByteArray(1024)
            try {
                while (!firstSuccess && hasRun) {
                    L.e("开始监听wifi信息发送回复")
                    socketClient.receive(DatagramPacket(receiver, receiver.size))
                    val reader = BufferedReader(ByteArrayInputStream(receiver).reader())
                    var resp = ""
                    var line = reader.readLine()
                    while (line != null) {
                        resp += line
                        line = reader.readLine()
                    }
                    // 成功返回：{"cmdType":2,"deviceReply":"dataRecived"}
                    L.e("接收到回复：$resp")
                    firstSuccess =
                        !TextUtils.isEmpty(resp) && JSONObject(resp).has("deviceReply")
                                && JSONObject(resp).getString("deviceReply") == "dataRecived"
                }
            } catch (e: Exception) {
                e.printStackTrace()
                fail(LISTEN_WIFI_FAIL, "监听设备联网失败")
            }
        }
    }

    /**
     * 监听获取设备签名
     */
    private fun receiverSecond() {
        var hasReceiver = false
        thread(start = true) {
            val receiver = ByteArray(1024)
            try {
                while (!hasReceiver && !secondSuccess && hasRun) {
                    hasReceiver = true
                    L.e("开始监听设备签名回复")
                    socketClient.receive(DatagramPacket(receiver, receiver.size))
                    val reader = BufferedReader(ByteArrayInputStream(receiver).reader())
                    reader(reader)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                fail(LISTEN_SIGNATURE_FAIL, "监听设备签名失败")
            }
        }
    }

    /**
     * 手机重连wifi
     */
    private fun reconnectedWifi(deviceInfo: DeviceInfo) {
        L.e("手机重连wifi")
        if (PingUtil.connect(context!!, task.mSsid, task.mBssid, task.mPassword)) {
            listener?.reconnectedSuccess(deviceInfo)
            L.e("连接成功")
        } else {
            listener?.reconnectedFail(deviceInfo, task.mSsid)
            L.e("连接失败")
        }
    }

    private fun fail(code: Int, message: String) {
        hasRun = false
        listener?.onFail(code.toString(), message)
        L.e(message)
        stopConnect()
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
    private fun reader(bufferedReader: BufferedReader) {
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
            if (hasCurrentError) {//网络连接失败
                fail(SIGNATURE_FAIL, "设备返回结果为联网失败")
            }
            if (deviceInfoObj.getString("wifiState") == "connected") {
                secondSuccess = true
                listener?.onStep(SoftAPStep.STEP_GOT_DEVICE_INFO)
                val deviceInfo = DeviceInfo(deviceInfoObj)
                listener?.onSuccess(deviceInfo)
                reconnectedWifi(deviceInfo)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Throws(JSONException::class)
    private fun genLinkString(ssid: String, password: String): String {
        val jsonObject = JSONObject()
        jsonObject.put("cmdType", 1)
        jsonObject.put("ssid", ssid)
        jsonObject.put("password", password)
        return jsonObject.toString()
    }

    @Throws(IOException::class)
    private fun sendMessage(message: ByteArray) {
        socketClient?.let {
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