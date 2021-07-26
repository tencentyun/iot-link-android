package com.tencent.iot.explorer.link.mvp.model

import android.text.TextUtils
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.tencent.iot.explorer.link.App
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.core.log.L
import com.tencent.iot.explorer.link.kitlink.util.HttpRequest
import com.tencent.iot.explorer.link.core.auth.callback.MyCallback
import com.tencent.iot.explorer.link.kitlink.util.RequestCode
import com.tencent.iot.explorer.link.mvp.ParentModel
import com.tencent.iot.explorer.link.mvp.view.HomeFragmentView
import com.tencent.iot.explorer.link.T
import com.tencent.iot.explorer.link.core.auth.IoTAuth
import com.tencent.iot.explorer.link.core.auth.entity.DeviceEntity
import com.tencent.iot.explorer.link.core.auth.entity.ProductUIDevShortCutConfig
import com.tencent.iot.explorer.link.core.auth.entity.RoomEntity
import com.tencent.iot.explorer.link.core.auth.message.MessageConst
import com.tencent.iot.explorer.link.core.auth.message.upload.ArrayString
import com.tencent.iot.explorer.link.core.auth.response.*
import com.tencent.iot.explorer.link.core.link.entity.TRTCParamsEntity
import com.tencent.iot.explorer.link.kitlink.consts.CommonField.DATA
import com.tencent.iot.explorer.link.kitlink.entity.DataTemplate
import com.tencent.iot.explorer.link.core.auth.entity.DevModeInfo
import com.tencent.iot.explorer.link.core.auth.util.JsonManager
import com.tencent.iot.explorer.link.kitlink.entity.ProdConfigDetailEntity
import com.tencent.iot.explorer.link.kitlink.entity.ProductEntity
import com.tencent.iot.explorer.link.kitlink.entity.ProductsEntity
import com.tencent.iot.explorer.link.kitlink.response.ProductsConfigResponse
import com.tencent.iot.explorer.link.kitlink.response.ShareDeviceListResponse
import com.tencent.iot.explorer.link.rtc.model.RoomKey
import com.tencent.iot.explorer.link.rtc.model.TRTCCalling
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

class HomeFragmentModel(view: HomeFragmentView) : ParentModel<HomeFragmentView>(view), MyCallback {

    private val familyList = App.data.familyList
    private val roomList = App.data.roomList
    val deviceList = App.data.deviceList
    val shareDeviceList = App.data.shareDeviceList
    val shortCuts: MutableMap<String, ProductUIDevShortCutConfig> = ConcurrentHashMap()

    var roomId = ""

    fun getDeviceEntity(position: Int): DeviceEntity {
        return deviceList[position]
    }

    /**
     * 切换家庭
     */
    fun tabFamily(position: Int) {
        App.data.setCurrentFamily(position)
        roomId = ""
        refreshRoomList()
    }

    /**
     * 切换房间
     */
    fun tabRoom(position: Int) {
        roomId = roomList[position].RoomId
        App.data.setCurrentRoom(position)
        refreshDeviceList()
    }

    /**
     * 请求获取家庭列表
     */
    fun refreshFamilyList() {
        familyList.clear()
        HttpRequest.instance.familyList(0, this)
    }

    fun loadDevData(device: DeviceEntity) {
        if (device == null) return
        HttpRequest.instance.deviceData(device.ProductId, device.DeviceName, object: MyCallback {
            override fun fail(msg: String?, reqCode: Int) {
                T.show(msg?:"")
            }

            override fun success(response: BaseResponse, reqCode: Int) {
                if (!response.isSuccess()) return
                response.parse(DeviceDataResponse::class.java)?.run {
                    if (device.deviceDataList == null) {
                        device.deviceDataList = CopyOnWriteArrayList()
                    }
                    device.deviceDataList.clear()
                    device.deviceDataList.addAll(parseList())
                }
                var productIdList = ArrayList<String>()
                productIdList.add(device.ProductId)
                getShortCutByProductsConfig(productIdList)
            }
        })
    }

