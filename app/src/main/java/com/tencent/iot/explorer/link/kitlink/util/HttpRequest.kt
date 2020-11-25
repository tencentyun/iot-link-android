package com.tencent.iot.explorer.link.kitlink.util

import android.text.TextUtils
import android.util.Log
import com.alibaba.fastjson.JSON
import com.tencent.iot.explorer.link.*
import com.tencent.iot.explorer.link.core.auth.util.JsonManager
import com.tencent.iot.explorer.link.core.auth.util.SignatureUtil
import com.tencent.iot.explorer.link.core.link.entity.DeviceInfo
import com.tencent.iot.explorer.link.core.log.L
import com.tencent.iot.explorer.link.core.utils.IPUtil
import com.tencent.iot.explorer.link.T
import com.tencent.iot.explorer.link.core.auth.callback.MyCallback
import com.tencent.iot.explorer.link.core.auth.response.BaseResponse
import com.tencent.iot.explorer.link.core.link.entity.TrtcDeviceInfo
import com.tencent.iot.explorer.link.core.utils.Utils
import com.tencent.iot.explorer.link.kitlink.consts.CommonField
import com.tencent.iot.explorer.link.kitlink.entity.AutomicTaskEntity
import com.tencent.iot.explorer.link.kitlink.entity.SceneEntity
import com.tencent.iot.explorer.link.retrofit.StringRequest
import java.util.*
import com.tencent.iot.explorer.link.retrofit.Callback
import kotlin.collections.HashMap

/**
 * 接口请求文件
 */
class HttpRequest private constructor() {

    private object HttpRequestHolder {
        val request = HttpRequest()
    }

    init {
        //初始化请求
        StringRequest.instance.init(OEM_APP_API)
    }

    companion object {
        val instance = HttpRequestHolder.request

        const val APP_KEY = BuildConfig.TencentIotLinkAppkey
        const val APP_SECRET = BuildConfig.TencentIotLinkAppSecret

        // 公版&开源体验版使用  当在 app-config.json 中配置 TencentIotLinkAppkey TencentIotLinkAppSecret 后，将自动切换为 OEM 版本。
        const val STUDIO_BASE_URL = "https://iot.cloud.tencent.com/api/studioapp"
        const val STUDIO_BASE_URL_FOR_LOGINED = "https://iot.cloud.tencent.com/api/studioapp/tokenapi"

        // OEM App 使用
        const val OEM_APP_API = "https://iot.cloud.tencent.com/api/exploreropen/appapi" // 需要替换为自建后台服务地址
        const val OEM_TOKEN_API = "https://iot.cloud.tencent.com/api/exploreropen/tokenapi"  // 可安全在设备端调用。

        const val APP_COS_AUTH = "https://iot.cloud.tencent.com/api/studioapp/AppCosAuth"
        const val BUSI_APP = "studioapp"
        const val BUSI_OPENSOURCE = "studioappOpensource"

        val ANDROID_ID = Utils.getAndroidID(T.getContext())
    }

    /**
     *  重连
     */
    fun reconnect() {
        StringRequest.instance.reconnect()
    }

    private fun baseParams(): HashMap<String, Any> {
        val param = HashMap<String, Any>()
        param["lang"] = Utils.getLang()
        param["Platform"] = "android"
        param["Agent"] = "android"
//        param["AppID"] = T.getContext().applicationInfo.packageName
        return param
    }

    /**
     * 未登录接口公共参数
     */
    private fun commonParams(action: String): HashMap<String, Any> {
        val param = baseParams()
        param["RequestId"] = UUID.randomUUID().toString()
        param["Action"] = action
        param["Platform"] = "android"
        param["AppKey"] = APP_KEY
        param["Timestamp"] = System.currentTimeMillis() / 1000
        param["Nonce"] = Random().nextInt(10)
        param["RegionId"] = App.data.regionId
        return param
    }

    /**
     * 登录后接口公共参数
     */
    private fun tokenParams(action: String): HashMap<String, Any> {
        val param = baseParams()
        param["RequestId"] = UUID.randomUUID().toString()
        param["Action"] = action
        param["Platform"] = "android"
        param["AppKey"] = APP_KEY
        param["Timestamp"] = System.currentTimeMillis() / 1000
        param["Nonce"] = Random().nextInt(10)
        param["AccessToken"] = App.data.getToken()
        param["RegionId"] = App.data.regionId
        return param
    }

    // 签字函数请务必在服务端实现，此处仅为演示，如有泄露概不负责
    private fun sign(param: HashMap<String, Any>): HashMap<String, Any> {
        val sign = SignatureUtil.format(param)
        val result = SignatureUtil.signature(sign, APP_SECRET)
        param["Signature"] = result
        return param
    }

    /**
     * 未登录请求（包含登录接口），接入层接口，此处为示例
     * 自建的接入服务器需要实现接入层接口
     */
    private fun postJson(param: HashMap<String, Any>, callback: MyCallback, reqCode: Int) {
        val action = param["Action"]
        var json = ""
        var api = ""
        var isOEM = false
        var host = ""

        if (!App.isOEMApp()) {// 公版&开源版
            param["AppID"] = T.getContext().applicationInfo.packageName
            json = JsonManager.toJson(param)
            host = STUDIO_BASE_URL
            api = "$action" + "?uin=$ANDROID_ID"
        } else {// OEM版
            host = OEM_APP_API
            json = JsonManager.toJson(sign(param))
            isOEM = true
            api = "$action"
        }

        L.e("api=$api")
        StringRequest.instance.postJson(host, api, json, object : Callback {
            override fun fail(msg: String?, reqCode: Int) {
                callback.fail(msg, reqCode)
            }

            override fun success(json: String?, reqCode: Int) {
                L.e("响应${param["Action"]}", json ?: "")
                JsonManager.parseJson(json, BaseResponse::class.java)?.run {
                    if (reqCode == RequestCode.wechat_login && isOEM
                        && this.code == ErrorCode.REQ_ERROR_CODE) {// OEM用户使用微信授权登录
                        this.msg = T.getContext().getString(R.string.ensure_import_wechat_login_with_offical_doc)//"请确保已按官网文档接入微信登录"
                    }
                    callback.success(this, reqCode)
                }
            }
        }, reqCode)
    }

