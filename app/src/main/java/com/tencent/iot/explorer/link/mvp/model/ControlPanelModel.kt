package com.tencent.iot.explorer.link.mvp.model

import android.os.Handler
import android.text.TextUtils
import com.tencent.iot.explorer.link.auth.IoTAuth
import com.tencent.iot.explorer.link.auth.message.payload.Payload
import com.tencent.iot.explorer.link.auth.message.upload.ArrayString
import com.tencent.iot.explorer.link.auth.socket.callback.ActivePushCallback
import com.tencent.iot.explorer.link.kitlink.entity.*
import com.tencent.iot.explorer.link.kitlink.response.BaseResponse
import com.tencent.iot.explorer.link.kitlink.response.ControlPanelResponse
import com.tencent.iot.explorer.link.kitlink.response.DeviceDataResponse
import com.tencent.iot.explorer.link.kitlink.response.DeviceProductResponse
import com.tencent.iot.explorer.link.kitlink.util.HttpRequest
import com.tencent.iot.explorer.link.kitlink.util.JsonManager
import com.tencent.iot.explorer.link.kitlink.util.MyCallback
import com.tencent.iot.explorer.link.kitlink.util.RequestCode
import com.tencent.iot.explorer.link.mvp.ParentModel
import com.tencent.iot.explorer.link.mvp.view.ControlPanelView
import com.tencent.iot.explorer.link.util.L
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
    private val propertyList = ArrayList<PropertyEntity>()

    //设备当前信息
    private val deviceDataList = arrayListOf<DeviceDataEntity>()

    //设备产品信息及面板数据
    val devicePropertyList = LinkedList<DevicePropertyEntity>()

    //主题风格
    var themeTag = ""

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
                        view?.showControlPanel(themeTag, navBar, hasTimerCloud)
                        return@set
                    }
                }
            }
        }
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
                            devicePropertyEntity.LastUpdate = it.LastUpdate
                            return@checked
                        }
                    }
                }
            }
            view?.showControlPanel(themeTag, navBar, hasTimerCloud)
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
                        //主题
                        themeTag = getTheme()
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
        view?.showControlPanel(themeTag, navBar, hasTimerCloud)
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