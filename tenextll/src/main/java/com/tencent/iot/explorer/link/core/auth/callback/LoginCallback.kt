package com.tencent.iot.explorer.link.core.auth.callback

import com.tencent.iot.explorer.link.core.auth.entity.User

/**
 * 登录成功回调
 */
interface LoginCallback {

    fun success(user: User)

    fun fail(msg: String)

}