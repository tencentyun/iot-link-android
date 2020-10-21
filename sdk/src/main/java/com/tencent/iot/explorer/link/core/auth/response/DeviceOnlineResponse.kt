package com.tencent.iot.explorer.link.core.auth.response

import com.tencent.iot.explorer.link.core.auth.entity.DeviceOnlineEntity

/**
 * 设备在线状态响应实体
 */
class DeviceOnlineResponse {

    var deviceStatuses: List<DeviceOnlineEntity>? = null
//    var ProductId = ""
    var RequestId = ""
//    var TotalCnt = 0
}