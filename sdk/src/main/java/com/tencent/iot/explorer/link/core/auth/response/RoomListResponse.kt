package com.tencent.iot.explorer.link.core.auth.response

import com.tencent.iot.explorer.link.core.auth.entity.Room

/**
 * 房间列表响应实体
 */
class RoomListResponse {

    var RequestId = ""
    var Roomlist = arrayListOf<Room>()
    var Total = 0

}