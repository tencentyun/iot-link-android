package com.tencent.iot.explorer.link.mvp.model

import android.text.TextUtils
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.tencent.iot.explorer.link.App
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.core.log.L
import com.tencent.iot.explorer.link.kitlink.entity.ShareDeviceEntity
import com.tencent.iot.explorer.link.kitlink.entity.WeatherEntity
import com.tencent.iot.explorer.link.kitlink.util.HttpRequest
import com.tencent.iot.explorer.link.core.auth.callback.MyCallback
import com.tencent.iot.explorer.link.kitlink.util.RequestCode
import com.tencent.iot.explorer.link.mvp.ParentModel
import com.tencent.iot.explorer.link.mvp.view.HomeFragmentView
import com.tencent.iot.explorer.link.T
import com.tencent.iot.explorer.link.core.auth.IoTAuth
import com.tencent.iot.explorer.link.core.auth.entity.DeviceEntity
import com.tencent.iot.explorer.link.core.auth.entity.RoomEntity
import com.tencent.iot.explorer.link.core.auth.message.upload.ArrayString
import com.tencent.iot.explorer.link.core.auth.response.*
import com.tencent.iot.explorer.link.kitlink.consts.CommonField.DATA
import com.tencent.iot.explorer.link.kitlink.response.ShareDeviceListResponse

class HomeFragmentModel(view: HomeFragmentView) : ParentModel<HomeFragmentView>(view), MyCallback {


    private var shareDeviceTotal = 0
    private var deviceTotal = 0
    private var familyTotal = 0
    private var roomTotal = 0

    private val familyList = App.data.familyList
    private val roomList = App.data.roomList
    private val deviceList = App.data.deviceList
    private val shareDeviceList = App.data.shareDeviceList

    var roomId = ""
    var deviceListEnd = false
    var shareDeviceListEnd = false
    private var familyListEnd = false
    private var roomListEnd = false

    private var isTabFamily = false

    val weatherEntity = WeatherEntity()

    fun getDeviceEntity(position: Int): DeviceEntity {
        return deviceList[position]
    }

    /**
     * 切换家庭
     */
    fun tabFamily(position: Int) {
        App.data.setCurrentFamily(position)
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
        familyListEnd = false
        roomListEnd = false
        deviceListEnd = false
        shareDeviceListEnd = false
        familyList.clear()
        loadFamilyList()
    }

    /**
     * 请求获取家庭列表
     */
    private fun loadFamilyList() {
        if (familyListEnd) return
        HttpRequest.instance.familyList(familyList.size, this)
    }

    /**
     * 请求获取当前家庭房间列表
     */
    fun refreshRoomList() {
        isTabFamily = true
        roomListEnd = false
        deviceListEnd = false
        shareDeviceListEnd = false
        roomList.clear()
        loadRoomList()
    }

    /**
     * 请求获取当前家庭房间列表
     */
    private fun loadRoomList() {
        if (roomListEnd) return
        HttpRequest.instance.roomList(App.data.getCurrentFamily().FamilyId, 0, this)
    }

    /**
     * 请求获取设备列表
     */
    fun refreshDeviceList() {
        deviceListEnd = false
        shareDeviceListEnd = false
        deviceList.clear()
        loadDeviceList()
    }

    /**
     * 请求获取设备列表
     */
    fun loadDeviceList() {
        if (TextUtils.isEmpty(App.data.getCurrentFamily().FamilyId)) return
        if (deviceListEnd) return
        HttpRequest.instance.deviceList(
            App.data.getCurrentFamily().FamilyId,
            roomId,
            deviceList.size,
            this
        )
    }

