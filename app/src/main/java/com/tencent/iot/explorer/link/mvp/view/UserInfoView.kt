package com.mvp.view

import com.mvp.ParentView

interface UserInfoView : ParentView {

    fun logout()

    fun showAvatar(imageUrl: String)

    fun showNick(nick: String)

    fun uploadFail(message: String)

    fun showUserInfo()

}