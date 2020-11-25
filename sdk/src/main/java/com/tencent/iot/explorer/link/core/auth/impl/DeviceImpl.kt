package com.tencent.iot.explorer.link.core.auth.impl

import com.tencent.iot.explorer.link.core.auth.callback.ControlPanelCallback
import com.tencent.iot.explorer.link.core.auth.callback.DeviceCallback
import com.tencent.iot.explorer.link.core.auth.callback.MyCallback
import com.tencent.iot.explorer.link.core.auth.entity.*
import com.tencent.iot.explorer.link.core.link.entity.DeviceInfo
import java.util.*

interface DeviceImpl {

    /**
     * 面板数据列表
     */
    fun panelList(): ArrayList<ControlPanel>

    /**
     * 面板数据模板
     */
    fun panelConfig(): ConfigEntity?

    /**
     * 设备产品信息
     */
    fun product(): ProductEntity?

    /**
     * 清空数据
     */
    fun clearData()

    /**
     * 请求获取设备列表
     */
    fun deviceList(familyId: String, roomId: String, offset: Int, callback: MyCallback)

    /**
     * 请求获取设备列表
     */
    fun deviceList(familyId: String, roomId: String, offset: Int, limit: Int, callback: MyCallback)

    /**
     * 请求获取设备列表(整合在线状态)
     */
    fun deviceList(familyId: String, roomId: String, offset: Int, callback: DeviceCallback)

    /**
     * 请求获取设备列表(整合在线状态)
     */
    fun deviceList(
        familyId: String, roomId: String, offset: Int, limit: Int, callback: DeviceCallback
    )

    /**
     * 获取设备在线状态
     */
    fun deviceOnlineStatus(deviceIds: ArrayList<String>, callback: MyCallback)

    /**
     * 修改设备别名
     */
    fun modifyDeviceAlias(
        productId: String, deviceName: String, aliasName: String, callback: MyCallback
    )

    /**
     * 更换房间
     */
    fun changeRoom(
        familyId: String, roomId: String, productId: String, deviceName: String, callback: MyCallback
    )

    /**
     * 扫码绑定设备
     */
    fun scanBindDevice(familyId: String, signature: String, callback: MyCallback)

    /**
     * WIFI配网绑定设备
     */
    fun wifiBindDevice(
        familyId: String, productId: String, deviceName: String, signature: String,
        timestamp: Long, connId: String, callback: MyCallback
    )

    /**
     * WIFI配网绑定设备
     */
    fun wifiBindDevice(familyId: String, deviceInfo: DeviceInfo, callback: MyCallback)

    /**
     * 删除设备
     */
    fun deleteDevice(familyId: String, productId: String, deviceName: String, callback: MyCallback)

    /**
     * 设备当前状态(如亮度、开关状态等)
     */
    fun deviceData(productId: String, deviceName: String, callback: MyCallback)

    /**
     * 获取设备详情
     */
    fun getDeviceInfo(productId: String, deviceName: String, callback: MyCallback)

    /**
     * 控制设备
     */
    fun controlDevice(productId: String, deviceName: String, data: String, callback: MyCallback)

    /**
     * 当前产品控制面板风格主题/面板数据
     */
    fun controlPanel(productIds: ArrayList<String>, callback: MyCallback)

    /**
     * 设备控制面板
     */
    fun controlPanel(productId: String, deviceName: String, callback: ControlPanelCallback)

    /**
     * 当前设备对应的产品信息
     */
    fun deviceProducts(productIds: ArrayList<String>, callback: MyCallback)

    /**
     * 获取绑定设备的 token
     */
    fun getBindDevToken(userId: String, callback: MyCallback)

    /**
     * 检查设备绑定 token 的状态
     */
    fun checkDeviceBindTokenState(userId: String, bindDeviceToken: String, callback: MyCallback)

    fun wifiBindDevice(userId: String, bindDeviceToken: String, familyId: String, deviceInfo: DeviceInfo, callback: MyCallback)

    /**
     * 手机请求加入房间
     */
    fun trtcCallDevice(deviceId: String, callback: MyCallback)

}