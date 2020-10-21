package com.tencent.iot.explorer.link.kitlink.response

import com.tencent.iot.explorer.link.core.auth.entity.DeviceOnlineEntity

/**
 * 设备在线状态响应实体
 */
class DeviceOnlineResponse {

    var DeviceStatuses: List<DeviceOnlineEntity>? = null
    //    var Devices = arrayListOf<DeviceOnlineEntity>()
//    var ProductId = ""
    var RequestId = ""
//    var TotalCnt = 0
}