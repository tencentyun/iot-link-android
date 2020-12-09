package com.tencent.iot.explorer.link.core.auth.listener

import com.tencent.iot.explorer.link.core.auth.entity.User

/**
 * 登录过期监听器
 */
interface LoginExpiredListener {

    fun expired(user: User)

}