    private fun loadDataMode(productIds: ArrayList<String>) {
        HttpRequest.instance.deviceProducts(productIds, object: MyCallback {
            override fun fail(msg: String?, reqCode: Int) {}

            override fun success(response: BaseResponse, reqCode: Int) {
                if (response.isSuccess()) {

                    var dataTemplate: DataTemplate? = null
                    if (!TextUtils.isEmpty(response.data.toString())) {
                        var products = JSON.parseObject(response.data.toString(), ProductsEntity::class.java)
                        if (products == null || products.Products == null) return

                        for (i in 0 until products!!.Products!!.size) {
                            var productEntity = JSON.parseObject(products!!.Products!!.getString(i), ProductEntity::class.java)

                            if (productEntity.DataTemplate != null) {
                                dataTemplate = JSON.parseObject(productEntity.DataTemplate.toString(), DataTemplate::class.java)
                            }

                            if (!TextUtils.isEmpty(productEntity.Name) && !TextUtils.isEmpty(productEntity.ProductId)) {
                                for (devEls in deviceList) {
                                    if (devEls.ProductId == productEntity.ProductId) {
                                        devEls.AliasName = productEntity.Name
                                    }
                                }

                                for (devEls in shareDeviceList) {
                                    if (devEls.ProductId == productEntity.ProductId) {
                                        devEls.AliasName = productEntity.Name
                                    }
                                }
                            }
                        }
                    }

                    if (dataTemplate == null || dataTemplate.properties == null || dataTemplate.properties!!.size == 0) {
                        return
                    }

                    var devModes = ArrayList<DevModeInfo>()
                    for (i in 0 until dataTemplate.properties!!.size) {
                        var devModeInfo = JSON.parseObject(dataTemplate.properties!!.get(i).toString(), DevModeInfo::class.java)
                        devModes.add(devModeInfo)
                    }
                    // 实际只有一条数据
                    for (productId in productIds) {
                        if (shortCuts.get(productId) != null) {
                            shortCuts.get(productId)?.devModeInfos = devModes
                        }
                    }
                    view?.showDeviceShortCut(shortCuts)
                }
            }
        })
    }

    /**
     * 请求获取当前家庭房间列表
     */
    fun refreshRoomList() {
        roomList.clear()
        HttpRequest.instance.roomList(App.data.getCurrentFamily().FamilyId, 0, this)
    }

    /**
     * 请求获取设备列表
     */
    fun refreshDeviceList() {
//        deviceList.clear()
//        shareDeviceList.clear()
//        view?.showDeviceList(0, roomId, false, true)
//        view?.showDeviceList(0, roomId, true, false)
        loadDeviceList()
    }

    /**
     * 请求获取设备列表
     */
    fun loadDeviceList() {
        if (TextUtils.isEmpty(App.data.getCurrentFamily().FamilyId)) return
        HttpRequest.instance.deviceList(App.data.getCurrentFamily().FamilyId, roomId, 0, this)
    }