    fun getRegionList(uri: String, callback: MyCustomCallBack, reqCode: Int) {
        StringRequest.instance.postJson(STUDIO_BASE_URL, uri, "", object : Callback {
            override fun fail(msg: String?, reqCode: Int) {
                callback.fail(msg, reqCode)
            }
            override fun success(json: String, reqCode: Int) {
                callback.success(json, reqCode)
            }
        }, reqCode)
    }

    /**
     * 登录后请求
     */
    private fun tokenPost(param: HashMap<String, Any>, callback: MyCallback, reqCode: Int) {
        if (TextUtils.isEmpty(App.data.getToken())) {//登录过期或未登录
            App.toLogin()
            return
        }
        val api:String
        val host:String
        if (!App.isOEMApp()) {// 公版&开源版
            param["AppID"] = T.getContext().applicationInfo.packageName
            host = STUDIO_BASE_URL_FOR_LOGINED
            api = "?uin=$ANDROID_ID"
        } else {// OEM版
            host = OEM_TOKEN_API
            api = ""
        }
        val json = JsonManager.toJson(param)

        StringRequest.instance.postJson(host, api, json, object : Callback {
            override fun fail(msg: String?, reqCode: Int) {
                callback.fail(msg, reqCode)
            }

            override fun success(json: String?, reqCode: Int) {
                L.e("响应${param["Action"]}", json ?: "")
                JsonManager.parseJson(json, BaseResponse::class.java)?.run {
                    // 检查特殊情况 token 失效
                    if (checkRespTokenValid(this)) {
                        callback.success(this, reqCode)
                    }
                }
            }
        }, reqCode)
    }

    // 处理当使用过期 token 请求时，返回的数据
    private fun checkRespTokenValid(resp: BaseResponse): Boolean {
        if (resp.code == ErrorCode.REQ_ERROR_CODE && resp.data != null) {
            var errMsg = ErrorMessage.parseErrorMessage(resp.data.toString())

            if (errMsg != null && errMsg.Code.equals(ErrorCode.DATA_MSG.ACCESS_TOKEN_ERR)) {
                App.toLogin()
                return false
            }
        }
        return true
    }

    /**
     * 登录后请求
     */
    private fun tokenAppCosAuth(param: HashMap<String, Any>, callback: MyCallback, reqCode: Int) {
        if (!App.isOEMApp()) {// 公版&开源版
            param["AppID"] = T.getContext().applicationInfo.packageName
        }
        val json = JsonManager.toJson(param)
        if (TextUtils.isEmpty(App.data.getToken())) {//登录过期或未登录
            App.toLogin()
            return
        }
        StringRequest.instance.postJson(APP_COS_AUTH, "", json, object : Callback {
            override fun fail(msg: String?, reqCode: Int) {
                callback.fail(msg, reqCode)
            }

            override fun success(json: String?, reqCode: Int) {
                L.e("响应${param["Action"]}", json ?: "")
                JsonManager.parseJson(json, BaseResponse::class.java)?.run {
                    if (checkRespTokenValid(this)) {
                        callback.success(this, reqCode)
                    }
                }
            }
        }, reqCode)
    }

    /**
     * 登录后请求
     */
    private fun getH5tokenPost(param: HashMap<String, Any>, callback: MyCallback, reqCode: Int) {
        if (TextUtils.isEmpty(App.data.getToken())) {//登录过期或未登录
            App.toLogin()
            return
        }

        val api: String
        val host: String
        if (!App.isOEMApp()) {// 公版&开源版
            param["AppID"] = T.getContext().applicationInfo.packageName
            host = STUDIO_BASE_URL_FOR_LOGINED
            api = "?uin=$ANDROID_ID"
        } else {// OEM版
            host = OEM_TOKEN_API
            api = ""
        }
        val json = JsonManager.toJson(param)

        StringRequest.instance.postJson(host, api, json, object : Callback {
            override fun fail(msg: String?, reqCode: Int) {
                callback.fail(msg, reqCode)
            }

            override fun success(json: String?, reqCode: Int) {
                L.e("响应${param["Action"]}", json ?: "")
                JsonManager.parseJson(json, BaseResponse::class.java)?.run {
                    if (checkRespTokenValid(this)) {
                        callback.success(this, reqCode)
                    }
                }
            }
        }, reqCode)
    }

    /**************************************  用户接口开始  ************************************************/

    /**
     * 获取最新 app 版本号
     *
     * http://tapd.oa.com/NEW_IOT/markdown_wikis/show/#1220393192001643313
     */
    fun getLastVersion(callback: MyCallback) {
        val param = commonParams("AppGetLatestVersion")
        param["ClientVersion"] = "0.0.0"
        param["AppPlatform"] = "android"
        param["Channel"] = 0
        postJson(param, callback, RequestCode.get_last_version)
    }

    /**
     * 手机号登录
     */
    fun phoneLogin(countryCode: String, phone: String, pwd: String, callback: MyCallback) {
        val param = commonParams("AppGetToken")
        param["Type"] = "phone"
        param["CountryCode"] = countryCode
        param["PhoneNumber"] = phone
        param["Password"] = pwd
        postJson(param, callback, RequestCode.phone_login)
    }

    /**
     * 邮箱登录
     */
    fun emailLogin(email: String, pwd: String, callback: MyCallback) {
        val param = commonParams("AppGetToken")
        param["Type"] = "email"
        param["Email"] = email
        param["Password"] = pwd
        postJson(param, callback, RequestCode.email_login)
    }

    /**
     * 手机号验证码登录
     */
    fun phoneVerifyCodeLogin(countryCode: String, phone: String, verifyCode: String, callback: MyCallback) {
        val param = commonParams("AppGetToken")
        param["Type"] = "phone"
        param["CountryCode"] = countryCode
        param["PhoneNumber"] = phone
        param["VerificationCode"] = verifyCode
        postJson(param, callback, RequestCode.phone_verifycode_login)
    }

    /**
     * 邮箱验证码登录
     */
    fun emailVerifyCodeLogin(email: String, verifyCode: String, callback: MyCallback) {
        val param = commonParams("AppGetToken")
        param["Type"] = "email"
        param["Email"] = email
        param["VerificationCode"] = verifyCode
        postJson(param, callback, RequestCode.email_verifycode_login)
    }

