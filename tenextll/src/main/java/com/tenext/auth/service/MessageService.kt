package com.tenext.auth.service

import com.tenext.auth.callback.MyCallback
import com.tenext.auth.consts.RequestCode
import com.tenext.auth.impl.MessageImpl

internal class MessageService : BaseService(), MessageImpl {

    /**
     * 绑定信鸽
     */
    override fun bindXG(xgToken: String, callback: MyCallback) {
        val param = tokenParams("AppBindXgToken")
        param["Token"] = xgToken
        tokenPost(param, callback, RequestCode.bind_xg)
    }

    /**
     * 解除绑定信鸽
     */
    override fun unbindXG(xgToken: String, callback: MyCallback) {
        val param = tokenParams("AppUnBindXgToken")
        param["Token"] = xgToken
        tokenPost(param, callback, RequestCode.unbind_xg)
    }

    override fun messageList(category: Int, msgId: String, timestamp: Long, callback: MyCallback) {
        messageList(category, msgId, timestamp, 20, callback)
    }

    /**
     * 消息列表
     * @param category 1设备，2家庭，3通知
     */
    override fun messageList(
        category: Int,
        msgId: String,
        timestamp: Long,
        limit: Int,
        callback: MyCallback
    ) {
        val param = tokenParams("AppGetMessages")
        param["Category"] = category
        param["Limit"] = limit
        param["MsgID"] = msgId
        param["MsgTimestamp"] = timestamp
        tokenPost(param, callback, RequestCode.message_list)
    }

    /**
     * 删除消息
     */
    override fun deleteMessage(msgId: String, callback: MyCallback) {
        val param = tokenParams("AppDeleteMessage")
        param["MsgID"] = msgId
        tokenPost(param, callback, RequestCode.delete_message)
    }


    /**
     * 查询用户设置
     */
    override fun getUserSetting(callback: MyCallback) {
        tokenPost(
            tokenParams("AppGetUserSetting"), callback,
            RequestCode.user_setting
        )
    }

    /**
     * 更新用户设置  0是不允许,1是允许
     */
    override fun updateUserSetting(
        wechatPush: Int, devicePush: Int, familyPush: Int, notifyPush: Int, callback: MyCallback
    ) {
        val param = tokenParams("AppUpdateUserSetting")
        param["EnableWechatPush"] = wechatPush
        param["EnableDeviceMessagePush"] = devicePush
        param["EnableFamilyMessagePush"] = familyPush
        param["EnableNotifyMessagePush"] = notifyPush
        tokenPost(param, callback, RequestCode.update_user_setting)
    }

}