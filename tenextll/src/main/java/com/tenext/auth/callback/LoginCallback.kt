package com.tenext.auth.callback

import com.tenext.auth.entity.User

/**
 * 登录成功回调
 */
interface LoginCallback {

    fun success(user: User)

    fun fail(msg: String)

}