package com.tencent.iot.explorer.link.core.auth.response

import com.tencent.iot.explorer.link.core.auth.entity.DeviceOnline

/**
 * 设备在线状态响应实体
 */
class DeviceOnlineResponse {

    var deviceStatuses: List<DeviceOnline>? = null
//    var ProductId = ""
    var RequestId = ""
//    var TotalCnt = 0
}