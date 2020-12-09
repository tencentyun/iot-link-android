package com.tencent.iot.explorer.link.core.auth.service

import com.tencent.iot.explorer.link.core.auth.IoTAuth
import com.tencent.iot.explorer.link.core.auth.callback.ControlPanelCallback
import com.tencent.iot.explorer.link.core.auth.callback.DeviceCallback
import com.tencent.iot.explorer.link.core.auth.callback.MyCallback
import com.tencent.iot.explorer.link.core.auth.consts.RequestCode
import com.tencent.iot.explorer.link.core.auth.entity.*
import com.tencent.iot.explorer.link.core.auth.impl.DeviceImpl
import com.tencent.iot.explorer.link.core.auth.response.*
import com.tencent.iot.explorer.link.core.link.entity.DeviceInfo
import com.tencent.iot.explorer.link.core.utils.IPUtil
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

internal class DeviceService : BaseService(), DeviceImpl {

    /**
     * 面板数据
     */
    private val panelList = arrayListOf<ControlPanel>()

    /**
     * 面板初始对象
     */
    private var panelConfig: ConfigEntity? = null
    private var hasPanel = false

    /**
     * 设备产品数据
     */
    private var product: ProductEntity? = null
    private var hasProduct = false

    private var deviceName = ""

    override fun panelList(): ArrayList<ControlPanel> {
        return panelList
    }

    override fun panelConfig(): ConfigEntity? {
        return panelConfig
    }

    override fun product(): ProductEntity? {
        return product
    }

    override fun clearData() {
        panelList.clear()
        panelConfig = null
        product = null
    }

    /**
     * 请求获取设备列表
     */
    override fun deviceList(familyId: String, roomId: String, offset: Int, callback: MyCallback) {
        deviceList(familyId, roomId, offset, 20, callback)
    }

    override fun deviceList(
        familyId: String, roomId: String, offset: Int, limit: Int, callback: MyCallback
    ) {
        val param = tokenParams("AppGetFamilyDeviceList")
        param["FamilyId"] = familyId
        param["RoomId"] = roomId
        param["Offset"] = offset
        param["Limit"] = limit
        tokenPost(param, callback, RequestCode.device_list)
    }

    /**
     * 请求获取设备列表,返回设备列表，包括在线状态(默认分页大小20条)
     */
    override fun deviceList(
        familyId: String, roomId: String, offset: Int, callback: DeviceCallback
    ) {
        deviceList(familyId, roomId, offset, 20, callback)
    }

    /**
     * 请求获取设备列表,返回设备列表，包括在线状态
     */
    override fun deviceList(
        familyId: String, roomId: String, offset: Int, limit: Int, callback: DeviceCallback
    ) {
        deviceList(familyId, roomId, offset, limit, object : MyCallback {
            override fun fail(msg: String?, reqCode: Int) {
                callback.fail(msg ?: "")
            }

            override fun success(response: BaseResponse, reqCode: Int) {
                if (response.isSuccess()) {
                    response.parse(DeviceListResponse::class.java)?.run {
                        IoTAuth.deviceList.addAll(DeviceList)
                        //列表获得成功回调
                        callback.success(DeviceList)
                        //在线状态
                        val size = IoTAuth.deviceList.size
                        val index = size - DeviceList.size
                        getOnlineStatus(index, size, callback)
                    }
                }
            }
        })
    }

    /**
     * 获得在线状态
     */
    private fun getOnlineStatus(index: Int, size: Int, callback: DeviceCallback) {
        val deviceIds = arrayListOf<String>()
        for (i in index until size) {
            IoTAuth.deviceList[i].let {
                deviceIds.add(it.DeviceId)
            }
        }
        if (deviceIds.isEmpty()) return
        deviceOnlineStatus(deviceIds, object : MyCallback {
            override fun fail(msg: String?, reqCode: Int) {
                callback.fail(msg ?: "")
            }

            override fun success(response: BaseResponse, reqCode: Int) {
                if (response.isSuccess()) {
                    response.parse(DeviceOnlineResponse::class.java)?.run {
                        if (!DeviceStatuses.isNullOrEmpty()) {
                            for (i in index until size) {
                                IoTAuth.deviceList[i].run {
                                    run check@{
                                        DeviceStatuses!!.forEach {
                                            if (DeviceId == it.DeviceId) {
                                                online = it.Online
                                                return@check
                                            }
                                        }
                                    }
                                }
                            }
                            callback.onlineUpdate()
                        }
                    }
                }
            }
        })
    }

