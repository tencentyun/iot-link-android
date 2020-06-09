package com.tenext.auth.impl

import com.tenext.auth.callback.MyCallback

interface MessageImpl {

    /**
     * 绑定信鸽
     */
    fun bindXG(xgToken: String, callback: MyCallback)

    /**
     * 解除绑定信鸽
     */
    fun unbindXG(xgToken: String, callback: MyCallback)

    /**
     * 消息列表
     * @param category 1设备，2家庭，3通知
     */
    fun messageList(category: Int, msgId: String, timestamp: Long, callback: MyCallback)

    /**
     * 消息列表
     * @param category 1设备，2家庭，3通知
     */
    fun messageList(category: Int, msgId: String, timestamp: Long, limit: Int, callback: MyCallback)


    /**
     * 删除消息
     */
    fun deleteMessage(msgId: String, callback: MyCallback)


    /**
     * 查询用户设置
     */
    fun getUserSetting(callback: MyCallback)

    /**
     * 更新用户设置  0是不允许,1是允许
     */
    fun updateUserSetting(
        wechatPush: Int,
        devicePush: Int,
        familyPush: Int,
        notifyPush: Int,
        callback: MyCallback
    )

}