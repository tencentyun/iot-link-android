package com.tencent.iot.video.link.service

import android.text.TextUtils
import android.util.Log
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.tencent.iot.video.link.callback.DetectMesssageCallback
import com.tencent.iot.video.link.consts.VideoConst
import com.tencent.iot.video.link.entity.VideoMessageType
import com.tencent.iot.video.link.entity.WlanDetectBody
import com.tencent.iot.video.link.entity.WlanRespBody
import com.tencent.xnet.XP2P
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

    private var TAG = DetectService::class.java.simpleName
    @Volatile
    private var socket: DatagramSocket? = null
    var port = 3072
    var overTime = 5000
    @Volatile
    var groupAddress = "255.255.255.255"
    private val BUFFER_SIZE = 2048
    private val sdkVarsion = XP2P.getVersion()
    var detectMesssageCallback: DetectMesssageCallback? = null
    var adapterCallback: DetectMesssageCallback = object: DetectMesssageCallback {
        override fun onMessage(version: String, message: String): Boolean {
            Log.e(TAG, "version $version, message $message")
            if (detectMesssageCallback?.onMessage(version, message) == true) {  // 使用集成 sdk 方的处理逻辑

            } else { // 使用内部处理逻辑
                var resp = JSONObject.parseObject(message, WlanRespBody::class.java)
                resp.let {
                    if (it.method == "probeMatch" && it.params != null && it.params.isReady()) {
//                        cancel() // 查询到设备的 IP 地址信息，停止广播发送和接收内容
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
        Log.e(TAG, "startSendBroadcast times $times")
        resetSocket()

        socket = DatagramSocket(port)
        openReceiver()
        launch(Dispatchers.IO) {
            for (i in 0 until times) {  // 尝试在协程中发送指定次数的广播
                sendBroadcast(body)
                delay(1000) // 每秒发一次广播
            }
        }
    }

    private fun sendBroadcast(body: WlanDetectBody) {
        Log.e(TAG, "sendBroadcast body ${JSON.toJSONString(body)}")
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
        if (!TextUtils.isEmpty(devs)) { paramsJson[VideoConst.VIDEO_WLAN_DEV_NAMES] = devs } // 存在数据的时候发送该内容
        json[VideoConst.VIDEO_WLAN_PARAMS] = paramsJson
        var dataPayload = json.toJSONString().toByteArray()
        var headerData = buildHeader(VideoMessageType.DETECT_BODY, sdkVarsion, dataPayload.size)
        var data2Send = ByteArray(headerData.size + dataPayload.size)
        System.arraycopy(headerData, 0, data2Send, 0, headerData.size)
        System.arraycopy(dataPayload, 0, data2Send, headerData.size, dataPayload.size)
        sendBroadcast(data2Send)
    }

    private fun buildHeader(type: VideoMessageType, version: String, len: Int): ByteArray {
        var headerBytes = ByteArray(4)
        headerBytes[0] = type.getValue().toByte()
        var versionParts = version.split(".")
        versionParts?.let { oriParts ->
            if (oriParts.size < 2) return@let
            oriParts[0].toBigIntegerOrNull()?.let { versionPrefix ->
                oriParts[1].toBigIntegerOrNull()?.let { versionsuffix ->
                    headerBytes[1] = ((versionPrefix.toInt() shl 4) or versionsuffix.toInt()).toByte()
                }
            }
        }
        len?.let {
            headerBytes[3] = (len / Math.pow(2.0, 8.0).toInt()).toByte()
            headerBytes[2] = (len % Math.pow(2.0, 8.0).toInt()).toByte()
        }
        return headerBytes
    }

    private fun sendBroadcast(dataStr: String) {
        sendBroadcast(dataStr.toByteArray())
    }

    private fun sendBroadcast(data: ByteArray) {
        socket?.let {
            val packet = DatagramPacket(data, data.size, InetAddress.getByName(groupAddress), port)
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
                            var headerBytes = ByteArray(4)
                            System.arraycopy(recvbuffer, 0, headerBytes, 0, headerBytes.size)

                            // 非标准响应 body 不做处理
                            if (headerBytes[0] != VideoMessageType.DETECT_RESP_BODY.getValue().toByte()) continue
                            var version = (headerBytes[1].toInt() shr 4).toString()
                            version = "$version.${(headerBytes[1].toInt() and 0x0F)}"
                            adapterCallback?.onMessage(version, String(recvbuffer, 4, dataPacket.length - 4))
                        } catch (e: SocketException) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        }.start()
    }
}