    /**
     * 微信登录
     */
    fun wechatLogin(code: String, callback: MyCallback) {
        val param = commonParams("AppGetTokenByWeiXin")
        param["code"] = code
        if (T.getContext().applicationInfo.packageName.equals(CommonField.OPEN_SOURCE_TAG)) {
            param["busi"] = BUSI_OPENSOURCE
        } else if (T.getContext().applicationInfo.packageName.equals(CommonField.PUBLISH_TAG)) {
            param["busi"] = BUSI_APP
        }

        postJson(param, callback, RequestCode.wechat_login)
    }

    /**
     * 获取手机验证码
     */
    fun sendMobileCode(type: String, countryCode: String, phone: String, callback: MyCallback) {
        val param = commonParams("AppSendVerificationCode")
        param["Type"] = type
        param["CountryCode"] = countryCode
        param["PhoneNumber"] = phone
        postJson(param, callback, RequestCode.send_mobile_code)
    }

    /**
     * 获取邮箱验证码
     */
    fun sendEmailCode(type: String, email: String, callback: MyCallback) {
        val param = commonParams("AppSendEmailVerificationCode")
        param["Type"] = type
        param["Email"] = email
        postJson(param, callback, RequestCode.send_email_code)
    }

    /**
     * 验证手机验证码
     */
    fun checkMobileCode(
        type: String,
        countryCode: String,
        phone: String,
        code: String,
        callback: MyCallback
    ) {
        val param = commonParams("AppCheckVerificationCode")
        param["Type"] = type
        param["CountryCode"] = countryCode
        param["PhoneNumber"] = phone
        param["VerificationCode"] = code
        postJson(param, callback, RequestCode.check_mobile_code)
    }

    /**
     * 验证邮箱验证码
     */
    fun checkEmailCode(type: String, email: String, code: String, callback: MyCallback) {
        val param = commonParams("AppCheckEmailVerificationCode")
        param["Type"] = type
        param["Email"] = email
        param["VerificationCode"] = code
        postJson(param, callback, RequestCode.check_email_code)
    }

    /**
     * 手机号注册
     */
    fun phoneRegister(
        countryCode: String, phone: String, code: String, pwd: String, callback: MyCallback
    ) {
        val param = commonParams("AppCreateCellphoneUser")
        param["CountryCode"] = countryCode
        param["PhoneNumber"] = phone
        param["VerificationCode"] = code
        param["Password"] = pwd
        postJson(param, callback, RequestCode.phone_register)
    }

    /**
     * 邮箱注册
     */
    fun emailRegister(email: String, code: String, pwd: String, callback: MyCallback) {
        val param = commonParams("AppCreateEmailUser")
        param["Email"] = email
        param["VerificationCode"] = code
        param["Password"] = pwd
        postJson(param, callback, RequestCode.email_register)
    }

    /**
     * 绑定用户手机号
     */
    fun bindPhone(countryCode: String, phone: String, code: String, callback: MyCallback) {
        val param = tokenParams("AppUpdateUser")
        param["CountryCode"] = countryCode
        param["PhoneNumber"] = phone
        param["VerificationCode"] = code
        updateUserInfo(param, callback, RequestCode.update_user_info)
    }

    /**
     * 邮箱重置密码
     */
    fun resetEmailPassword(email: String, code: String, pwd: String, callback: MyCallback) {
        val param = tokenParams("AppResetPasswordByEmail")
        param["Email"] = email
        param["VerificationCode"] = code
        param["Password"] = pwd
        postJson(param, callback, RequestCode.email_reset_pwd)
    }

    /**
     * 手机号重置密码
     */
    fun resetPhonePassword(
        countryCode: String,
        phone: String,
        code: String,
        pwd: String,
        callback: MyCallback
    ) {
        val param = tokenParams("AppResetPasswordByCellphone")
        param["CountryCode"] = countryCode
        param["PhoneNumber"] = phone
        param["VerificationCode"] = code
        param["Password"] = pwd
        postJson(param, callback, RequestCode.phone_reset_pwd)
    }

    fun resetPassword(oldPwd: String, pwd: String, callback: MyCallback) {
        val param = tokenParams("AppUserResetPassword")
        param["Password"] = oldPwd
        param["NewPassword"] = pwd
        tokenPost(param, callback, RequestCode.reset_password)
    }

    /**
     * 绑定信鸽
     */
    fun bindXG(xgToken: String, callback: MyCallback) {
        val param = tokenParams("AppBindXgToken")
        param["Token"] = xgToken
        tokenPost(param, callback, RequestCode.bind_xg)
    }

    /**
     * 解除绑定信鸽
     */
    fun unbindXG(xgToken: String, callback: MyCallback) {
        val param = tokenParams("AppUnBindXgToken")
        param["Token"] = xgToken
        tokenPost(param, callback, RequestCode.unbind_xg)
    }

    /**
     * 绑定用户邮箱号
     */
    fun bindEmail(email: String, code: String, passwd: String, callback: MyCallback) {
        val param = tokenParams("AppUpdateUser")
        param["Email"] = email
        param["VerificationCode"] = code
        if (!TextUtils.isEmpty(passwd)) param["NewPassword"] = passwd
        updateUserInfo(param, callback, RequestCode.update_user_info)
    }

    /**
     * 绑定用户手机号
     */
    fun bindPhone(countryCode: String, phone: String, code: String, passwd: String, callback: MyCallback) {
        val param = tokenParams("AppUpdateUser")
        param["CountryCode"] = countryCode
        param["PhoneNumber"] = phone
        param["VerificationCode"] = code
        if (!TextUtils.isEmpty(passwd)) param["NewPassword"] = passwd
        updateUserInfo(param, callback, RequestCode.update_user_info)
    }

    /**
     * 绑定微信
     */
    fun bindWX(code: String, callback: MyCallback) {
        val param = tokenParams("AppUpdateUserByWeiXin")
        param["code"] = code
        if (T.getContext().applicationInfo.packageName.equals(CommonField.OPEN_SOURCE_TAG)) {
            param["busi"] = BUSI_OPENSOURCE
        } else if (T.getContext().applicationInfo.packageName.equals(CommonField.PUBLISH_TAG)) {
            param["busi"] = BUSI_APP
        }
        postJson(param, callback, RequestCode.bind_wx)
    }

    /**
     * 修改用户邮箱号
     */
    fun modifyEmail(email: String, code: String, callback: MyCallback) {
        val param = tokenParams("AppUpdateUser")
        param["Email"] = email
        param["VerificationCode"] = code
        updateUserInfo(param, callback, RequestCode.update_user_info)
    }

