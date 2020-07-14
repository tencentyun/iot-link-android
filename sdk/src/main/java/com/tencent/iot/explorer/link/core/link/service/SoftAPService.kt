package com.tencent.iot.explorer.link.core.link

import android.content.Context
import android.net.wifi.WifiManager
import android.text.TextUtils
import android.util.Log
import com.alibaba.fastjson.JSON
import com.tencent.iot.explorer.link.core.auth.util.Weak
import com.tencent.iot.explorer.link.core.link.entity.DeviceInfo
import com.tencent.iot.explorer.link.core.link.entity.LinkTask
import com.tencent.iot.explorer.link.core.link.entity.SoftAPStep
import com.tencent.iot.explorer.link.core.link.listener.SoftAPListener
import com.tencent.iot.explorer.link.core.log.L
import com.tencent.iot.explorer.link.core.utils.PingUtil
import org.json.JSONObject
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import kotlin.concurrent.thread

class SoftAPService(context: Context) : ConfigService(){

    private val TAG = this.javaClass.simpleName

    companion object {
        const val SEND_WIFI_FAIL = 0
        const val LISTEN_WIFI_FAIL = 3
    }

    private var context by Weak {
        context.applicationContext
    }

    private lateinit var task: LinkTask

    private var listener: SoftAPListener? = null

    init {
        createClient()
    }

    private fun createClient() {
        (context?.applicationContext?.getSystemService(Context.WIFI_SERVICE) as? WifiManager)?.let {
            socket = DatagramSocket(port)
            L.e("gateway=${it.dhcpInfo.gateway}")
            host = intToIp(it.dhcpInfo.gateway)
            Log.e("XXX", "host=" + host)
        }
    }

    /**
     * xxxxxxxx 转成 xxx.xxx.xxx.xxx
     * int转化为ip地址
     */
    private fun intToIp(paramInt: Int): String {
        return ((paramInt and 0xFF).toString() + "." + (0xFF and (paramInt shr 8)) + "." +
                (0xFF and (paramInt shr 16)) + "." + (0xFF and (paramInt shr 24)))
    }

    /**
     * 停止设备配网
     */
    fun stopConnect() {
        sendWifiInfoSuccess = false
        hasRun = false
        socket?.close()
    }

    /**
     * 开始 soft ap 配网
     */
    fun startConnect(task: LinkTask, listener: SoftAPListener) {
        L.e("开始 soft ap 配网")
        hasRun = true
        this.task = task
        this.listener = listener
        listener.onStep(SoftAPStep.STEP_LINK_START)
        thread {
            sendUdpPacketWithWifiInfo()
            // 向硬件设备发送 wifi 账号/密码成功后，断开 tcp/ip 链接（用于发送 udp 报文的链接）
            if (sendWifiInfoSuccess && hasRun) {
                stopConnect()
            }
        }
    }

    /**
     * 通过 udp 报文发送 wifi 的账号/密码
     */
    private fun sendUdpPacketWithWifiInfo() {
        val wifiMsg = genLinkString(task.mSsid, task.mPassword, task.mAccessToken).toByteArray()
        val datagramPacket =
            DatagramPacket(wifiMsg, wifiMsg.size, InetAddress.getByName(host), port)
        recvWifiMsgFeedback()
        listener?.onStep(SoftAPStep.STEP_SEND_WIFI_INFO)
        try {
            var times = 0
            while (!sendWifiInfoSuccess && hasRun && times < maxTimes2Try) {
                L.d("正在发送wifi信息")
                socket?.send(datagramPacket)
                times++
                Thread.sleep(1000)
            }
            if (!sendWifiInfoSuccess && times >= maxTimes2Try) {
                fail(SEND_WIFI_FAIL, "请检查是否正确连接热点")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            fail(SEND_WIFI_FAIL, "发送wifi信息到设备失败")
        }
    }

    /**
     * 监听发送 wifi 信息到设备
     */
    private fun recvWifiMsgFeedback() {
        thread(start = true) {
            val receiver = ByteArray(1024)
            try {
                while (!sendWifiInfoSuccess && hasRun) {
                    L.e("开始监听wifi信息发送回复")
                    socket?.receive(DatagramPacket(receiver, receiver.size))
                    ByteArrayInputStream(receiver).use {
                        BufferedReader(it.reader()).use {
                            var resp = ""
                            var line = it.readLine()
                            while (line != null) {
                                resp += line
                                line = it.readLine()
                            }

                            checkWifiUDPResp(resp)
                        }
                    }

                }
            } catch (e: Exception) {
                e.printStackTrace()
                fail(LISTEN_WIFI_FAIL, "监听设备联网失败")
            }
        }
    }

    /**
     * 检查设备对应 udp 报文的相应内容
     */
    private fun checkWifiUDPResp(resp: String) {

        // 成功返回：{"cmdType":2,"productId":"0BCDALFUO8","deviceName":"dev4","protoVersion":"2.0"}
        if (TextUtils.isEmpty(resp)) {
            sendWifiInfoSuccess = false
            L.e("设备没有对 udp 报文做响应")

        } else {
            var resJson = JSONObject(resp)
            if (resJson == null)  return
            L.e("接收到回复：${resJson}")

            sendWifiInfoSuccess = resJson.has("productId") && resJson.has("deviceName")

            if (sendWifiInfoSuccess) {
                listener?.onStep(SoftAPStep.STEP_GOT_DEVICE_INFO)
                val deviceInfo = DeviceInfo(resJson)
                listener?.onSuccess(deviceInfo)
                reconnectedWifi(deviceInfo)
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

}