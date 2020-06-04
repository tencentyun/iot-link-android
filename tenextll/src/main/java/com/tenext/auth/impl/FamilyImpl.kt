package com.tenext.auth.impl

import com.tenext.auth.callback.MyCallback

interface FamilyImpl {

    /**
     * 新增家庭
     */
    fun createFamily(familyName: String, address: String, callback: MyCallback)

    /**
     * 请求获取家庭列表
     */
    fun familyList(offset: Int, callback: MyCallback)

    /**
     * 请求获取家庭列表
     */
    fun familyList(offset: Int, limit: Int, callback: MyCallback)

    /**
     * 修改家庭
     */
    fun modifyFamily(familyId: String, familyName: String, address: String, callback: MyCallback)

    /**
     * 家庭详情
     */
    fun familyInfo(familyId: String, callback: MyCallback)

    /**
     * 家庭成员列表
     */
    fun memberList(familyId: String, offset: Int, callback: MyCallback)

    /**
     * 家庭成员列表
     */
    fun memberList(familyId: String, offset: Int, limit: Int, callback: MyCallback)

    /**
     * 房间列表
     */
    fun roomList(familyId: String, offset: Int, callback: MyCallback)

    /**
     * 房间列表
     */
    fun roomList(familyId: String, offset: Int, limit: Int, callback: MyCallback)

}