    /**
     * 获取设备在线状态
     */
    override fun deviceOnlineStatus(deviceIds: ArrayList<String>, callback: MyCallback) {
        val param = tokenParams("AppGetDeviceStatuses")
        param["DeviceIds"] = deviceIds
        tokenPost(param, callback, RequestCode.device_online_status)
    }

    /**
     * 修改设备别名
     */
    override fun modifyDeviceAlias(
        productId: String, deviceName: String, aliasName: String, callback: MyCallback
    ) {
        val param = tokenParams("AppUpdateDeviceInFamily")
        param["ProductId"] = productId
        param["DeviceName"] = deviceName
        param["AliasName"] = aliasName
        tokenPost(param, callback, RequestCode.modify_device_alias_name)
    }

    override fun changeRoom(
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
     * 扫码绑定设备
     */
    override fun scanBindDevice(familyId: String, signature: String, callback: MyCallback) {
        val param = tokenParams("AppSecureAddDeviceInFamily")
        param["FamilyId"] = familyId
        param["DeviceSignature"] = signature
        tokenPost(param, callback, RequestCode.scan_bind_device)
    }

    /**
     * WIFI配网绑定设备
     */
    override fun wifiBindDevice(
        familyId: String, productId: String, deviceName: String, signature: String, timestamp: Long,
        connId: String, callback: MyCallback
    ) {
        val param = tokenParams("AppSigBindDeviceInFamily")
        param["FamilyId"] = familyId
        param["ProductId"] = productId
        param["DeviceName"] = deviceName
        param["Signature"] = signature
        param["DeviceTimestamp"] = timestamp
        param["ConnId"] = connId
        tokenPost(param, callback, RequestCode.wifi_bind_device)
    }

    /**
     * WIFI配网绑定设备
     */
    override fun wifiBindDevice(familyId: String, deviceInfo: DeviceInfo, callback: MyCallback) {
        val param = tokenParams("AppSigBindDeviceInFamily")
        param["FamilyId"] = familyId
        param["ProductId"] = deviceInfo.productId
        param["DeviceName"] = deviceInfo.deviceName
        param["Signature"] = deviceInfo.signature
        param["DeviceTimestamp"] = deviceInfo.timestamp
//        param["ConnId"] = deviceInfo.connId
        tokenPost(param, callback, RequestCode.wifi_bind_device)
    }

    /**
     * 删除设备
     */
    override fun deleteDevice(
        familyId: String, productId: String, deviceName: String, callback: MyCallback
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
    override fun deviceData(productId: String, deviceName: String, callback: MyCallback) {
        val param = tokenParams("AppGetDeviceData")
        param["ProductId"] = productId
        param["DeviceName"] = deviceName
        tokenPost(param, callback, RequestCode.device_data)
    }

    /**
     * 获取设备详情
     */
    override fun getDeviceInfo(productId: String, deviceName: String, callback: MyCallback) {
        val param = tokenParams("AppGetDeviceInFamily")
        param["ProductId"] = productId
        param["DeviceName"] = deviceName
        tokenPost(param, callback, RequestCode.get_device_info)
    }

    /**
     * 控制设备
     */
    override fun controlDevice(
        productId: String, deviceName: String, data: String, callback: MyCallback
    ) {
        val param = tokenParams("AppControlDeviceData")
        param["ProductId"] = productId
        param["DeviceName"] = deviceName
        param["Data"] = data
        tokenPost(param, callback, RequestCode.control_device)
    }

    /**
     * 产品控制面板风格主题
     */
    override fun controlPanel(productIds: ArrayList<String>, callback: MyCallback) {
        val param = tokenParams("AppGetProductsConfig")
        param["ProductIds"] = productIds
        tokenPost(param, callback, RequestCode.control_panel)
    }

    /**
     * 当前设备对应的产品信息
     */
    override fun deviceProducts(productIds: ArrayList<String>, callback: MyCallback) {
        val param = tokenParams("AppGetProducts")
        param["ProductIds"] = productIds
        tokenPost(param, callback, RequestCode.device_product)
    }

    /**
     * 当前产品控制面板风格主题
     */
    override fun controlPanel(
        productId: String, deviceName: String, callback: ControlPanelCallback
    ) {
        this.deviceName = deviceName
        //产品面板数据
        controlPanel(arrayListOf(productId), object : MyCallback {
            override fun fail(msg: String?, reqCode: Int) {
                callback.fail(msg ?: "")
            }

            override fun success(response: BaseResponse, reqCode: Int) {
                response.parse(ControlPanelResponse::class.java)?.Data?.let {
                    if (it.isNotEmpty()) {
                        it[0].parse().run {
                            panelConfig = this.configEntity
                            hasPanel = true
                            //合并数据
                            mergeData(callback)
                        }
                    }
                }
            }
        })
        //设备产品信息
        deviceProducts(arrayListOf(productId), object : MyCallback {
            override fun fail(msg: String?, reqCode: Int) {
                callback.fail(msg ?: "")
            }

            override fun success(response: BaseResponse, reqCode: Int) {
                response.parse(DeviceProductResponse::class.java)?.run {
                    if (Products.isNotEmpty()) {
                        Products[0].run {
                            parseTemplate()
                            product = this
                            hasProduct = true
                            //合并数据
                            mergeData(callback)
                        }
                    }
                }
            }
        })
    }

    /**
     * 获取绑定设备的 token
     */
    override fun getBindDevToken(userId: String, callback: MyCallback) {
        val param = tokenParams("AppCreateDeviceBindToken")
        param["ClientIp"] = IPUtil.getHostIP()
        param["IotAppID"] = IoTAuth.APP_KEY
        param["UserID"] = userId

        tokenPost(param, callback, RequestCode.get_bind_device_token)
    }

    /**
     * 检查设备绑定 token 的状态
     */
    override fun checkDeviceBindTokenState(userId: String, bindDeviceToken: String, callback: MyCallback) {
        val param = HashMap<String, Any>()
        param["RequestId"] = UUID.randomUUID().toString()
        param["Action"] = "AppGetDeviceBindTokenState"
        param["AccessToken"] = IoTAuth.user.Token

        param["ClientIp"] = IPUtil.getHostIP()
        param["IotAppID"] = IoTAuth.APP_KEY
        param["UserID"] = userId
        param["Token"] = bindDeviceToken
        tokenPost(param, callback, RequestCode.check_device_bind_token_state)
    }

    /**
     * 手机请求加入房间
     */
    override fun trtcCallDevice(deviceId: String, callback: MyCallback) {
        val param = tokenParams("App::IotRTC::CallDevice")
        param["DeviceId"] = deviceId
        tokenPost(param, callback, RequestCode.trtc_call_device)
    }

    override fun wifiBindDevice(userId: String, bindDeviceToken: String, familyId: String,
                                deviceInfo: DeviceInfo, callback: MyCallback) {
        val param = tokenParams("AppTokenBindDeviceFamily")
        param["ClientIp"] = IPUtil.getHostIP()
        param["Action"] = "AppTokenBindDeviceFamily"
        param["IotAppID"] = IoTAuth.APP_KEY
        param["UserID"] = userId
        param["Token"] = bindDeviceToken
        param["FamilyId"] = familyId
        param["ProductId"] = deviceInfo.productId
        param["DeviceName"] = deviceInfo.deviceName

        tokenPost(param, callback, RequestCode.wifi_bind_device)
    }

    /**
     * 合并面板及产品数据
     */
    @Synchronized
    private fun mergeData(callback: ControlPanelCallback) {
        if (!hasPanel || !hasProduct) return
        hasPanel = false
        panelList.clear()
        panelConfig?.Panel?.standard?.properties?.forEach {
            val panel = ControlPanel()
            panel.id = it.id
            panel.big = it.isBig()
            panel.icon = it.ui.icon
            panel.type = it.ui.type
            panelList.add(panel)
        }
        hasProduct = false
        product?.myTemplate?.properties?.run {
            forEach {
                this@DeviceService.contains(it.id)?.run {
                    it.parseDefine()
                    this.name = it.name
                    this.desc = it.desc
                    this.define = it.productDefine
                    this.valueType = it.getType()
                    this.required = it.required
                    this.mode = it.mode
                }
            }
        }
        callback.success(panelList)
        deviceData(product!!.ProductId, deviceName,
            object : MyCallback {
                override fun fail(msg: String?, reqCode: Int) {
                    callback.fail(msg ?: "")
                }

                override fun success(response: BaseResponse, reqCode: Int) {
                    response.parse(DeviceDataResponse::class.java)?.parseList()?.forEach {
                        contains(it.id)?.run {
                            this.value = it.value
                            this.LastUpdate = it.lastUpdate
                        }
                    }
                    callback.refresh()
                }
            })
        deviceName = ""
    }

    /**
     * 面板中是否包含字段
     */
    private fun contains(id: String): ControlPanel? {
        panelList.forEach {
            if (it.id == id) {
                return it
            }
        }
        return null
    }

}