    /**
     * 修改用户手机号
     */
    fun modifyPhone(countryCode: String, phone: String, code: String, callback: MyCallback) {
        val param = tokenParams("AppUpdateUser")
        param["CountryCode"] = countryCode
        param["PhoneNumber"] = phone
        param["VerificationCode"] = code
        updateUserInfo(param, callback, RequestCode.update_user_info)
    }


    /**
     * 账号注销
     */
    fun cancelAccount(callback: MyCallback) {
        val param = tokenParams("AppUserCancelAccount")
        tokenPost(param, callback, RequestCode.cancel_account)
    }

    /**
     * 意见反馈
     */
    fun feedback(advise: String, phone: String, pic: String, callback: MyCallback) {
        val param = tokenParams("AppUserFeedBack")
        param["Type"] = "advise"
        param["Desc"] = advise
        param["Contact"] = phone
        param["LogUrl"] = pic
        tokenPost(param, callback, RequestCode.feedback)
    }

    /**
     * 修改用户昵称
     */
    fun modifyAliasName(nickName: String, callback: MyCallback, reqCode: Int) {
        val param = tokenParams("AppUpdateUser")
        param["NickName"] = nickName
        updateUserInfo(param, callback, reqCode)
    }

    /**
     * 修改头像
     */
    fun modifyPortrait(avatar: String, callback: MyCallback) {
        val param = tokenParams("AppUpdateUser")
        param["Avatar"] = avatar
        updateUserInfo(param, callback, RequestCode.update_user_info)
    }

    /**
     * 用户信息
     */
    fun userInfo(callback: MyCallback) {
        tokenPost(tokenParams("AppGetUser"), callback, RequestCode.user_info)
    }

    /**
     * 消息列表
     * @param category 1设备，2家庭，3通知
     */
    fun messageList(
        category: Int,
        msgId: String,
        timestamp: Long,
        offset: Int,
        limit: Int,
        callback: MyCallback
    ) {
        val param = tokenParams("AppGetMessages")
        param["Category"] = category
        param["Limit"] = limit
        param["Offset"] = offset
        param["MsgID"] = msgId
        param["MsgTimestamp"] = timestamp
        tokenPost(param, callback, RequestCode.message_list)
    }

    /**
     * 删除消息
     */
    fun deleteMessage(msgId: String, callback: MyCallback) {
        val param = tokenParams("AppDeleteMessage")
        param["MsgID"] = msgId
        tokenPost(param, callback, RequestCode.delete_message)
    }

    /**
     * 退出登录
     */
    fun logout(callback: MyCallback) {
        tokenPost(tokenParams("AppLogoutUser"), callback, RequestCode.logout)
    }

    /**
     * 查询用户设置
     */
    fun getUserSetting(callback: MyCallback) {
        tokenPost(tokenParams("AppGetUserSetting"), callback, RequestCode.user_setting)
    }

    /**
     * 更新用户设置  0是不允许,1是允许
     */
    fun updateUserSetting(
        wechatPush: Int,
        devicePush: Int,
        familyPush: Int,
        notifyPush: Int,
        callback: MyCallback
    ) {
        val param = tokenParams("AppUpdateUserSetting")
        param["EnableWechatPush"] = wechatPush
        param["EnableDeviceMessagePush"] = devicePush
        param["EnableFamilyMessagePush"] = familyPush
        param["EnableNotifyMessagePush"] = notifyPush
        tokenPost(param, callback, RequestCode.update_user_setting)
    }

    /**
     *  查找手机用户
     */
    fun findPhoneUser(phone: String, countryCode: String, callback: MyCallback) {
        val param = tokenParams("AppFindUser")
        param["CountryCode"] = countryCode
        param["PhoneNumber"] = phone
        param["Type"] = "phone"
        tokenPost(param, callback, RequestCode.find_phone_user)
    }

    /**
     *  查找邮箱用户
     */
    fun findEmailUser(email: String, callback: MyCallback) {
        val param = tokenParams("AppFindUser")
        param["Email"] = email
        param["Type"] = "email"
        tokenPost(param, callback, RequestCode.find_email_user)
    }

    /**
     * 更新用户信息
     */
    private fun updateUserInfo(param: HashMap<String, Any>, callback: MyCallback, reqCode: Int) {
        tokenPost(param, callback, reqCode)
    }

    /**
     * 设置温度单位
     */
    fun setTemperatureUnit(unit: String, callback: MyCallback) {
        val param = tokenParams("AppUpdateUserSetting")
        param["TemperatureUnit"] = unit
        updateUserInfo(param, callback, RequestCode.set_unit_of_temperature)
    }

    /**
     * 设置时区
     */
    fun setRegion(region: String, callback: MyCallback) {
        val param = tokenParams("AppUpdateUserSetting")
        param["Region"] = region
        updateUserInfo(param, callback, RequestCode.set_region)
    }

    /**
     * 拉取全局配置
     */
    fun getGlobalConfig(key: String, callback: MyCallback) {
        val param = commonParams("AppGetGlobalConfig")
        param["Keys"] = key
        postJson(param, callback, RequestCode.get_global_config)
    }

    /*************************************  用户接口结束   **********************************************/


    /*************************************  家庭接口开始  ************************************************/

    /**
     * 请求获取家庭列表
     */
    fun familyList(offset: Int, callback: MyCallback) {
        val param = tokenParams("AppGetFamilyList")
        param["Offset"] = offset
        param["Limit"] = 50
        tokenPost(param, callback, RequestCode.family_list)
    }

    /**
     * 新增家庭
     */
    fun createFamily(familyName: String, address: String, callback: MyCallback) {
        val param = tokenParams("AppCreateFamily")
        param["Name"] = familyName
        param["Address"] = address
        tokenPost(param, callback, RequestCode.create_family)
    }

    /**
     * 房间列表
     */
    fun roomList(familyId: String, offset: Int, callback: MyCallback) {
        val param = tokenParams("AppGetRoomList")
        param["FamilyId"] = familyId
        param["Offset"] = offset
        param["Limit"] = 20
        tokenPost(param, callback, RequestCode.room_list)
    }

    /**
     * 创建房间
     */
    fun createRoom(familyId: String, roomName: String, callback: MyCallback) {
        val param = tokenParams("AppCreateRoom")
        param["Name"] = roomName
        param["FamilyId"] = familyId
        tokenPost(param, callback, RequestCode.create_room)
    }

