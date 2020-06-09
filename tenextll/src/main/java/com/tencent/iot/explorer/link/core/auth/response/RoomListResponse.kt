package com.tenext.auth.response

import com.tenext.auth.entity.Room

/**
 * 房间列表响应实体
 */
class RoomListResponse {

    var RequestId = ""
    var Roomlist = arrayListOf<Room>()
    var Total = 0

}