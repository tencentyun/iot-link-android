package com.tencent.iot.explorer.link.kitlink.response

import com.tencent.iot.explorer.link.core.auth.entity.RoomEntity

/**
 * 房间列表响应实体
 */
class RoomListResponse {

    var RequestId = ""
    var Roomlist: List<RoomEntity>? = null
    var Total = 0

}