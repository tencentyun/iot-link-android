package com.auth.socket

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.util.Base64
import com.auth.message.resp.HeartMessage
import com.auth.message.MessageConst
import com.auth.message.payload.Payload
import com.auth.message.payload.PayloadMessage
import com.auth.message.resp.RespFailMessage
import com.auth.message.resp.RespSuccessMessage
import com.auth.message.resp.ResponseMessage
import com.auth.socket.callback.DispatchCallback
import com.auth.socket.callback.HeartCallback
import com.util.L
import java.lang.Exception

open class DispatchMsgHandler {

    var heartCallback: HeartCallback? = null
    var dispatchCallback: DispatchCallback? = null

    /**
     * 消息分发处理
     */
    @Synchronized
    fun dispatch(message: String) {
        try {
            JSON.parseObject(message).run {
                when {
                    getString("action") == "DeviceChange" -> {
                        parsePayloadMessage(message)
                    }
                    (containsKey("data")) -> {
                        parseYunMessage(getIntValue("reqId"), message)
                    }
                    else -> {
                        dispatchCallback?.unknownMessage(getIntValue("reqId"), message)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            dispatchCallback?.unknownMessage(0, message)
        }
    }

    fun parseYunMessage(reqId: Int, message: String) {
        when (reqId) {
            MessageConst.HEART_ID -> {
                try {
                    heartCallback?.response(
                        reqId,
                        JSON.parseObject(message, HeartMessage::class.java)
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            else -> {
                parseRspMessage(reqId, message)
            }
        }
    }

    /**
     * 解析请求响应
     */
    private fun parseRspMessage(reqId: Int, message: String) {
        var failMessage = RespFailMessage()
        try {
            JSON.parseObject(message, ResponseMessage::class.java)?.run {
                data?.let {
                    if (it.Response == null) {
                        failMessage.setErrorMessage("解析响应数据时，Response为空")
                        dispatchCallback?.yunMessageFail(reqId, message, failMessage)
                        return
                    }
                    if (it.Response!!.getJSONObject(MessageConst.ERROR) == null) {
                        val success = RespSuccessMessage()
                        success.response = it.Response!!.toJSONString()
                        success.RequestId =
                            it.Response!!.getString(MessageConst.REQUEST_ID)
                        dispatchCallback?.yunMessage(reqId, message, success)
                        return
                    } else {
                        try {
                            failMessage = JSON.parseObject(
                                it.Response!!.toJSONString(),
                                RespFailMessage::class.java
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                            failMessage.setErrorMessage("使用RespFailMessage类解析数据失败")
                        }
                        dispatchCallback?.yunMessageFail(reqId, message, failMessage)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            failMessage.setErrorMessage("解析响应数据时发生错误")
            dispatchCallback?.yunMessageFail(reqId, message, failMessage)
        }
    }

    /**
     * 解析监听到的设备更新数据
     */
    fun parsePayloadMessage(message: String) {
        try {
            JSON.parseObject(message, PayloadMessage::class.java)?.run {
                params?.let {
                    val payload = Payload()
                    payload.json = message
                    payload.deviceId = it.DeviceId
                    val p = String(Base64.decodeFast(it.Payload))
                    payload.payload = p
                    payload.data = getPayload(p)
                    dispatchCallback?.payloadMessage(payload)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            dispatchCallback?.payloadUnknownMessage(message, e.message ?: "")
        }
    }

    //    {"type":"delta", "payload":{"state":{"power_switch":0},"version":0}}
//    {"type":"update","state":{"reported":{"brightness":23}},"version":0,"clientToken":"API-ControlDeviceData-1571981804"}
    //{ "method": "report", "params": { "brightness": 14, "color": 0, "power_switch": 0, "name": "test-light-position-3556"}, "timestamp": 1581585022, "clientToken": "22"}
    private fun getPayload(p: String): String {
        L.e("Payload转码", "$p")
        JSON.parseObject(p)?.run {
            when (getString("type")) {
                "update" -> {
                    val state = this.getJSONObject("state")
                    state?.let {
                        return it.getString("reported")
                    }
                }
                "delta" -> {
                    val payload = this.getJSONObject("payload")
                    payload?.let {
                        return it.getString("state")
                    }
                }
                else -> return getString("params")
            }
        }
        return ""
    }

}