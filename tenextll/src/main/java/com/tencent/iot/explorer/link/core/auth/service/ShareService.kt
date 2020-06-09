package com.tenext.auth.service

import com.tenext.auth.callback.MyCallback
import com.tenext.auth.consts.RequestCode
import com.tenext.auth.impl.ShareImpl

internal class ShareService : BaseService(), ShareImpl {

    /**
     * 设备分享的设备列表(返回的是设备列表)
     */
    override fun shareDeviceList(offset: Int, callback: MyCallback) {
        val param = tokenParams("AppListUserShareDevices")
        param["Offset"] = offset
        param["Limit"] = 20
        tokenPost(param, callback, RequestCode.share_device_list)
    }

    /**
     * 设备分享的用户列表(返回的是用户列表)
     */
    override fun shareUserList(
        productId: String, deviceName: String, offset: Int, callback: MyCallback
    ) {
        val param = tokenParams("AppListShareDeviceUsers")
        param["ProductId"] = productId
        param["DeviceName"] = deviceName
        param["Offset"] = offset
        param["Limit"] = 20
        tokenPost(param, callback, RequestCode.share_user_list)
    }

    /**
     * 删除分享列表的某个设备(删除某个已经分享的设备)
     */
    override fun deleteShareDevice(productId: String, deviceName: String, callback: MyCallback) {
        val param = tokenParams("AppRemoveUserShareDevice")
        param["ProductId"] = productId
        param["DeviceName"] = deviceName
        tokenPost(param, callback, RequestCode.delete_share_device)
    }

    /**
     * 删除一个设备的分享用户列表中的某个用户(删除某个用户的分享权限)
     */
    override fun deleteShareUser(
        productId: String, deviceName: String, userId: String, callback: MyCallback
    ) {
        val param = tokenParams("AppRemoveShareDeviceUser")
        param["ProductId"] = productId
        param["DeviceName"] = deviceName
        param["RemoveUserID"] = userId
        tokenPost(param, callback, RequestCode.delete_share_user)
    }

    override fun sendShareDevice(
        productId: String,
        deviceName: String,
        userId: String,
        callback: MyCallback
    ) {
        val param = tokenParams("AppSendShareDeviceInvite")
        param["ProductId"] = productId
        param["DeviceName"] = deviceName
        param["ToUserID"] = userId
        tokenPost(param, callback, RequestCode.send_share_invite)
    }

    /**
     * 绑定分享设备
     */
    override fun bindShareDevice(
        productId: String, deviceName: String, deviceToken: String, callback: MyCallback
    ) {
        val param = tokenParams("AppBindUserShareDevice")
        param["ProductId"] = productId
        param["DeviceName"] = deviceName
        param["ShareDeviceToken"] = deviceToken
        tokenPost(param, callback, RequestCode.bind_share_device)
    }

}