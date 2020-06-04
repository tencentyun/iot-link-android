package com.tenext.auth.response

import com.alibaba.fastjson.JSON
import com.tenext.auth.entity.DeviceStatus

/**
 * 设备当前数据
 */
class DeviceDataResponse {

    var Data = ""
    var RequestId = ""

    fun parseList(): List<DeviceStatus> {
        val list = ArrayList<DeviceStatus>()
        val obj = JSON.parseObject(Data)
        obj.keys.forEach {
            val entity = DeviceStatus()
            entity.id = it
            entity.value = obj.getJSONObject(it).getString("Value")
            entity.lastUpdate = obj.getJSONObject(it).getLong("LastUpdate")
            list.add(entity)
        }
        return list
    }

}