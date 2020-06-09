package com.mvp.presenter

import android.content.Context
import com.mvp.model.UserInfoModel
import com.mvp.ParentPresenter
import com.mvp.view.UserInfoView

/**
 * 个人信息
 */
class UserInfoPresenter(view: UserInfoView) : ParentPresenter<UserInfoModel, UserInfoView>(view) {
    override fun getIModel(view: UserInfoView): UserInfoModel {
        return UserInfoModel(view)
    }

    fun upload(context: Context, srcPath: String) {
        model?.upload(context, srcPath)
    }

    fun logout() {
        model?.logout()
    }

    fun getUserInfo() {
        model?.getUserInfo()
    }

    fun modifyNick(nick: String) {
        model?.modifyNick(nick)
    }
}