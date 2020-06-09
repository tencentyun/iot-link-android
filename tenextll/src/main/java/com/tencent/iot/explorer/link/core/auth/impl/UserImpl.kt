package com.tenext.auth.impl

import android.content.Context
import com.tenext.auth.callback.MyCallback

interface UserImpl {

    /**
     *  登出
     */
    fun logout(callback: MyCallback)

    /**
     * 用户信息
     */
    fun userInfo(callback: MyCallback)

    /**
     * 意见反馈
     */
    fun feedback(advise: String, phone: String, pic: String, callback: MyCallback)

    /**
     * 发送手机验证码
     */
    fun sendBindPhoneCode(countryCode: String, phone: String, callback: MyCallback)

    /**
     * 验证手机验证码
     */
    fun checkBindPhoneCode(
        countryCode: String, phone: String, code: String, callback: MyCallback
    )

    /**
     * 绑定用户手机号
     */
    fun bindPhone(countryCode: String, phone: String, code: String, callback: MyCallback)

    /**
     *  修改用户昵称
     */
    fun modifyAlias(alias: String, callback: MyCallback)

    /**
     *  修改头像
     *  @param avatar 网络路径
     */
    fun modifyPortrait(avatar: String, callback: MyCallback)

    /**
     *  查找手机用户
     */
    fun findPhoneUser(phone: String, countryCode: String, callback: MyCallback)

    /**
     *  查找邮箱用户
     */
    fun findEmailUser(email: String, callback: MyCallback)

}