    /**
     * 获取设备在线状态
     */
    private fun getDeviceOnlineStatus(deviceList: ArrayList<DeviceEntity>) {
        var productId = ""
        var productIds = arrayListOf<String>()
        val deviceIds = arrayListOf<String>()
        for (i in 0 until deviceList.size) {
            deviceList[i].let {
                if (TextUtils.isEmpty(productId)) {
                    productId = it.ProductId
                }
                if (!productIds.contains(it.ProductId)) {
                    productIds.add(it.ProductId)
                }
                deviceIds.add(it.DeviceId)
            }
        }
        if (deviceIds.isNotEmpty() && !TextUtils.isEmpty(productId)) {
            HttpRequest.instance.deviceOnlineStatus(productId, deviceIds, object : MyCallback {
                override fun fail(msg: String?, reqCode: Int) {
                    L.e(msg ?: "")
                }

                override fun success(response: BaseResponse, reqCode: Int) {
                    if (response.isSuccess()) {
                        response.parse(DeviceOnlineResponse::class.java)?.run {
                            if (!DeviceStatuses.isNullOrEmpty()) {
                                for (i in 0 until deviceList.size) {
                                    deviceList[i].run {
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
                                excludeBleProduct(productIds)
                                view?.showDeviceOnline()
                            }
                        }
                    }
                }
            })
        }
    }

    // 获取设备产品配置的快捷入口
    private fun excludeBleProduct(productIds: ArrayList<String>) {
        HttpRequest.instance.getProductsConfig(productIds, object: MyCallback {
            override fun fail(msg: String?, reqCode: Int) {
                if (msg != null) L.e(msg)
            }

            override fun success(response: BaseResponse, reqCode: Int) {
                if (response.isSuccess()) {
                    response.parse(ProductsConfigResponse::class.java)?.run {

                        for (data in Data) {
                            var config = JsonManager.parseJson(data.Config, ProdConfigDetailEntity::class.java)
                            if (config != null && config.bleConfig != null && !TextUtils.isEmpty(config.bleConfig!!.protocolType)) {
                                for (dev in deviceList) {
                                    if (dev.ProductId == data.ProductId) {
                                        dev.online = 1
                                    }
                                }

                                for (dev in shareDeviceList) {
                                    if (dev.ProductId == data.ProductId) {
                                        dev.online = 1
                                    }
                                }

                            }
                        }

                        view?.showDeviceOnline()
                    }
                }
            }
        })
    }

    /**
     * 获取共享的设备
     */
    private fun refreshShareDeviceList() {
        loadShareDeviceList()
    }

    /**
     * 获取共享的设备
     */
    fun loadShareDeviceList() {
        if (roomId != "") return
        HttpRequest.instance.shareDeviceList(0, this)
    }

    override fun fail(msg: String?, reqCode: Int) {
        L.e(msg ?: "")
    }

    override fun success(response: BaseResponse, reqCode: Int) {
        when (reqCode) {
            RequestCode.family_list -> {
                if (response.isSuccess()) {
                    response.parse(FamilyListResponse::class.java)?.run {

                        familyList.addAll(FamilyList)
                        if (familyList.isNotEmpty()) {
                            App.data.getCurrentFamily()
                            view?.showFamily()
                            refreshRoomList()
                        } else {//没有家庭，创建家庭
                            HttpRequest.instance.createFamily(
                                T.getContext().getString(R.string.somebody_family, App.data.userInfo.NickName),//"${App.data.userInfo.NickName}的家",
                                "",
                                this@HomeFragmentModel
                            )
                        }
                    }
                }
            }
            RequestCode.create_family -> {
                if (response.isSuccess())
                    refreshFamilyList()
            }
            RequestCode.room_list -> {
                if (response.isSuccess()) {
                    response.parse(RoomListResponse::class.java)?.run {
                        roomList.clear()
                        roomList.add(RoomEntity())
                        //刷新房间列表后，设备列表显示全部设备
                        roomId = ""
                        refreshDeviceList()

                        Roomlist?.let {
                            roomList.addAll(it)
                            if (roomList.isNoSelect()) {
                                roomList.addSelect(0)
                            }
                        }

                        L.e("roomList=${JSON.toJSONString(roomList)}")
                        view?.showRoomList()
                    }
                }
            }
            RequestCode.device_list -> {
                if (response.isSuccess()) {
                    response.parse(DeviceListResponse::class.java)?.run {
                        deviceList.clear()
                        deviceList.addAll(DeviceList)
                        view?.showDeviceList(deviceList.size, roomId, true, false)
                        //到底时开始加载共享的设备列表,并且是在全部设备这个房间时
                        if (roomId == "") {
                            refreshShareDeviceList()
                        } else {
                            shareDeviceList.clear()
                            view?.showDeviceList(shareDeviceList.size, roomId, false, true)
                        }
                        //在线状态
                        getDeviceOnlineStatus(deviceList)

                        val productIdList = ArrayList<String>()
                        for (device in deviceList) {
                            productIdList.add(device.ProductId)
                            loadDevData(device)
                        }
                        getProductsConfig(productIdList, deviceList)

                        val deviceIdList = ArrayString()
                        for (device in deviceList) {
                            deviceIdList.addValue(device.DeviceId)
                        }
                        // TRTC: trtc设备注册websocket监听
                        IoTAuth.registerActivePush(deviceIdList, null)
                        App.data.rtcDeviceIdList = deviceIdList
                    }
                }
            }
            RequestCode.share_device_list -> {
                if (response.isSuccess()) {
                    response.parse(ShareDeviceListResponse::class.java)?.run {
                        shareDeviceList.clear()
                        shareDeviceList.addAll(ShareDevices)

                        val deviceIdList = ArrayString()
                        for (device in shareDeviceList) {
                            deviceIdList.addValue(device.DeviceId)
                        }

                        val productIdList = ArrayList<String>()
                        for (device in shareDeviceList) {
                            productIdList.add(device.ProductId)
                            loadDevData(device)
                        }
                        // TRTC: trtc设备注册websocket监听
                        IoTAuth.registerActivePush(deviceIdList, null)
                        App.data.rtcDeviceIdList = deviceIdList

                        view?.showDeviceList(shareDeviceList.size, roomId, false, true)
                        //在线状态
                        getDeviceOnlineStatus(shareDeviceList)
                    }
                }
            }
        }
    }

    // 获取设备产品配置的快捷入口
    private fun getShortCutByProductsConfig(productIds: ArrayList<String>) {
        HttpRequest.instance.getProductsConfig(productIds, object:MyCallback {
            override fun fail(msg: String?, reqCode: Int) {
                if (msg != null) L.e(msg)
            }

            override fun success(response: BaseResponse, reqCode: Int) {
                if (response.isSuccess()) {
                    response.parse(ControlPanelResponse::class.java)?.Data?.let {
                        it.forEach{
                            it.parse().run {
                                shortCuts.put(this.ProductId, this.configEntity.ShortCut)
                                loadDataMode(productIds)
                            }
                        }
                    }
                }
            }
        })
    }

    /**
     * 获取设备产品配置
     */
    private fun getProductsConfig(productIds: List<String>, deviceList: List<DeviceEntity>) {
        HttpRequest.instance.getProductsConfig(productIds, object:MyCallback {
            override fun fail(msg: String?, reqCode: Int) {
                if (msg != null) L.e(msg)
            }

            override fun success(response: BaseResponse, reqCode: Int) {
                if (response.isSuccess()) {
                    response.parse(ControlPanelResponse::class.java)?.Data?.let {
                        it.forEach{
                            var callingMyApp = false
                            it.parse().run {
                                if (configEntity.Global.trtc) {
                                    val trtcDeviceIdList = ArrayString()
                                    for (device in deviceList) {
                                        if (device.ProductId == ProductId) {
                                            trtcDeviceIdList.addValue(device.DeviceId)
                                            getDeviceCallStatus(device)
                                            callingMyApp = true
                                            break //目前只考虑接收一台设备通话的请求
                                        }
                                    }
                                    // TRTC: trtc设备注册websocket监听
                                    IoTAuth.registerActivePush(trtcDeviceIdList, null)
                                    App.data.rtcDeviceIdList = trtcDeviceIdList
                                }
                            }
                            if (callingMyApp) {
                                return
                            }
                        }
                    }
                }
            }
        })
    }

    /**
     * 获取 设备当前状态(如亮度、开关状态等)
     */
    private fun getDeviceCallStatus(device: DeviceEntity) {
        HttpRequest.instance.deviceData(device.ProductId, device.DeviceName, object: MyCallback {
            override fun fail(msg: String?, reqCode: Int) {
                if (msg != null) L.e(msg)
            }
            override fun success(response: BaseResponse, reqCode: Int) {
                return
                if (response.code == 0) { //获取 设备当前状态(如亮度、开关状态等) 成功
                    // 解析设备状态
                    val json = response.data as JSONObject
                    val dataJson = json.getJSONObject(DATA)
                    if (dataJson == null || dataJson.isEmpty()) {
                        return
                    }
                    val videoCallStatusJson = dataJson.getJSONObject(MessageConst.TRTC_VIDEO_CALL_STATUS)
                    if (videoCallStatusJson == null) return
                    val videoCallStatus = videoCallStatusJson.getInteger("Value")

                    val audioCallStatusJson = dataJson.getJSONObject(MessageConst.TRTC_AUDIO_CALL_STATUS)
                    if (audioCallStatusJson == null) return
                    val audioCallStatus = audioCallStatusJson.getInteger("Value")
                    // 判断设备的video_call_status, audio_call_status字段是否等于1，若等于1，就调用CallDevice接口
                    if (videoCallStatus == 1) {
                        trtcCallDevice(device, TRTCCalling.TYPE_VIDEO_CALL)
                    } else if (audioCallStatus == 1) {
                        trtcCallDevice(device, TRTCCalling.TYPE_AUDIO_CALL)
                    }
                }

            }
        })
    }

    /**
     * 被设备呼叫获取trtc参数信息
     */
    private fun trtcCallDevice(device: DeviceEntity, callingType: Int) {
        HttpRequest.instance.trtcCallDevice(device.DeviceId, object: MyCallback {
            override fun fail(msg: String?, reqCode: Int) {
                if (msg != null) L.e(msg)
            }

            override fun success(response: BaseResponse, reqCode: Int) {
                // 解析房间参数，并拉起被呼叫页面
                val json = response.data as JSONObject
                if (json == null || !json.containsKey(MessageConst.TRTC_PARAMS)) return;
                val data = json.getString(MessageConst.TRTC_PARAMS)
                if (TextUtils.isEmpty(data)) return;
                val params = JSON.parseObject(data, TRTCParamsEntity::class.java)

                var room = RoomKey()
                room.userId = params.UserId
                room.appId = params.SdkAppId
                room.userSig = params.UserSig
                room.roomId = params.StrRoomId
                room.callType = callingType
                view?.enterRoom(room, device.DeviceId)
            }
        })
    }

    fun updateDeviceStatus(deviceId: String, status: Int) {
        for (deviceEntity in deviceList) {
            if (deviceEntity.DeviceId == deviceId) {
                deviceEntity.online = status
                return
            }
        }
        for (deviceEntity in shareDeviceList) {
            if (deviceEntity.DeviceId == deviceId) {
                deviceEntity.online = status
                return
            }
        }
    }
}