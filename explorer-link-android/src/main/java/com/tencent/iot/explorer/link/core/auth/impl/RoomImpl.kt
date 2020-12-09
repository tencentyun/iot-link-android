package com.tencent.iot.explorer.link.core.auth.impl

import com.tencent.iot.explorer.link.core.auth.callback.MyCallback

interface RoomImpl {

    /**
     * 创建房间
     */
    fun create(familyId: String, roomName: String, callback: MyCallback)

    /**
     * 修改房间
     */
    fun modify(familyId: String, roomId: String, roomName: String, callback: MyCallback)

    /**
     * 删除房间
     */
    fun delete(familyId: String, roomId: String, callback: MyCallback)

}