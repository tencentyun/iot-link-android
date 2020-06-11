package com.tencent.iot.explorer.link.kitlink.entity

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.alibaba.fastjson.util.Base64
import com.util.L

class DeviceChangeEntity {

    var Time = 0L
    var Type = ""
    var SubType = ""
    var Topic = ""
    var Payload = ""
    var Seq = 0
    var DeviceId = ""

    var payloadEntity: JSONObject? = null

    fun getDecodePayload(): String {
        return String(Base64.decodeFast(Payload))
    }

    private fun getDecodePayloadObject(): JSONObject {
        if (payloadEntity == null)
            payloadEntity = JSON.parseObject(getDecodePayload())
        payloadEntity?.let {
            return it
        }
        return JSONObject()
    }

    //    {"type":"delta", "payload":{"state":{"power_switch":0},"version":0}}
    //    {"type":"update","state":{"reported":{"brightness":23}},"version":0,"clientToken":"API-ControlDeviceData-1571981804"}
    fun getPayloadReported(): JSONObject {
        getDecodePayloadObject().run {
            when (getString("type")) {
                "update" -> {
                    val state = this.getJSONObject("state")
                    state?.let {
                        return it.getJSONObject("reported")
                    }
                }
                "delta" -> {
                    val payload = this.getJSONObject("payload")
                    payload?.let {
                        return it.getJSONObject("state")
                    }
                }
            }
            return JSONObject()
        }
    }

}