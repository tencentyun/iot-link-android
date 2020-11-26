package com.tencent.iot.explorer.link.mvp.model

import android.os.Handler
import android.text.TextUtils
import com.alibaba.fastjson.JSON
import com.tencent.iot.explorer.link.App
import com.tencent.iot.explorer.link.core.auth.IoTAuth
import com.tencent.iot.explorer.link.core.auth.callback.MyCallback
import com.tencent.iot.explorer.link.core.auth.entity.DeviceDataEntity
import com.tencent.iot.explorer.link.core.auth.entity.NavBar
import com.tencent.iot.explorer.link.core.auth.entity.ProductProperty
import com.tencent.iot.explorer.link.core.auth.entity.Property
import com.tencent.iot.explorer.link.core.auth.message.MessageConst.DEVICE_CHANGE
import com.tencent.iot.explorer.link.core.auth.message.payload.Payload
import com.tencent.iot.explorer.link.core.auth.message.upload.ArrayString
import com.tencent.iot.explorer.link.core.auth.response.BaseResponse
import com.tencent.iot.explorer.link.core.auth.response.ControlPanelResponse
import com.tencent.iot.explorer.link.core.auth.response.DeviceDataResponse
import com.tencent.iot.explorer.link.core.auth.response.DeviceProductResponse
import com.tencent.iot.explorer.link.core.auth.socket.callback.ActivePushCallback
import com.tencent.iot.explorer.link.core.auth.util.JsonManager
import com.tencent.iot.explorer.link.core.link.entity.TRTCParamsEntity
import com.tencent.iot.explorer.link.core.log.L
import com.tencent.iot.explorer.link.kitlink.consts.CommonField
import com.tencent.iot.explorer.link.kitlink.entity.DevicePropertyEntity
import com.tencent.iot.explorer.link.kitlink.response.UserSettingResponse
import com.tencent.iot.explorer.link.kitlink.util.HttpRequest
import com.tencent.iot.explorer.link.kitlink.util.RequestCode
import com.tencent.iot.explorer.link.mvp.ParentModel
import com.tencent.iot.explorer.link.mvp.view.ControlPanelView
import com.tencent.iot.explorer.trtc.model.RoomKey
import com.tencent.iot.explorer.trtc.model.TRTCCalling
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList


/**
 * 控制面板业务
 */