    /**
     * 修改家庭
     */
    fun modifyFamily(familyId: String, familyName: String, address: String, callback: MyCallback) {
        val param = tokenParams("AppModifyFamily")
        param["FamilyId"] = familyId
        if (!TextUtils.isEmpty(familyName))
            param["Name"] = familyName
        if (!TextUtils.isEmpty(address))
            param["Address"] = address
        tokenPost(param, callback, RequestCode.modify_family)
    }

    /**
     * 修改房间
     */
    fun modifyRoom(familyId: String, roomId: String, roomName: String, callback: MyCallback) {
        val param = tokenParams("AppModifyRoom")
        param["FamilyId"] = familyId
        param["RoomId"] = roomId
        param["Name"] = roomName
        tokenPost(param, callback, RequestCode.modify_room)
    }

    /**
     * 更换房间
     */
    fun changeRoom(
        familyId: String,
        roomId: String,
        productId: String,
        deviceName: String,
        callback: MyCallback
    ) {
        val param = tokenParams("AppModifyFamilyDeviceRoom")
        param["FamilyId"] = familyId
        param["RoomId"] = roomId
        param["ProductId"] = productId
        param["DeviceName"] = deviceName
        tokenPost(param, callback, RequestCode.change_room)
    }

    /**
     * 删除家庭
     */
    fun deleteFamily(familyId: String, familyName: String, callback: MyCallback) {
        val param = tokenParams("AppDeleteFamily")
        param["FamilyId"] = familyId
        param["Name"] = familyName
        tokenPost(param, callback, RequestCode.delete_family)
    }

    /**
     * 删除房间
     */
    fun deleteRoom(familyId: String, roomId: String, callback: MyCallback) {
        val param = tokenParams("AppDeleteRoom")
        param["FamilyId"] = familyId
        param["RoomId"] = roomId
        tokenPost(param, callback, RequestCode.delete_room)
    }

    /**
     * 家庭详情
     */
    fun familyInfo(familyId: String, callback: MyCallback) {
        val param = tokenParams("AppDescribeFamily")
        param["FamilyId"] = familyId
        tokenPost(param, callback, RequestCode.family_info)
    }

    /**
     * 家庭成员列表
     */
    fun memberList(familyId: String, offset: Int, callback: MyCallback) {
        val param = tokenParams("AppGetFamilyMemberList")
        param["FamilyId"] = familyId
        param["Offset"] = offset
        param["Limit"] = 50
        tokenPost(param, callback, RequestCode.member_list)
    }

    /**
     * 移除家庭成员
     */
    fun deleteFamilyMember(familyId: String,memberId: String, callback: MyCallback) {
        val param = tokenParams("AppDeleteFamilyMember")
        param["FamilyId"] = familyId
        param["MemberID"] = memberId
        tokenPost(param, callback, RequestCode.delete_family_member)
    }

    /**
     * 成员加入家庭
     */
    fun joinFamily(shareToken: String, callback: MyCallback) {
        val param = tokenParams("AppJoinFamily")
        param["ShareToken"] = shareToken
        tokenPost(param, callback, RequestCode.join_family)
    }

    /**
     * 成员自动退出家庭
     */
    fun exitFamily(familyId: String, callback: MyCallback) {
        val param = tokenParams("AppExitFamily")
        param["FamilyId"] = familyId
        tokenPost(param, callback, RequestCode.exit_family)
    }

    /**
     *  发送邀请成员
     */
    fun sendFamilyInvite(familyId: String, userId: String, callback: MyCallback) {
        val param = tokenParams("AppSendShareFamilyInvite")
        param["FamilyId"] = familyId
        param["ToUserID"] = userId
        tokenPost(param, callback, RequestCode.send_family_invite)
    }

    /*************************************  家庭接口结束  ************************************************/


    /*************************************  设备接口开始  ************************************************/

    /**
     * 请求获取设备列表
     */
    fun deviceList(familyId: String, roomId: String, offset: Int, callback: MyCallback) {
        val param = tokenParams("AppGetFamilyDeviceList")
        param["FamilyId"] = familyId
        param["RoomId"] = roomId
        param["Offset"] = offset
        param["Limit"] = 20
        tokenPost(param, callback, RequestCode.device_list)
    }


    /**
     * 获取设备在线状态
     */
    fun deviceOnlineStatus(productId: String, deviceIds: ArrayList<String>, callback: MyCallback) {
//        val param = tokenParams("AppGetDeviceOnlineStatus")
        val param = tokenParams("AppGetDeviceStatuses")
        param["ProductId"] = productId
//        param["ListDeviceName"] = deviceIds
        param["DeviceIds"] = deviceIds
        tokenPost(param, callback, RequestCode.device_online_status)
    }

    /**
     * 修改设备别名
     */
    fun modifyDeviceAliasName(
        productId: String,
        deviceName: String,
        aliasName: String,
        callback: MyCallback
    ) {
        val param = tokenParams("AppUpdateDeviceInFamily")
        param["ProductId"] = productId
        param["DeviceName"] = deviceName
        param["AliasName"] = aliasName
        tokenPost(param, callback, RequestCode.modify_device_alias_name)
    }

    /**
     * 扫码绑定设备
     */
    fun scanBindDevice(familyId: String, roomId: String, signature: String, callback: MyCallback) {
        val param = tokenParams("AppSecureAddDeviceInFamily")
        param["FamilyId"] = familyId
        param["RoomId"] = roomId
        param["DeviceSignature"] = signature
        tokenPost(param, callback, RequestCode.scan_bind_device)
    }

    /**
     * 扫码绑定设备, 蓝牙签名绑定设备
     */
    fun sigBindDevice(
        familyId: String,
        roomId: String,
        deviceInfo: TrtcDeviceInfo,
        bindType: String,
        callback: MyCallback) {
        val param = tokenParams("AppSigBindDeviceInFamily")
        param["FamilyId"] = familyId
        param["RoomId"] = roomId
        param["DeviceId"] = deviceInfo.productId + "/" + deviceInfo.deviceName
        param["DeviceTimestamp"] = deviceInfo.timestamp
        param["ConnId"] = deviceInfo.connId
        param["SignMethod"] = deviceInfo.signMethod
        param["BindType"] = bindType
        param["Signature"] = deviceInfo.signature
        tokenPost(param, callback, RequestCode.sig_bind_device)
    }

