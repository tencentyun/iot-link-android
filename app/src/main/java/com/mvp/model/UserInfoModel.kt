package com.mvp.model

import android.content.Context
import android.text.TextUtils
import com.kitlink.App
import com.kitlink.response.BaseResponse
import com.kitlink.response.UserInfoResponse
import com.kitlink.util.HttpRequest
import com.kitlink.util.MyCallback
import com.kitlink.util.RequestCode
import com.mvp.ParentModel
import com.mvp.view.UploadView
import com.mvp.view.UserInfoView
import com.tencent.cos.xml.exception.CosXmlClientException
import com.util.L


/**
 * 个人信息
 */
class UserInfoModel(view: UserInfoView) : ParentModel<UserInfoView>(view), UploadView, MyCallback {

    private var avatar = ""
    private var nick = ""
    private var uploadModel: UploadModel? = null

    override fun fail(msg: String?, reqCode: Int) {
        L.e(msg ?: "")
    }

    override fun success(response: BaseResponse, reqCode: Int) {
        if (response.isSuccess()) {
            when (reqCode) {
                RequestCode.logout -> {
                    view?.logout()
                    App.data.setAppUser(null)
                }
                RequestCode.update_user_info -> {
                    App.data.userInfo.Avatar = avatar
                    view?.showAvatar(avatar)
                }
                RequestCode.user_info -> {
                    response.parse(UserInfoResponse::class.java)?.Data?.run {
                        App.data.userInfo = this
                        view?.showUserInfo()
                    }
                }
                RequestCode.modify_nick -> {
                    App.data.userInfo.NickName = nick
                    view?.showNick(nick)
                }
            }
        }
    }

    fun getUserInfo() {
        HttpRequest.instance.userInfo(this)
    }

    /**
     * 修改用户昵称
     */
    fun modifyNick(nick: String) {
        this.nick = nick
        HttpRequest.instance.modifyAliasName(nick, this, RequestCode.modify_nick)
    }

    /**
     * 提交用户头像
     */
    private fun commit() {
        if (TextUtils.isEmpty(avatar)) return
        HttpRequest.instance.modifyPortrait(avatar, this)
    }

    /**
     * 退出登录
     */
    fun logout() {
        unbindXG()
        HttpRequest.instance.logout(this)
    }

    /**
     * 解绑信鸽推送
     */
    private fun unbindXG() {
        if (TextUtils.isEmpty(App.data.xg_token)) return
        HttpRequest.instance.unbindXG(App.data.xg_token, this)
    }

    /**
     * 上传图片第一步：获取签名
     */
    fun upload(context: Context, srcPath: String) {
        uploadModel = UploadModel(this)
        uploadModel?.uploadSingleFile(context, srcPath)
    }

    override fun successAll() {
    }

    override fun uploadSuccess(loadPath: String, fileUrl: String, all: Boolean) {
        L.e("上传成功")
        avatar = fileUrl
        commit()
        L.e(fileUrl)
    }

    /**
     * 上传图片  上传失败
     */
    override fun uploadFail(loadPath: String, exception: CosXmlClientException?) {
        L.e("上传失败")
        exception?.let {
            L.e(exception.errorMessage)
            view?.uploadFail(it.errorMessage)
        }
    }


}