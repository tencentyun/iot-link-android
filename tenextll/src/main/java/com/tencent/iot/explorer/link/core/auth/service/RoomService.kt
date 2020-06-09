package com.tenext.auth.service

import com.tenext.auth.callback.MyCallback
import com.tenext.auth.consts.RequestCode
import com.tenext.auth.impl.RoomImpl

/**
 * 房间Service
 */
internal class RoomService : BaseService(), RoomImpl {

    /**
     * 创建房间
     */
    override fun create(familyId: String, roomName: String, callback: MyCallback) {
        val param = tokenParams("AppCreateRoom")
        param["Name"] = roomName
        param["FamilyId"] = familyId
        tokenPost(param, callback, RequestCode.create_room)
    }

    /**
     * 修改房间
     */
    override fun modify(familyId: String, roomId: String, roomName: String, callback: MyCallback) {
        val param = tokenParams("AppModifyRoom")
        param["FamilyId"] = familyId
        param["RoomId"] = roomId
        param["Name"] = roomName
        tokenPost(param, callback, RequestCode.modify_room)
    }

    /**
     * 删除房间
     */
    override fun delete(familyId: String, roomId: String, callback: MyCallback) {
        val param = tokenParams("AppDeleteRoom")
        param["FamilyId"] = familyId
        param["RoomId"] = roomId
        tokenPost(param, callback, RequestCode.delete_room)
    }


}