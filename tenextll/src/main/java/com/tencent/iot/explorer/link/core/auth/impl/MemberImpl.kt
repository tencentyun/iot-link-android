package com.tencent.iot.explorer.link.core.auth.impl

import com.tencent.iot.explorer.link.core.auth.callback.MyCallback

/**
 * 家庭成员Impl
 */
interface MemberImpl {

    /**
     *  发送邀请成员
     */
    fun sendFamilyInvite(familyId: String, userId: String, callback: MyCallback)

    /**
     * 移除家庭成员
     */
    fun deleteFamilyMember(familyId: String,memberId: String, callback: MyCallback)

    /**
     * 成员加入家庭
     */
    fun joinFamily(shareToken: String, callback: MyCallback)

    /**
     * 成员自动退出家庭
     */
    fun exitFamily(familyId: String, callback: MyCallback)

    /**
     * 删除家庭
     */
    fun deleteFamily(familyId: String, familyName: String, callback: MyCallback)

}