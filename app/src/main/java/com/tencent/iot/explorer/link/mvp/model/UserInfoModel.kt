package com.tencent.iot.explorer.link.mvp.model

import android.content.Context
import android.text.TextUtils
import com.tencent.iot.explorer.link.App
import com.tencent.iot.explorer.link.kitlink.response.UserInfoResponse
import com.tencent.iot.explorer.link.kitlink.util.HttpRequest
import com.tencent.iot.explorer.link.core.auth.callback.MyCallback
import com.tencent.iot.explorer.link.kitlink.util.RequestCode
import com.tencent.iot.explorer.link.mvp.ParentModel
import com.tencent.iot.explorer.link.mvp.view.UploadView
import com.tencent.iot.explorer.link.mvp.view.UserInfoView
import com.tencent.cos.xml.exception.CosXmlClientException
import com.tencent.iot.explorer.link.core.auth.response.BaseResponse
import com.tencent.iot.explorer.link.core.log.L
import com.tencent.iot.explorer.link.kitlink.response.UserSettingResponse


/**
 * 个人信息
 */
class UserInfoModel(view: UserInfoView) : ParentModel<UserInfoView>(view), UploadView, MyCallback {

    private var avatar = ""
    private var nick = ""
    private var temperatureUnit = ""
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
                RequestCode.set_unit_of_temperature -> {
                    view?.showTemperatureUnit(temperatureUnit)
                }
                RequestCode.user_setting -> {
                    response.parse(UserSettingResponse::class.java)?.UserSetting?.run {
                        App.data.userSetting = this
                        view?.showUserSetting()
                    }
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

    /**
     * 设置温度单位
     */
    fun setTemperatureUnit(unit: String) {
        temperatureUnit = unit
        HttpRequest.instance.setTemperatureUnit(unit, this)
    }

    fun getUserSetting() {
        HttpRequest.instance.getUserSetting(this)
    }

    fun getGlobalConfig(key: String) {
        HttpRequest.instance.getGlobalConfig(key, this)
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
            if (!TextUtils.isEmpty(exception.message)) {
                exception.message?.let { it0 -> {
                    L.e(it0)
                    view?.uploadFail(it0)
                } }
            }
        }
    }

}