    /**
     * 获取设备在线状态
     */
    private fun getDeviceOnlineStatus(index: Int) {
        var productId = ""
        val deviceIds = arrayListOf<String>()
        for (i in index until deviceList.size) {
            App.data.deviceList[i].let {
                if (TextUtils.isEmpty(productId)) {
                    productId = it.ProductId
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
                                for (i in index until deviceList.size) {
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
                                view?.showDeviceOnline()
                            }
                        }
                    }
                }
            })
        }
    }

    /**
     * 获取共享的设备
     */
    private fun refreshShareDeviceList() {
        shareDeviceList.clear()
        loadShareDeviceList()
    }

    /**
     * 获取共享的设备
     */
    fun loadShareDeviceList() {
        if (shareDeviceListEnd) return
        if (roomId != "") return
        HttpRequest.instance.shareDeviceList(shareDeviceList.size, this)
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
                        if (Total >= 0) {
                            familyTotal = Total
                        }
                        familyListEnd = familyList.size == familyTotal
                        if (familyList.isNotEmpty()) {
                            App.data.getCurrentFamily()
                            view?.showFamily()
                            refreshRoomList()
                            if (!familyListEnd) {
                                loadFamilyList()
                            }
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
                        if (isTabFamily) {
                            isTabFamily = false
                            roomList.clear()
                            roomList.add(RoomEntity())
                            //刷新房间列表后，设备列表显示全部设备
                            roomId = ""
                            refreshDeviceList()
                        }
                        if (Roomlist != null) {
                            roomList.addAll(Roomlist!!)
                            if (roomList.isNoSelect()) {
                                roomList.addSelect(0)
                            }
                        }
                        if (Total >= 0) {
                            roomTotal = Total
                        }
                        roomListEnd = roomList.size >= roomTotal
                        L.e("roomList=${JSON.toJSONString(roomList)}")
                        view?.showRoomList()
                        //还有数据
                        if (!roomListEnd) {
                            loadRoomList()
                        }
                    }
                }
            }
            RequestCode.device_list -> {
                if (response.isSuccess()) {
                    response.parse(DeviceListResponse::class.java)?.run {
                        deviceList.addAll(DeviceList)
                        if (Total >= 0) {
                            deviceTotal = Total
                            deviceListEnd = deviceList.size >= Total
                        }
                        view?.showDeviceList(
                            deviceList.size,
                            roomId,
                            deviceListEnd,
                            shareDeviceListEnd
                        )
                        if (deviceListEnd && roomId == "") {
                            //到底时开始加载共享的设备列表,并且是在全部设备这个房间时
                            refreshShareDeviceList()
                        }
                        //在线状态
                        getDeviceOnlineStatus(deviceList.size - DeviceList.size)

                        val productIdList = ArrayList<String>()
                        // TRTC: 轮询在线的trtc设备的call_status
                        for (device in deviceList) {
                            productIdList.add(device.ProductId)
                        }
                        getProductsConfig(productIdList, deviceList)
                    }
                }
            }
            RequestCode.share_device_list -> {
                if (response.isSuccess()) {
                    response.parse(ShareDeviceListResponse::class.java)?.run {
                        if (Total >= 0) {
                            shareDeviceTotal = Total
                            shareDeviceListEnd = shareDeviceList.size >= Total
                        }
                        if (shareDeviceList.isEmpty() && !ShareDevices.isNullOrEmpty()) {
                            val title = ShareDeviceEntity()
                            title.DeviceId = "title"
                            shareDeviceList.add(title)
                            deviceList.add(title)
                        }
                        shareDeviceList.addAll(ShareDevices)
                        deviceList.addAll(ShareDevices)
                        view?.showDeviceList(
                            deviceList.size,
                            roomId,
                            deviceListEnd,
                            shareDeviceListEnd
                        )
                        //在线状态
                        getDeviceOnlineStatus(deviceList.size - ShareDevices.size)
                    }
                }
            }
        }
    }

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

    private fun getDeviceCallStatus(device: DeviceEntity) {
        HttpRequest.instance.deviceData(device.ProductId, device.DeviceName, object: MyCallback {
            override fun fail(msg: String?, reqCode: Int) {
                if (msg != null) L.e(msg)
            }
            override fun success(response: BaseResponse, reqCode: Int) {
                if (response.code == 0) { //获取 设备当前状态(如亮度、开关状态等) 成功
                    // 解析设备状态
                    val json = response.data as JSONObject
                    val dataJson = json.getJSONObject(DATA)
                    if (dataJson == null || dataJson.isEmpty()) {
                        return
                    }
                    val videoCallStatusJson = dataJson.getJSONObject("video_call_status")
                    val videoCallStatus = videoCallStatusJson.getInteger("Value")

                    val audioCallStatusJson = dataJson.getJSONObject("audio_call_status")
                    val audioCallStatus = audioCallStatusJson.getInteger("Value")
                    // 判断设备的video_call_status, audio_call_status字段是否等于1，若等于1，就调用CallDevice接口
                    if (videoCallStatus == 1 || audioCallStatus == 1) {
                        trtcCallDevice(device)
                    }
                }

            }
        })
    }

    private fun trtcCallDevice(device: DeviceEntity) {
        HttpRequest.instance.trtcCallDevice(device.DeviceId, object: MyCallback {
            override fun fail(msg: String?, reqCode: Int) {
                if (msg != null) L.e(msg)
            }

            override fun success(response: BaseResponse, reqCode: Int) {
                // 解析房间参数，并拉起被呼叫页面
                L.d(response.toString())
            }
        })
    }
}