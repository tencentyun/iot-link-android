package com.tencent.iot.explorer.link.core.auth.response

import com.tencent.iot.explorer.link.core.auth.entity.RoomEntity

/**
 * 房间列表响应实体
 */
class RoomListResponse {

    var RequestId = ""
    var Roomlist = arrayListOf<RoomEntity>()
    var Total = 0

}