package com.tencent.iot.explorer.link.auth.message.payload

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject

class Payload {

    //设备监听到下发的数据字符串
    var json = ""

    // {"type":"delta", "payload":{"state":{"power_switch":0},"version":0}}
    // {"type":"update","state":{"reported":{"brightness":23}},"version":0,"clientToken":"API-ControlDeviceData-1571981804"}
    //json中返回的“Payload”字段base64转码后得到的json数据
    var payload = ""

    //在payload中得到的设备更新字段json字符串
    var data = ""

    //json中返回的“DeviceId”字段
    var deviceId = ""

    fun keySet(): Set<String>? {
        try {
            return JSON.parseObject(data)?.keys
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun getValue(id: String): String {
        try {
            JSON.parseObject(data)?.run {
                return when (getString(id)) {
                    "true" -> "1"
                    "false" -> "0"
                    else -> getString(id)

                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return "0"
    }

}