    /**
     * WIFI配网绑定设备
     */
    fun wifiBindDevice(familyId: String, deviceInfo: DeviceInfo, callback: MyCallback) {
        val param = baseParams()

        param["RequestId"] = UUID.randomUUID().toString()
        param["ClientIp"] = IPUtil.getHostIP()
        param["Action"] = "AppTokenBindDeviceFamily"
        param["AccessToken"] = App.data.getToken()
        param["IotAppID"] = APP_KEY
        param["UserID"] = App.data.userInfo.UserID
        param["Token"] = App.data.bindDeviceToken
        param["FamilyId"] = familyId
        param["ProductId"] = deviceInfo.productId
        param["DeviceName"] = deviceInfo.deviceName
        param["RegionId"] = App.data.regionId
        tokenPost(param, callback, RequestCode.wifi_bind_device)
    }

    /**
     * 删除设备
     */
    fun deleteDevice(
        familyId: String,
        productId: String,
        deviceName: String,
        callback: MyCallback
    ) {
        val param = tokenParams("AppDeleteDeviceInFamily")
        param["FamilyId"] = familyId
        param["ProductId"] = productId
        param["DeviceName"] = deviceName
        tokenPost(param, callback, RequestCode.delete_device)
    }

    /**
     * 设备当前状态(如亮度、开关状态等)
     */
    fun deviceData(productId: String, deviceName: String, callback: MyCallback) {
        val param = tokenParams("AppGetDeviceData")
        param["ProductId"] = productId
        param["DeviceName"] = deviceName
        tokenPost(param, callback, RequestCode.device_data)
    }

    /**
     * 获取设备详情
     */
    fun getDeviceInfo(productId: String, deviceName: String, familyId: String, callback: MyCallback) {
        val param = tokenParams("AppGetDeviceInFamily")
        param["ProductId"] = productId
        param["DeviceName"] = deviceName
        param["FamilyId"] = familyId
        tokenPost(param, callback, RequestCode.get_device_info)
    }

    /**
     * 控制设备
     */
    fun controlDevice(productId: String, deviceName: String, data: String, callback: MyCallback) {
        val param = tokenParams("AppControlDeviceData")
        param["ProductId"] = productId
        param["DeviceName"] = deviceName
        param["Data"] = data
        tokenPost(param, callback, RequestCode.control_device)
    }

    /**
     * 当前产品控制面板风格主题
     */
    fun controlPanel(productIds: ArrayList<String>, callback: MyCallback) {
        val param = tokenParams("AppGetProductsConfig")
        param["ProductIds"] = productIds
        tokenPost(param, callback, RequestCode.control_panel)
    }

    /**
     * 当前设备对应的产品信息
     */
    fun deviceProducts(productIds: ArrayList<String>, callback: MyCallback) {
        val param = tokenParams("AppGetProducts")
        param["ProductIds"] = productIds
        tokenPost(param, callback, RequestCode.device_product)
    }

    /**
     * 发送设备分享
     */
    fun sendShareInvite(
        productId: String, deviceName: String, userId: String, callback: MyCallback
    ) {
        val param = tokenParams("AppSendShareDeviceInvite")
        param["ProductId"] = productId
        param["DeviceName"] = deviceName
        param["ToUserID"] = userId
        tokenPost(param, callback, RequestCode.send_share_invite)
    }

    /**
     * 获取绑定设备的 token
     */
    fun getBindDevToken(callback: MyCallback) {
        val param = baseParams()
        param["RequestId"] = UUID.randomUUID().toString()
        param["ClientIp"] = IPUtil.getHostIP()
        param["Action"] = "AppCreateDeviceBindToken"
        param["AccessToken"] = App.data.getToken()
        param["IotAppID"] = APP_KEY
        param["UserID"] = App.data.userInfo.UserID
        param["RegionId"] = App.data.regionId

        tokenPost(param, callback, RequestCode.get_bind_device_token)
    }

    /**
     * 检查设备绑定 token 的状态
     */
    fun checkDeviceBindTokenState(callback: MyCallback) {
        val param = baseParams()
        param["RequestId"] = UUID.randomUUID().toString()
        param["ClientIp"] = IPUtil.getHostIP()
        param["Action"] = "AppGetDeviceBindTokenState"
        param["AccessToken"] = App.data.getToken()
        param["IotAppID"] = APP_KEY
        param["UserID"] = App.data.userInfo.UserID
        param["Token"] = App.data.bindDeviceToken
        param["RegionId"] = App.data.regionId

        tokenPost(param, callback, RequestCode.check_device_bind_token_state)
    }

    /**
     * 手机请求加入房间
     */
    fun trtcCallDevice(deviceId: String, callback: MyCallback) {
        val param = tokenParams("App::IotRTC::CallDevice")
        param["DeviceId"] = deviceId
        tokenPost(param, callback, RequestCode.trtc_call_device)
    }

    /****************************************   设备接口结束  ************************************************/

    /******************************************   云端定时接口开始  *************************************************************/

    /**
     * 云端定时列表
     */
    fun timeList(productId: String, deviceName: String, offset: Int, callback: MyCallback) {
        val param = tokenParams("AppGetTimerList")
        param["ProductId"] = productId
        param["DeviceName"] = deviceName
        param["Offset"] = offset
        param["Limit"] = 20
        tokenPost(param, callback, RequestCode.time_list)
    }

    /**
     *  创建定时任务
     */
    fun createTimer(
        productId: String,
        deviceName: String,
        timerName: String,
        days: String,
        timePoint: String,
        repeat: Int,
        data: String,
        callback: MyCallback
    ) {
        val param = tokenParams("AppCreateTimer")
        param["ProductId"] = productId
        param["DeviceName"] = deviceName
        param["TimerName"] = timerName
        param["Days"] = days
        param["TimePoint"] = timePoint
        param["Repeat"] = repeat
        param["Data"] = data
        tokenPost(param, callback, RequestCode.create_timer)
    }

