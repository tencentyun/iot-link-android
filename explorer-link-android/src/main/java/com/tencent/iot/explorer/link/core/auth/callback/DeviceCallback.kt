package com.tencent.iot.explorer.link.core.auth.callback

import com.tencent.iot.explorer.link.core.auth.entity.DeviceEntity

/**
 *  设备列表callback
 */
interface DeviceCallback {

    /**
     * 列表请求成功
     */
    fun success(deviceList: List<DeviceEntity>)

    /**
     * 在线状态获取成功
     */
    fun onlineUpdate()

    /**
     * 请求失败
     */
    fun fail(message: String)

}