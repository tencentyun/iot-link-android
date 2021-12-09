package com.tencent.iot.video.link.service

import android.util.Log
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.tencent.iot.video.link.callback.DetectMesssageCallback
import com.tencent.iot.video.link.consts.VideoConst
import com.tencent.iot.video.link.entity.WlanDetectBody
import com.tencent.iot.video.link.entity.WlanRespBody
import kotlinx.coroutines.*
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.SocketException

/**
 * 设备探测服务
 */
class DetectService private constructor(): CoroutineScope by MainScope() {

    companion object {
        private var instance: DetectService? = null

        @Synchronized
        fun getInstance(): DetectService {
            instance?.let {
                return it
            }
            instance = DetectService()
            return instance!!
        }
    }

    private var socket: DatagramSocket? = null
    var port = 11010
    var overTime = 5000
    @Volatile
    var groupAddress = "255.255.255.255"
    private val BUFFER_SIZE = 2048
    var detectMesssageCallback: DetectMesssageCallback? = null
    var adapterCallback: DetectMesssageCallback = object: DetectMesssageCallback {
        override fun onMessage(message: String): Boolean {
            if (detectMesssageCallback?.onMessage(message) == true) {  // 使用集成 sdk 方的处理逻辑

            } else { // 使用内部处理逻辑
                var resp = JSONObject.parseObject(message, WlanRespBody::class.java)
                resp.let {
                    if (it.method == "probeMatch" && it.params != null && it.params.isReady()) {
                        cancel() // 查询到设备的 IP 地址信息，停止广播发送和接收内容
                    }
                }
            }
            return true
        }
    }

    // 尝试发一次广播
    fun startSendBroadcast(body: WlanDetectBody) {
        startSendBroadcast(body, 1)
    }

    fun clear() {
        cancel() // 关闭所有的协程
        resetSocket()
    }

    fun resetSocket() {
        socket?.let { // 尝试关闭 socket
            it.close()
        }
        socket = null
    }

    // 尝试指定次数的广播
    fun startSendBroadcast(body: WlanDetectBody, times: Int) {
        resetSocket()

        Log.e("XXX", "startSendBroadcast times $times")
        socket = DatagramSocket(port)
        Log.e("XXX", "xxx 1")
        openReceiver()
        Log.e("XXX", "xxx 2")
        launch(Dispatchers.IO) {
            Log.e("XXX", "xxx 3")
            for (i in 0 until times) {  // 尝试在协程中发送指定次数的广播
                sendBroadcast(body)
                delay(1000) // 每秒发一次广播
            }
        }
    }

    private fun sendBroadcast(body: WlanDetectBody) {
        Log.e("XXX", "sendBroadcast body ${JSON.toJSONString(body)}")
        var json = JSONObject()
        json[VideoConst.VIDEO_WLAN_METHOD] = "probe"
        json[VideoConst.VIDEO_WLAN_CLIENT_TOKEN] = body.clientToken
        json[VideoConst.VIDEO_WLAN_TIME_STAMP] = System.currentTimeMillis() / 1000
        json[VideoConst.VIDEO_WLAN_TIMEOUT_MS] = overTime

        var paramsJson = JSONObject()
        paramsJson[VideoConst.MULTI_VIDEO_PROD_ID] = body.productId
        var devs = ""
        if (body != null && body.deviceNames.isNotEmpty()) {
            for (i in 0 until body.deviceNames.size) {
                devs = "$devs,${body.deviceNames.get(i)}"
            }
        }
        if (devs.endsWith(",")) { // 清理多余的分割逗号
            devs = devs.substring(0, devs.length - 1)
        }
        paramsJson[VideoConst.VIDEO_WLAN_DEV_NAMES] = devs
        json[VideoConst.VIDEO_WLAN_PARAMS] = paramsJson
        sendBroadcast(json.toJSONString())
    }

    private fun sendBroadcast(dataStr: String) {
        socket?.let {
            Log.e("XXX", "sended dataStr $dataStr")
            val buffer = dataStr.toByteArray()
            val packet = DatagramPacket(buffer, buffer.size, InetAddress.getByName(groupAddress), port)
            it.send(packet)
        }
    }

    // 开启广播接收器
    fun openReceiver() {
        //在子线程中循环接收数据
        Thread {
            launch (Dispatchers.Default) {
                val recvbuffer = ByteArray(BUFFER_SIZE)
                val dataPacket = DatagramPacket(recvbuffer, BUFFER_SIZE)
                socket?.let {
                    while (!it.isClosed) {
                        try {
                            it.receive(dataPacket)
                            adapterCallback?.onMessage(String(recvbuffer))
                        } catch (e: SocketException) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        }.start()
    }
}