    /**
     *  修改定时任务
     *  @param days 定时器开启时间，每一位——0:关闭,1:开启, 从左至右依次表示: 周日 周一 周二 周三 周四 周五 周六 1000000
     *  @param repeat 是否循环，0表示不需要，1表示需要
     */
    fun modifyTimer(
        productId: String,
        deviceName: String,
        timerName: String,
        timerId: String,
        days: String,
        timePoint: String,
        repeat: Int,
        data: String,
        callback: MyCallback
    ) {
        val param = tokenParams("AppModifyTimer")
        param["ProductId"] = productId
        param["DeviceName"] = deviceName
        param["TimerId"] = timerId
        param["TimerName"] = timerName
        param["Days"] = days
        param["TimePoint"] = timePoint
        param["Repeat"] = repeat
        param["Data"] = data
        tokenPost(param, callback, RequestCode.modify_timer)
    }

    /**
     *  修改定时任务状态，打开或者关闭
     *  @param status 0 关闭，1 开启
     */
    fun modifyTimerStatus(
        productId: String,
        deviceName: String,
        timerId: String,
        status: Int,
        callback: MyCallback
    ) {
        val param = tokenParams("AppModifyTimerStatus")
        param["ProductId"] = productId
        param["DeviceName"] = deviceName
        param["TimerId"] = timerId
        param["Status"] = status
        tokenPost(param, callback, RequestCode.modify_timer_status)
    }

    /**
     *  删除定时
     */
    fun deleteTimer(productId: String, deviceName: String, timerId: String, callback: MyCallback) {
        val param = tokenParams("AppDeleteTimer")
        param["ProductId"] = productId
        param["DeviceName"] = deviceName
        param["TimerId"] = timerId
        tokenPost(param, callback, RequestCode.delete_timer)
    }

    /****************************************   云端定时接口结束  ************************************************/


    /**
     * 上传图片第一步获取签名
     */
    fun appCosAuth(callback: MyCallback) {
        val param = tokenParams("AppCosAuth")
        param["path"] = "iotexplorer-app-logs/user_{uin}/"
        tokenAppCosAuth(param, callback, RequestCode.app_cos_auth)
    }


    /****************************************   设备分享接口开始  ************************************************/

    /**
     * 设备分享的设备列表(返回的是设备列表)
     */
    fun shareDeviceList(offset: Int, callback: MyCallback) {
        val param = tokenParams("AppListUserShareDevices")
        param["Offset"] = offset
        param["Limit"] = 50
        tokenPost(param, callback, RequestCode.share_device_list)
    }

    /**
     * 设备分享的用户列表(返回的是用户列表)
     */
    fun shareUserList(productId: String, deviceName: String, offset: Int, callback: MyCallback) {
        val param = tokenParams("AppListShareDeviceUsers")
        param["ProductId"] = productId
        param["DeviceName"] = deviceName
        param["Offset"] = offset
        param["Limit"] = 100
        tokenPost(param, callback, RequestCode.share_user_list)
    }

    /**
     * 删除分享列表的某个设备(删除某个已经分享的设备)
     */
    fun deleteShareDevice(
        productId: String,
        deviceName: String,
        callback: MyCallback
    ) {
        val param = tokenParams("AppRemoveUserShareDevice")
        param["ProductId"] = productId
        param["DeviceName"] = deviceName
        tokenPost(param, callback, RequestCode.delete_share_device)
    }

    /**
     * 删除一个设备的分享用户列表中的某个用户(删除某个用户的分享权限)
     */
    fun deleteShareUser(
        productId: String,
        deviceName: String,
        userId: String,
        callback: MyCallback
    ) {
        val param = tokenParams("AppRemoveShareDeviceUser")
        param["ProductId"] = productId
        param["DeviceName"] = deviceName
        param["RemoveUserID"] = userId
        tokenPost(param, callback, RequestCode.delete_share_user)
    }

    /**
     * 获取分享票据
     */
    fun getShareTicket(callback: MyCallback) {
        val param = tokenParams("AppGetTokenTicket")
        tokenPost(param, callback, RequestCode.share_ticket)
    }

    fun getOneTimeTokenTicket(callback: MyCallback) {
        val param = tokenParams("AppGetTokenTicket")
        getH5tokenPost(param, callback, RequestCode.token_ticket)
    }

    /**
     * 票据换取token
     */
    fun ticketToToken(shareTicket: String, callback: MyCallback) {
        val param = commonParams("AppCheckInTokenTicket")
        param["TokenTicket"] = shareTicket
        postJson(param, callback, RequestCode.ticket_to_token)
    }

    /**
     * 获取设备分享Token
     */
    fun getShareToken(
        familyId: String,
        productId: String,
        deviceName: String,
        callback: MyCallback
    ) {
        val param = tokenParams("AppCreateShareDeviceToken")
        param["FamilyId"] = familyId
        param["ProductId"] = productId
        param["DeviceName"] = deviceName
        tokenPost(param, callback, RequestCode.get_share_token)
    }

    /**
     * 获取设备分享 Token 信息
     */
    fun getShareDeviceInfo(deviceToken: String, callback: MyCallback) {
        val param = tokenParams("AppDescribeShareDeviceToken")
        param["ShareDeviceToken"] = deviceToken
        tokenPost(param, callback, RequestCode.get_share_device_info)
    }

    /**
     * 绑定分享设备(绑定操作)
     */
    fun bindShareDevice(
        productId: String,
        deviceName: String,
        deviceToken: String,
        callback: MyCallback
    ) {
        val param = tokenParams("AppBindUserShareDevice")
        param["ProductId"] = productId
        param["DeviceName"] = deviceName
        param["ShareDeviceToken"] = deviceToken
        tokenPost(param, callback, RequestCode.bind_share_device)
    }

    /****************************************   设备分享接口结束   *******************************************************/

    /****************************************   设备推荐接口开始   *******************************************************/

    /**
     * 获取产品推荐父类别列表
     */
    fun getParentCategoryList(callback: MyCallback) {
        val param = tokenParams("AppGetParentCategoryList")
        tokenPost(param, callback, RequestCode.get_parent_category_list)
    }

    /**
     * 推荐产品子类别列表
     */
    fun getRecommList(
        categoryKey: String,
        callback: MyCallback
    ) {
        val param = tokenParams("AppGetRecommList")
        param["CategoryKey"] = categoryKey
        tokenPost(param, callback, RequestCode.get_recommend_device_list)
    }

    fun getProductsConfig(productIds: List<String>, callback: MyCallback) {
        val param = tokenParams("AppGetProductsConfig")
        param["ProductIds"] = productIds
        tokenPost(param, callback, RequestCode.get_products_config)
    }
    /****************************************   设备推荐接口结束   *******************************************************/


