package com.tencent.iot.explorer.link.core.auth.service

import com.tencent.iot.explorer.link.core.auth.callback.MyCallback
import com.tencent.iot.explorer.link.core.auth.consts.RequestCode
import com.tencent.iot.explorer.link.core.auth.impl.MemberImpl

/**
 * 家庭成员Service
 */
internal class MemberService : BaseService(), MemberImpl {

    /**
     *  发送邀请成员
     */
    override fun sendFamilyInvite(familyId: String, userId: String, callback: MyCallback) {
        val param = tokenParams("AppSendShareFamilyInvite")
        param["FamilyId"] = familyId
        param["ToUserID"] = userId
        tokenPost(param, callback, RequestCode.send_family_invite)
    }

    override fun deleteFamilyMember(familyId: String, memberId: String, callback: MyCallback) {
        val param = tokenParams("AppDeleteFamilyMember")
        param["FamilyId"] = familyId
        param["MemberID"] = memberId
        tokenPost(param, callback, RequestCode.delete_family_member)
    }

    override fun joinFamily(shareToken: String, callback: MyCallback) {
        val param = tokenParams("AppJoinFamily")
        param["ShareToken"] = shareToken
        tokenPost(param, callback, RequestCode.join_family)
    }

    override fun exitFamily(familyId: String, callback: MyCallback) {
        val param = tokenParams("AppExitFamily")
        param["FamilyId"] = familyId
        tokenPost(param, callback, RequestCode.exit_family)
    }

    override fun deleteFamily(familyId: String, familyName: String, callback: MyCallback) {
        val param = tokenParams("AppDeleteFamily")
        param["FamilyId"] = familyId
        param["Name"] = familyName
        tokenPost(param, callback, RequestCode.delete_family)
    }

}