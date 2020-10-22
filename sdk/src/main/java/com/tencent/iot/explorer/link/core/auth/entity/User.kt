package com.tencent.iot.explorer.link.core.auth.entity

import android.text.TextUtils

/**
 * 用户
 */
class User {

    var ExpireAt = 0L

    var Token = ""

    var CancelAccountTime = 0L

    /**
     *  登录是否过期
     */
    fun isExpire(): Boolean {
        return (System.currentTimeMillis() > ExpireAt * 1000) || TextUtils.isEmpty(Token)
    }

    /**
     * 清除用户信息
     */
    fun clear() {
        ExpireAt = 0L
        Token = ""
    }

}