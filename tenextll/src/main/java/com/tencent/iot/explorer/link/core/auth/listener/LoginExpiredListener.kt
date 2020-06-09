package com.tenext.auth.listener

import com.tenext.auth.entity.User

/**
 * 登录过期监听器
 */
interface LoginExpiredListener {

    fun expired(user: User)

}