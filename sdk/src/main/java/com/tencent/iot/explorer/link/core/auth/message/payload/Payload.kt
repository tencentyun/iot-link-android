package com.tencent.iot.explorer.link.core.auth.message.payload

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

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append("收到下发原数据：").append(json).append("\n")
            .append("原数据中Payload转码后（base64）：").append(payload).append("\n")
            .append("payload中有效数据：").append(data)
        return sb.toString()
    }

}