package com.tencent.iot.explorer.link.kitlink.response

/**
 * 新增房间响应实体
 */
class CreateRoomResponse {

    var Data = RoomID()

    inner class RoomID {
        var RoomId = ""
    }
}