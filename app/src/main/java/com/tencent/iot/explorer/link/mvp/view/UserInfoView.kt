package com.tencent.iot.explorer.link.mvp.view

import com.tencent.iot.explorer.link.mvp.ParentView

interface UserInfoView : ParentView {

    fun logout()

    fun showAvatar(imageUrl: String)

    fun showNick(nick: String)

    fun uploadFail(message: String)

    fun showUserInfo()

}