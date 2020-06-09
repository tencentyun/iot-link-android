package com.tencent.iot.explorer.link.core.auth.impl

import com.tencent.iot.explorer.link.core.auth.callback.MyCallback

interface ShareImpl {

    /**
     * 设备分享的设备列表(返回的是设备列表)
     */
    fun shareDeviceList(offset: Int, callback: MyCallback)

    /**
     * 设备分享的用户列表(返回的是用户列表)
     */
    fun shareUserList(productId: String, deviceName: String, offset: Int, callback: MyCallback)

    /**
     * 删除分享列表的某个设备(删除某个已经分享的设备)
     */
    fun deleteShareDevice(productId: String, deviceName: String, callback: MyCallback)

    /**
     * 删除一个设备的分享用户列表中的某个用户(删除某个用户的分享权限)
     */
    fun deleteShareUser(productId: String, deviceName: String, userId: String, callback: MyCallback)

    /**
     * 发送分享邀请
     */
    fun sendShareDevice(productId: String,deviceName: String,userId: String,callback: MyCallback)

    /**
     * 绑定分享设备
     */
    fun bindShareDevice(
        productId: String, deviceName: String, deviceToken: String, callback: MyCallback
    )

}