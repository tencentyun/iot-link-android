package com.tencent.iot.explorer.link.core.auth.service

import android.text.TextUtils
import com.tencent.iot.explorer.link.core.auth.callback.MyCallback
import com.tencent.iot.explorer.link.core.auth.consts.RequestCode
import com.tencent.iot.explorer.link.core.auth.impl.FamilyImpl

internal class FamilyService : BaseService(), FamilyImpl {

    override fun createFamily(familyName: String, address: String, callback: MyCallback) {
        val param = tokenParams("AppCreateFamily")
        param["Name"] = familyName
        param["Address"] = address
        tokenPost(param, callback, RequestCode.create_family)
    }

    override fun familyList(offset: Int, callback: MyCallback) {
        familyList(offset, 20, callback)
    }

    override fun familyList(offset: Int, limit: Int, callback: MyCallback) {
        val param = tokenParams("AppGetFamilyList")
        param["Offset"] = offset
        param["Limit"] = limit
        tokenPost(param, callback, RequestCode.family_list)
    }

    /**
     * 修改家庭
     */
    override fun modifyFamily(
        familyId: String, familyName: String, address: String, callback: MyCallback
    ) {
        val param = tokenParams("AppModifyFamily")
        param["FamilyId"] = familyId
        if (!TextUtils.isEmpty(familyName))
            param["Name"] = familyName
        if (!TextUtils.isEmpty(address))
            param["Address"] = address
        tokenPost(param, callback, RequestCode.modify_family)
    }

    /**
     * 家庭详情
     */
    override fun familyInfo(familyId: String, callback: MyCallback) {
        val param = tokenParams("AppDescribeFamily")
        param["FamilyId"] = familyId
        tokenPost(param, callback, RequestCode.family_info)
    }

    /**
     * 家庭成员列表
     */
    override fun memberList(familyId: String, offset: Int, limit: Int, callback: MyCallback) {
        val param = tokenParams("AppGetFamilyMemberList")
        param["FamilyId"] = familyId
        param["Offset"] = offset
        param["Limit"] = 50
        tokenPost(param, callback, RequestCode.member_list)
    }

    override fun memberList(familyId: String, offset: Int, callback: MyCallback) {
        memberList(familyId, offset, 20, callback)
    }

    override fun roomList(familyId: String, offset: Int, callback: MyCallback) {
        roomList(familyId, offset, 20, callback)
    }

    override fun roomList(familyId: String, offset: Int, limit: Int, callback: MyCallback) {
        val param = tokenParams("AppGetRoomList")
        param["FamilyId"] = familyId
        param["Offset"] = offset
        param["Limit"] = 20
        tokenPost(param, callback, RequestCode.room_list)
    }

}