class ControlPanelModel(view: ControlPanelView) : ParentModel<ControlPanelView>(view), MyCallback,
    ActivePushCallback {

    init {
        IoTAuth.addActivePushCallback(this)
    }

    var productId = ""
    var deviceName = ""
    var deviceId = ""

    private var hasPanel = false
    private var hasProduct = false

    //面板UI列表
    private val uiList = ArrayList<Property>()

    //产品信息
    private val propertyList = ArrayList<ProductProperty>()

    //设备当前信息
    private val deviceDataList = arrayListOf<DeviceDataEntity>()

    //设备产品信息及面板数据
    val devicePropertyList = LinkedList<DevicePropertyEntity>()

    //是否显示导航栏
    private var navBar: NavBar? = null

    //是否显示云端定时
    private var hasTimerCloud = false

    /**
     * 断网后重新连上服务器
     */
    override fun reconnected() {
        registerActivePush()
    }

    /**
     * 监听设备返回
     */
    override fun success(payload: Payload) {
        L.e("Payoad", payload.data)
        payload.keySet()?.forEachIndexed { _, id ->
            run set@{
                devicePropertyList.forEach {
                    if (id == it.id) {
                        it.setValue(payload.getValue(id))
                        view?.showControlPanel(navBar, hasTimerCloud)
                        var jsonObject = JSONObject(payload.json)
                        val action = jsonObject.getString(CommonField.MODULE_ACTION);
                        if (action == DEVICE_CHANGE) { //收到了设备属性改变的wss消息
                            var paramsObject = jsonObject.getJSONObject(CommonField.PARAM) as JSONObject
                            val subType = paramsObject.getString(CommonField.SUB_TYPE)
                            if (subType == CommonField.REPORT) { //收到了设备端属性状态改变的wss消息

                                var payloadParamsObject = JSONObject(payload.payload)
                                val payloadParamsJson = payloadParamsObject.getJSONObject(CommonField.PARAM)
                                var videoCallStatus = -1
                                if (payloadParamsJson.has(CommonField.TRTC_VIDEO_CALL_STATUS)) {
                                    videoCallStatus = payloadParamsJson.getInt(CommonField.TRTC_VIDEO_CALL_STATUS)
                                }
                                var audioCallStatus = -1
                                if (payloadParamsJson.has(CommonField.TRTC_AUDIO_CALL_STATUS)) {
                                    audioCallStatus = payloadParamsJson.getInt(CommonField.TRTC_AUDIO_CALL_STATUS)
                                }

                                // 判断payload中是否包含设备的video_call_status, audio_call_status字段以及是否等于1，若等于1，就调用CallDevice接口, 主动拨打
                                if (videoCallStatus == 1) {
                                    trtcCallDevice(TRTCCalling.TYPE_VIDEO_CALL)
                                } else if (audioCallStatus == 1) {
                                    trtcCallDevice(TRTCCalling.TYPE_AUDIO_CALL)
                                }
                            }
                        }
                        return@set
                    }
                }
            }
        }
    }

    private fun trtcCallDevice(callingType: Int) {
        HttpRequest.instance.trtcCallDevice("$productId/$deviceName", object: MyCallback {
            override fun fail(msg: String?, reqCode: Int) {
                if (msg != null) L.e(msg)
            }

            override fun success(response: BaseResponse, reqCode: Int) {
                // 解析房间参数，并呼叫页面
                val json = response.data as com.alibaba.fastjson.JSONObject
                if (json == null || !json.containsKey(CommonField.TRTC_PARAMS)) return;
                val data = json.getString(CommonField.TRTC_PARAMS)
                if (TextUtils.isEmpty(data)) return;
                val params = JSON.parseObject(data, TRTCParamsEntity::class.java)

                var room = RoomKey()
                room.userId = params.UserId
                room.appId = params.SdkAppId
                room.userSig = params.UserSig
                room.roomId = params.StrRoomId
                room.callType = callingType
                view?.enterRoom(room)
            }
        })
    }

    /**
     * 监听设备返回
     */
    override fun unknown(json: String, errorMessage: String) {
        L.e("unknown-json", json)
        L.e("unknown-errorMessage", errorMessage)
    }

    /**
     * 设备当前状态(如亮度、开关状态等)
     */
    fun requestDeviceData() {
        if (hasPanel) {
            deviceDataList.clear()
            HttpRequest.instance.deviceData(productId, deviceName, this)
        }
    }

    /**
     * 当前产品控制面板风格主题
     */
    fun requestControlPanel() {
        uiList.clear()
        propertyList.clear()
        devicePropertyList.clear()
        hasPanel = false
        hasProduct = false
        //面板
        HttpRequest.instance.controlPanel(arrayListOf(productId), this)
        //产品信息：功能名称
        HttpRequest.instance.deviceProducts(arrayListOf(productId), this)
    }

    /**
     * 注册设备监听
     */
    fun registerActivePush() {
        IoTAuth.registerActivePush(ArrayString(deviceId), null)
    }

    /**
     * 用户控制设备(上报数据)
     */
    fun controlDevice(id: String, value: String) {
        L.d("上报数据:id=$id value=$value")
        val data = if (isCovertInt(value)) {
            "{\"$id\":$value}"
        } else {
            "{\"$id\":\"$value\"}"
        }
        HttpRequest.instance.controlDevice(productId, deviceName, data, this)
    }

    /**
     * 获取温度单位等用户设置
     */
    fun getUserSetting() {
        HttpRequest.instance.getUserSetting(this)
    }

    /**
     * 转换数值
     */
    private fun isCovertInt(value: String): Boolean {
        try {
            value.toInt()
            return true
        } catch (e: Exception) {
        }
        return false
    }

    override fun fail(msg: String?, reqCode: Int) {
        L.e(msg ?: "")
    }

    override fun success(response: BaseResponse, reqCode: Int) {
        when (reqCode) {
            RequestCode.device_data -> {
                parseDeviceData(response)
            }
            RequestCode.control_panel -> {
                parsePanel(response)
            }
            RequestCode.device_product -> {
                parseProduct(response)
            }
            RequestCode.control_device -> {
                parseControlDevice(response)
            }
            RequestCode.user_setting -> {
                response.parse(UserSettingResponse::class.java)?.UserSetting?.run {
                    App.data.userSetting = this
                }
            }
        }
    }

    /**
     * 获得对应id的名称
     */
    fun getDevicePropertyForId(id: String): DevicePropertyEntity? {
        devicePropertyList.forEach {
            if (it.id == id) {
                return it
            }
        }
        return null
    }

    /**
     * 解析设备当前数据
     */
    private fun parseDeviceData(response: BaseResponse) {
        if (!response.isSuccess()) return
        response.parse(DeviceDataResponse::class.java)?.run {
            deviceDataList.clear()
            deviceDataList.addAll(parseList())
            L.e("deviceDataList", JsonManager.toJson(deviceDataList))
            deviceDataList.forEach {
                run checked@{
                    devicePropertyList.forEachIndexed { _, devicePropertyEntity ->
                        if (it.id == devicePropertyEntity.id) {
                            devicePropertyEntity.setValue(it.value)
                            devicePropertyEntity.LastUpdate = it.lastUpdate
                            return@checked
                        }
                    }
                }
            }
            view?.showControlPanel(navBar, hasTimerCloud)
        }
    }

    /**
     * 解析面板数据
     */
    private fun parsePanel(response: BaseResponse) {
        response.parse(ControlPanelResponse::class.java)?.Data?.let {
            if (it.isNotEmpty()) {
                it[0].parse().run {
                    navBar = getNavBar()
                    hasTimerCloud = isTimingProject()
                    uiList.addAll(getUIList())
                    L.e("uiList = ${JsonManager.toJson(uiList)}")
                    if (uiList.isNotEmpty()) {
                        hasPanel = true
                        //请求数据
                        requestDeviceData()
                        //合并
                        mergeData()
                    }
                }
            }
        }
    }

    /**
     * 解析产品信息
     */
    private fun parseProduct(response: BaseResponse) {
        if (!response.isSuccess()) return
        response.parse(DeviceProductResponse::class.java)?.run {
            if (Products.isNotEmpty()) {
                Products[0].parseTemplate()?.properties?.run {
                    propertyList.addAll(this)
                    L.e("propertyList = ${JsonManager.toJson(propertyList)}")
                    hasProduct = true
                    mergeData()
                }
            }
        }
    }

    /**
     * 合并面板及产品数据
     */
    @Synchronized
    private fun mergeData() {
        if (!hasPanel || !hasProduct) return
        uiList.forEachIndexed { _, property ->
            val devicePropertyEntity = DevicePropertyEntity()
            devicePropertyEntity.id = property.id
            devicePropertyEntity.type = property.ui.type
            devicePropertyEntity.icon = property.ui.icon
            devicePropertyEntity.big = property.big
            //完善devicePropertyEntity
            completeProperty(devicePropertyEntity)
            //数据不全不显示
            if (!TextUtils.isEmpty(devicePropertyEntity.name))
                if (property.isBig()) {
                    //大按钮在第一个位置
                    devicePropertyList.addFirst(devicePropertyEntity)
                } else {
                    devicePropertyList.add(devicePropertyEntity)
                }
        }
        L.e("devicePropertyList", JsonManager.toJson(devicePropertyList) ?: "")
        view?.showControlPanel(navBar, hasTimerCloud)
    }

    private fun completeProperty(entity: DevicePropertyEntity) {
        propertyList.forEach {
            if (it.id == entity.id) {
                entity.name = it.name
                entity.desc = it.desc
                entity.mode = it.mode
                entity.required = it.required
                entity.valueType = it.getType()
                when {
                    it.isNumberType() -> entity.numberEntity =
                            it.getNumberEntity()
                    it.isStringType() -> entity.stringEntity =
                            it.getStringEntity()
                    it.isEnumType() -> entity.enumEntity = it.getEnumEntity()
                    it.isBoolType() -> entity.boolEntity = it.getBoolEntity()
                    it.isTimestampType() -> entity.timestamp = true
                }
                return
            }
        }
    }

    /**
     * 控制设备返回处理
     */
    private fun parseControlDevice(response: BaseResponse) {
        if (response.isSuccess()) {
//            waitUpdate()
        } else {
            L.e(response.msg)
        }
    }

    private val handler = Handler()
    private var waitUpdate = false
    private val runnable = Runnable {
        if (waitUpdate) {
            requestDeviceData()
        }
    }

    private fun waitUpdate() {
        waitUpdate = true
        handler.postDelayed(runnable, 1000)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(runnable)
        IoTAuth.removeActivePushCallback(ArrayString(deviceId), this)
    }

}