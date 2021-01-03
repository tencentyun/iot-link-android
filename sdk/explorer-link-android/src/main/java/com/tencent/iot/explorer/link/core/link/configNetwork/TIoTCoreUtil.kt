package com.tencent.iot.explorer.link.core.link.configNetwork

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import com.tencent.iot.explorer.link.core.link.entity.DeviceInfo
import com.tencent.iot.explorer.link.core.link.entity.LinkTask
import com.tencent.iot.explorer.link.core.link.entity.SoftAPStep
import com.tencent.iot.explorer.link.core.link.listener.SoftAPConfigNetListener
import com.tencent.iot.explorer.link.core.link.listener.SoftAPListener
import com.tencent.iot.explorer.link.core.link.listener.WiredConfigListener
import com.tencent.iot.explorer.link.core.link.service.SoftAPService
import java.lang.Exception
import java.net.DatagramPacket
import java.net.InetAddress
import java.net.MulticastSocket
import java.util.*

class TIoTCoreUtil {
    val GROUP_ADDRESS = "239.0.0.255"

    var softAPService: SoftAPService? = null
    var softAPConfigNetListener: SoftAPConfigNetListener? = null
    var port = 8266
    @Volatile
    var wiredRecvRun = false
    var localHostPort = 7838
    var destHostPort = 7838
    var config = WiredConfig()

    fun generateQrCodeWithConfig(qrcodeConfig: QrcodeConfig): Bitmap? {
        if (qrcodeConfig.height <= 0 || qrcodeConfig.width <= 0) {
            return null
        }
        var json = JSONObject()
        json.put("ssid", qrcodeConfig.ssid)
        json.put("bssid", qrcodeConfig.bssid)
        json.put("pwd", qrcodeConfig.wifiPwd)
        json.put("token", qrcodeConfig.token)
        return generateQRCodeFromString(json.toJSONString(), qrcodeConfig.width, qrcodeConfig.height)
    }

    private fun generateQRCodeFromString(content: String?, width: Int, height: Int): Bitmap? {
        val hints = HashMap<EncodeHintType, String>()
        val multiFormatWriter = MultiFormatWriter()
        hints[EncodeHintType.CHARACTER_SET] = "UTF-8"
        val bitMatrix: BitMatrix = multiFormatWriter.encode(content, BarcodeFormat.QR_CODE, width, height, hints)
        val pixels = IntArray(width * height)
        for (y in 0 until height) {
            for (x in 0 until width) {
                if (bitMatrix.get(x, y)) {
                    pixels[y * width + x] = -0x1000000
                } else {
                    pixels[y * width + x] = -0x1
                }
            }
        }
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        return bitmap
    }

    fun configNetBySoftAp(context: Context, task: LinkTask, listener: SoftAPConfigNetListener) {
        softAPConfigNetListener = listener
        if (context == null) {
            softAPConfigNetListener?.onFail("error", "context is null")
            return
        }
        if (softAPService == null || softAPService?.port != port) {
            softAPService?.socket?.close()
            softAPService?.port = port
            softAPService = SoftAPService(context)
        }
        softAPService?.startConnect(task, softAPListener)
    }

    private val softAPListener = object : SoftAPListener {
        override fun onSuccess(deviceInfo: DeviceInfo) {
                softAPConfigNetListener?.onSuccess()
        }

        override fun onFail(code: String, msg: String) {
                softAPConfigNetListener?.onFail(code, msg)
        }

        override fun reconnectedSuccess(deviceInfo: DeviceInfo) {
                softAPConfigNetListener?.reconnectedSuccess()
        }

        override fun reconnectedFail(deviceInfo: DeviceInfo, ssid: String) {
                softAPConfigNetListener?.reconnectedFail()
        }

        override fun onStep(step: SoftAPStep) {}
    }

    fun configNetByWired(token: String, wiredConfigListener: WiredConfigListener) {
        if (wiredConfigListener != null) {
            wiredConfigListener.onStartConfigNet()
        }
        readyReceive(token, wiredConfigListener)
    }

    fun send(data: String) {
        val group: InetAddress = InetAddress.getByName(GROUP_ADDRESS)
        val multicastSocket = MulticastSocket()
        Thread(Runnable {
            val bytes: ByteArray = data.toByteArray()
            val datagramPacket = DatagramPacket(bytes, bytes.size, group, destHostPort) // 发送数据报，指定目标端口和目标地址
            multicastSocket.send(datagramPacket)
        }).start()
    }

    private fun readyReceive(token: String, wiredConfigListener: WiredConfigListener) {
        if (wiredRecvRun) { // 正在配网中，禁止继续
            if (wiredConfigListener != null) {
                wiredConfigListener.onConfiging()
            }
            return
        }
        wiredRecvRun = true
        val inetAddress = InetAddress.getByName(GROUP_ADDRESS) // 多播组
        val multicastSocket = MulticastSocket(localHostPort) // 新建一个socket，绑定接收端口1900

        multicastSocket.joinGroup(inetAddress) // 加入多播组
        Thread(Runnable {  // 定时一分钟后结束有线配网
            Thread.sleep(60 * 1000)
            wiredRecvRun = false
            if (wiredConfigListener != null) {
                wiredConfigListener.onFail()
            }
        }).start()

        Thread(Runnable {
            while (wiredRecvRun) {
                val inBuff = ByteArray(256 * 1024)
                val inPacket = DatagramPacket(inBuff, inBuff.size) // 构造接收数据报，包含要接收的数据、长度
                inPacket.data = inBuff
                multicastSocket.receive(inPacket) // 接收数据报
                var dataStr = String(inPacket.data, inPacket.offset, inPacket.length)
                try {
                    JSONObject.parseObject(dataStr) // 尝试是否是标准的 json 字串，只有标准的 json 才进行后续的解析

                    var recData = JSONObject.parseObject(dataStr, UdpData::class.java)
                    if (recData != null && recData.status == "received" &&
                        recData.deviceName == config.deviceName && recData.productId == config.productId) {
                        wiredRecvRun = false;
                        if (wiredConfigListener != null) {
                            wiredConfigListener.onSuccess(recData.productId, recData.deviceName)
                        }

                    } else if (recData != null && recData.status == "online") {
                        config = WiredConfig()
                        config.productId = recData.productId
                        config.deviceName = recData.deviceName
                        config.token = token
                        send(JSON.toJSONString(config))
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            multicastSocket.close()
        }).start()
    }

}