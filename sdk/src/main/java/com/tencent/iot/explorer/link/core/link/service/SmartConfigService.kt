package com.tencent.iot.explorer.link.core.link

import android.content.Context
import android.text.TextUtils
import com.espressif.iot.esptouch.EsptouchTask
import com.espressif.iot.esptouch.IEsptouchResult
import com.tencent.iot.explorer.link.core.link.entity.DeviceInfo
import com.tencent.iot.explorer.link.core.link.entity.LinkTask
import com.tencent.iot.explorer.link.core.link.entity.SmartConfigStep
import com.tencent.iot.explorer.link.core.link.exception.TCLinkException
import com.tencent.iot.explorer.link.core.link.listener.SmartConfigListener
import com.tencent.iot.explorer.link.core.log.L
import com.tencent.iot.explorer.link.core.utils.WifiUtil
import org.json.JSONObject
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import kotlin.concurrent.thread

class SmartConfigService(context: Context) : ConfigService() {

    private val TAG = this.javaClass.simpleName

    private var context: Context? = null
    private var esptouchTask: EsptouchTask? = null
    private var listener: SmartConfigListener? = null
    private var task: LinkTask? = null

    init {
        this.context = context.applicationContext
    }

    fun stopConnect() {
        esptouchTask?.let {
            it.interrupt()
            esptouchTask = null
        }
        closeSocket()
    }

    fun startConnect(task: LinkTask, listener: SmartConfigListener) {
        this.listener = listener

        // 如果不是 2.4GHz 的 wifi 快速失败
        if (!WifiUtil.is24GHz(context)) {
            this.listener?.onFail(
                TCLinkException("CONNECT_TO_DEVICE_FAILURE", "wifi 的频率不是 2.4GHz")
            )
            return
        }

        this.task = task
        thread(start = true) {
            try {
                this.listener?.onStep(SmartConfigStep.STEP_LINK_START)

                esptouchTask = EsptouchTask(this.task?.mSsid, this.task?.mBssid,
                    this.task?.mPassword, context!!.applicationContext)
                esptouchTask?.let {
                    this.listener?.onStep(SmartConfigStep.STEP_DEVICE_CONNECTING)

                    val result = it.executeForResult()
                    if (!result.isSuc) {
                        L.e(TAG, "设备未联网:" + result.inetAddress)
                        this.listener?.deviceConnectToWifiFail()

                    } else {
                        L.d(TAG, "连接成功:" + result.inetAddress)
                        this.listener?.deviceConnectToWifi(result)
                        this.listener?.onStep(SmartConfigStep.STEP_DEVICE_CONNECTED_TO_WIFI)
                        requestDeviceInfo(result)
                    }
                }

            } catch (e: Exception) {
                L.e(e.message!!)

                this.listener?.onFail(
                    TCLinkException("CONNECT_TO_DEVICE_FAILURE", "连接设备失败", e)
                )
                stopConnect()
            }
        }
    }

    private fun requestDeviceInfo(result: IEsptouchResult) {
        try {
            L.d("start create socket,host=${result.inetAddress.hostAddress},port=$port")
            socket = DatagramSocket(port)
            sendUdpPacketWithWifiInfo(result.inetAddress.hostAddress, this.task!!.mSsid, this.task!!.mPassword)

        } catch (e: Exception) {
            L.e(e.message!!)

            this.listener?.onFail(
                TCLinkException("GET_DEVICE_INFO_FAILURE", "获取设备信息失败", e))
            closeSocket()
        }
    }

    private fun sendUdpPacketWithWifiInfo(host: String, ssid: String, pwd: String) {
        val wifiMsg = genLinkString(ssid, pwd, task!!.mAccessToken, task!!.mRegion).toByteArray()
        val datagramPacket = DatagramPacket(wifiMsg, wifiMsg.size, InetAddress.getByName(host), port)
        recvWifiMsgFeedback()
        try {
            var times = 0
            while (!sendWifiInfoSuccess && times < maxTimes2Try) {
                L.d("正在发送 wifi 信息")
                socket?.send(datagramPacket)
                times++
                Thread.sleep(1000)
            }
            if (!sendWifiInfoSuccess && times >= maxTimes2Try) {
                this.listener?.onFail(TCLinkException("CONNECT_TO_DEVICE_FAILURE",
                    "发送 wifi 信息失败"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            this.listener?.onFail(TCLinkException("CONNECT_TO_DEVICE_FAILURE",
                "发送 wifi 信息失败"))
        }
    }

    private fun recvWifiMsgFeedback() {
        thread(start = true) {

            val receiver = ByteArray(1024)
            try {
                while (!sendWifiInfoSuccess) {
                    L.e("开始监听 wifi 信息发送回复")
                    socket!!.receive(DatagramPacket(receiver, receiver.size))
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

                closeSocket()
            } catch (e: Exception) {
                e.printStackTrace()

                this.listener?.onFail(TCLinkException("CONNECT_TO_DEVICE_FAILURE",
                    "监听设备联网失败"))
                closeSocket()
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
            L.d("接收到回复：${resJson}")

            sendWifiInfoSuccess = resJson.has("productId") && resJson.has("deviceName")

            if (sendWifiInfoSuccess) {
                listener?.onStep(SmartConfigStep.STEP_GOT_DEVICE_INFO)
                val deviceInfo = DeviceInfo(resJson)
                listener?.onSuccess(deviceInfo)
            }
        }
    }

}