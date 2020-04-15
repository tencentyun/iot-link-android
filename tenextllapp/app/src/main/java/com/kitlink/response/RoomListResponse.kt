package com.kitlink.response

import com.kitlink.entity.RoomEntity

/**
 * 房间列表响应实体
 */
class RoomListResponse {

    var RequestId = ""
    var Roomlist: List<RoomEntity>? = null
    var Total = 0

}