    /****************************************   场景联动接口开始   *******************************************************/
    fun createManualTask(sceneEntity: SceneEntity, callback: MyCallback) {
        val param = tokenParams("AppCreateScene")
        param["SceneName"] = sceneEntity.sceneName
        param["SceneIcon"] = sceneEntity.sceneIcon
        param["FamilyId"] = sceneEntity.familyId
        param["Actions"] = sceneEntity.actions!!
        tokenPost(param, callback, RequestCode.create_manual_task)
    }

    fun updateManualTask(sceneEntity: SceneEntity, callback: MyCallback) {
        val param = tokenParams("AppModifyScene")
        param["SceneName"] = sceneEntity.sceneName
        param["SceneIcon"] = sceneEntity.sceneIcon
        param["Actions"] = sceneEntity.actions!!
        param["SceneId"] = sceneEntity.sceneId
        tokenPost(param, callback, RequestCode.update_manual_task)
    }

    fun createAutomicTask(automicTaskEntity: AutomicTaskEntity, callback: MyCallback) {
        val param = tokenParams("AppCreateAutomation")
        param["Name"] = automicTaskEntity.name
        param["Icon"] = automicTaskEntity.icon
        param["FamilyId"] = automicTaskEntity.familyId
        param["Actions"] = automicTaskEntity.actions!!
        param["Status"] = automicTaskEntity.status
        param["MatchType"] = automicTaskEntity.matchType
        param["Conditions"] = automicTaskEntity.conditions!!
        param["EffectiveBeginTime"] = String.format("%02d:%02d", automicTaskEntity.workTimeMode.startTimeHour,
            automicTaskEntity.workTimeMode.startTimerMin)
        param["EffectiveEndTime"] = String.format("%02d:%02d", automicTaskEntity.workTimeMode.endTimeHour,
            automicTaskEntity.workTimeMode.endTimeMin)
        if (automicTaskEntity.workTimeMode.workDayType == 3) {
            param["EffectiveDays"] = automicTaskEntity.workTimeMode.workDays
        } else if (automicTaskEntity.workTimeMode.workDayType == 2) {
            param["EffectiveDays"] = "1111111"
        } else if (automicTaskEntity.workTimeMode.workDayType == 1) {
            param["EffectiveDays"] = "0111110"
        } else if (automicTaskEntity.workTimeMode.workDayType == 0) {
            param["EffectiveDays"] = "1000001"
        }

        Log.e("XXX", "----------> \n" + JSON.toJSONString(param))
        tokenPost(param, callback, RequestCode.create_automic_task)
    }

    fun updateAutomicTask(automicTaskEntity: AutomicTaskEntity, callback: MyCallback) {
        val param = tokenParams("AppModifyAutomation")
        param["Name"] = automicTaskEntity.name
        param["Icon"] = automicTaskEntity.icon
        param["AutomationId"] = automicTaskEntity.automationId
        param["Actions"] = automicTaskEntity.actions!!
        param["Status"] = automicTaskEntity.status
//        param["FamilyId"] = automicTaskEntity.familyId
        param["MatchType"] = automicTaskEntity.matchType
        param["Conditions"] = automicTaskEntity.conditions!!
        param["EffectiveBeginTime"] = String.format("%02d:%02d", automicTaskEntity.workTimeMode.startTimeHour,
            automicTaskEntity.workTimeMode.startTimerMin)
        param["EffectiveEndTime"] = String.format("%02d:%02d", automicTaskEntity.workTimeMode.endTimeHour,
            automicTaskEntity.workTimeMode.endTimeMin)
        if (automicTaskEntity.workTimeMode.workDayType == 3) {
            param["EffectiveDays"] = automicTaskEntity.workTimeMode.workDays
        } else if (automicTaskEntity.workTimeMode.workDayType == 2) {
            param["EffectiveDays"] = "1111111"
        } else if (automicTaskEntity.workTimeMode.workDayType == 1) {
            param["EffectiveDays"] = "0111110"
        } else if (automicTaskEntity.workTimeMode.workDayType == 0) {
            param["EffectiveDays"] = "1000001"
        }

        tokenPost(param, callback, RequestCode.update_automic_task)
    }

    fun queryAutomicTask(familyId: String, callback: MyCallback) {
        val param = tokenParams("AppGetAutomationList")
        param["FamilyId"] = familyId
        tokenPost(param, callback, RequestCode.query_all_automic_task)
    }

    fun queryManualTask(familyId: String, offset: Int, callback: MyCallback) {
        val param = tokenParams("AppGetSceneList")
        param["FamilyId"] = familyId
        param["Offset"] = offset
        param["Limit"] = 999
        tokenPost(param, callback, RequestCode.query_all_manual_task)
    }

    fun runManualTask(sceneId: String, callback: MyCallback) {
        val param = tokenParams("AppRunScene")
        param["SceneId"] = sceneId
        tokenPost(param, callback, RequestCode.run_manual_task)
    }

    fun delManualTask(sceneId: String, callback: MyCallback) {
        val param = tokenParams("AppDeleteScene")
        param["SceneId"] = sceneId
        tokenPost(param, callback, RequestCode.del_manual_task)
    }

    fun delAutomicTask(automationId: String, callback: MyCallback) {
        val param = tokenParams("AppDeleteAutomation")
        param["AutomationId"] = automationId
        tokenPost(param, callback, RequestCode.del_automic_task)
    }

    fun getAutomicTaskDetail(automationId: String, callback: MyCallback) {
        val param = tokenParams("AppDescribeAutomation")
        param["AutomationId"] = automationId
        tokenPost(param, callback, RequestCode.get_automic_task_detail)
    }

    // status 0:开启 1:关闭
    fun updateAutomicTaskStatus(automationId: String, status: Int, callback: MyCallback) {
        val param = tokenParams("AppModifyAutomationStatus")
        param["AutomationId"] = automationId
        param["Status"] = status
        tokenPost(param, callback, RequestCode.update_automic_task_status)
    }

    fun getTaskRunLog(msgId: String, familyId: String, callback: MyCallback) {
        val param = tokenParams("AppGetSceneAndAutomationLogs")
        param["Limit"] = 20
        param["FamilyId"] = familyId
        if (!TextUtils.isEmpty(msgId)) {
            param["MsgId"] = msgId
        }
        tokenPost(param, callback, RequestCode.get_run_task_log)
    }
    /****************************************   场景联动接口结束   *******************************************************/

}