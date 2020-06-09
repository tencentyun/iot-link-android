package com.tenext.auth.impl

import com.tenext.auth.callback.MyCallback

/**
 * 密码
 */
interface PasswordImpl {

    /**
     * 发送手机验证码
     */
    fun sendPhoneCode(countryCode: String, phone: String, callback: MyCallback)

    /**
     * 发送手机验证码
     */
    fun sendEmailCode(email: String, callback: MyCallback)

    /**
     * 验证手机验证码
     */
    fun checkPhoneCode(
        countryCode: String, phone: String, code: String, callback: MyCallback
    )

    /**
     * 验证邮箱验证码
     */
    fun checkEmailCode(email: String, code: String, callback: MyCallback)

    /**
     * 邮箱重置密码
     */
    fun resetEmailPassword(email: String, code: String, pwd: String, callback: MyCallback)

    /**
     * 手机号重置密码
     */
    fun resetPhonePassword(
        countryCode: String, phone: String, code: String, pwd: String, callback: MyCallback
    )

    /**
     * 重置密码
     */
    fun resetPassword(oldPwd: String, pwd: String, callback: MyCallback)

}