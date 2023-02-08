package com.tencent.iot.explorer.link.mvp.model

import android.os.Build
import android.os.Handler
import android.text.TextUtils
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONArray
import com.tencent.iot.explorer.link.App
import com.tencent.iot.explorer.link.T
import com.tencent.iot.explorer.link.core.auth.IoTAuth
import com.tencent.iot.explorer.link.core.auth.callback.MyCallback
import com.tencent.iot.explorer.link.core.auth.entity.*
import com.tencent.iot.explorer.link.core.auth.message.MessageConst
import com.tencent.iot.explorer.link.core.auth.message.MessageConst.TRTC_AUDIO_CALL_STATUS
import com.tencent.iot.explorer.link.core.auth.message.MessageConst.TRTC_VIDEO_CALL_STATUS
import com.tencent.iot.explorer.link.core.auth.message.payload.Payload
import com.tencent.iot.explorer.link.core.auth.message.upload.ArrayString
import com.tencent.iot.explorer.link.core.auth.response.BaseResponse
import com.tencent.iot.explorer.link.core.auth.response.ControlPanelResponse
import com.tencent.iot.explorer.link.core.auth.response.DeviceDataResponse
import com.tencent.iot.explorer.link.core.auth.response.DeviceProductResponse
import com.tencent.iot.explorer.link.core.auth.socket.callback.ActivePushCallback
import com.tencent.iot.explorer.link.core.auth.util.JsonManager
import com.tencent.iot.explorer.link.core.link.service.BleConfigService
import com.tencent.iot.explorer.link.core.link.service.LLSyncTLVEntity
import com.tencent.iot.explorer.link.core.log.L
import com.tencent.iot.explorer.link.core.utils.SharePreferenceUtil
import com.tencent.iot.explorer.link.kitlink.consts.CommonField
import com.tencent.iot.explorer.link.kitlink.entity.DevicePropertyEntity
import com.tencent.iot.explorer.link.kitlink.response.UserSettingResponse
import com.tencent.iot.explorer.link.kitlink.util.HttpRequest
import com.tencent.iot.explorer.link.kitlink.util.RequestCode
import com.tencent.iot.explorer.link.kitlink.util.VideoUtils
import com.tencent.iot.explorer.link.mvp.ParentModel
import com.tencent.iot.explorer.link.mvp.view.ControlPanelView
import com.tencent.iot.explorer.link.rtc.model.TRTCUIManager
import com.tencent.iot.video.link.entity.DeviceStatus
import com.tencent.xnet.XP2P
import com.tencent.xnet.XP2PCallback
import java.util.*


/**
 * 控制面板业务
 */
class ControlPanelModel(view: ControlPanelView) : ParentModel<ControlPanelView>(view), MyCallback,
    ActivePushCallback {

    init {
        IoTAuth.addActivePushCallback(this)
        startReconnectCycle()
    }

    var categoryId = 0
    var netType = ""
    var productId = ""
    var deviceName = ""
    var deviceId = ""

    private var hasPanel = false
    private var hasProduct = false
    private var firstTime = true
    private var isP2PConnect = false //p2p是否连接通（包含请求获取设备状态信令成功才算连接通）
    var otherNeedP2PConnect = false //其他类型需要重连p2p
    private var reconnectCycleTask: TimerTask? = null

    //面板UI列表
    private val uiList = ArrayList<Property>()

    //产品信息
    private val propertyList = ArrayList<ProductProperty>()

    //设备当前信息
    private val deviceDataList = arrayListOf<DeviceDataEntity>()

    //设备产品信息及面板数据
    var devicePropertyList = LinkedList<DevicePropertyEntity>()

    //是否显示导航栏
    private var navBar: NavBar? = null

    //是否显示云端定时
    private var hasTimerCloud = false

    private var xp2pInfo = ""

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
            propertyList.forEachIndexed { index, it ->
                var jsonObject = JSON.parseObject(payload.payload)
                val method = jsonObject.getString(MessageConst.METHOD)
                if (method.equals(MessageConst.CONTROL) && netType.equals("ble") && id == it.id) { //ble收到控制指令，向ble设备发送 二进制远程控制指令
                    var byteArr = LLSyncTLVEntity().getControlBleDevicebyteArr(JSON.parseObject(it.define).getString("type"), index, payload.getValue(id), it.define)
                    BleConfigService.get().bluetoothGatt?.let {
                        BleConfigService.get().controlBleDevice(it, byteArr)
                    }
                    return@forEachIndexed
                }
            }
            run set@{
                devicePropertyList.forEach {
                    var jsonObject = JSON.parseObject(payload.payload)
                    val method = jsonObject.getString(MessageConst.METHOD)
                    if (method.equals(MessageConst.CONTROL)) {
                        return@set   //control消息不在控制面板上变化。
                    }
                    if (id == it.id) {
                        it.setValue(payload.getValue(id))
                        view?.showControlPanel(navBar, hasTimerCloud)
                        return@set
                    }
                }
            }
        }
        val jsonObject = org.json.JSONObject(payload.json)
        val action = jsonObject.getString(MessageConst.MODULE_ACTION)
        if (action == MessageConst.DEVICE_CHANGE) { //设备状态发生改变
            val paramsObject = jsonObject.getJSONObject(MessageConst.PARAM) as org.json.JSONObject
            val subType = paramsObject.getString(MessageConst.SUB_TYPE)
            val deviceId = paramsObject.getString(MessageConst.DEVICE_ID)
            if (subType == MessageConst.ONLINE) {
                view?.refreshDeviceStatus(true)
            } else if (subType == MessageConst.OFFLINE) {
                view?.refreshDeviceStatus(false)
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

    fun removeReconnectCycleTasktask() {
        reconnectCycleTask?.cancel()
        reconnectCycleTask = null
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
        var data = if (isCovertInt(value)) {
            "{\"$id\":$value}"
        } else {
            "{\"$id\":\"$value\"}"
        }
        if (id == TRTC_VIDEO_CALL_STATUS || id == TRTC_AUDIO_CALL_STATUS) { //如果点击选择的是trtc设备的呼叫状态
            if (value == "1") { //并且状态值为1，代表应用正在call设备
                TRTCUIManager.getInstance().callingDeviceId = "$productId/$deviceName" //保存下设备id（productId/deviceName）
                val userId = SharePreferenceUtil.getString(App.activity, App.CONFIG, CommonField.USER_ID)
                val agent = "android/1.4.0 (Android" + Build.VERSION.SDK_INT + ";" + App.data.getMobileBrand() + " " + App.data.getMobileModel() + ";" + Locale.getDefault().language + "-" + Locale.getDefault().country + ")"
                data = "{\"$id\":$value, \"${MessageConst.AGENT}\":\"$agent\", \"${MessageConst.TRTC_CALLEDID}\":\"$deviceId\", \"${MessageConst.TRTC_CALLERID}\":\"$userId\"}"

            }
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
            if (categoryId == 567) {
                val deviceDatas = parseList()
                L.e("deviceDatas", JsonManager.toJson(deviceDatas))
                deviceDatas.forEach {
                    if (it.id == "_sys_xp2p_info") {
                        val xp2pInfo = it.value
                        this@ControlPanelModel.xp2pInfo = xp2pInfo
                        L.e("ControlPanel","firstTime: ${firstTime}, isP2PConnect: ${isP2PConnect}")
                        if (firstTime) {
                            XP2P.setCallback(xp2pCallback)
                            XP2P.startService(deviceId, productId, deviceName)
                            XP2P.setParamsForXp2pInfo(deviceId, "", "", xp2pInfo)
                            firstTime = false
                        } else if (!isP2PConnect) {//p2p链路断开 或者 p2p未断开但给设备发送信令失败
                            XP2P.stopService(deviceId)
                            XP2P.setCallback(xp2pCallback)
                            XP2P.startService(deviceId, productId, deviceName)
                            XP2P.setParamsForXp2pInfo(deviceId, "", "", xp2pInfo)
                        } else if (otherNeedP2PConnect) {
                            otherNeedP2PConnect = false
                            XP2P.stopService(deviceId)
                            XP2P.setCallback(xp2pCallback)
                            XP2P.startService(deviceId, productId, deviceName)
                            XP2P.setParamsForXp2pInfo(deviceId, "", "", xp2pInfo)
                        }

                    }
                }
            }
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

    val xp2pCallback = object : XP2PCallback {
        override fun fail(msg: String?, errorCode: Int) {}

        override fun commandRequest(id: String?, msg: String?) {}

        override fun xp2pEventNotify(id: String?, msg: String?, event: Int) {
            //1003 p2p链路断开
            //1004 p2p链路初始化成功
            //1005 p2p链路初始化失败
            when (event) {
                1003 -> {
                    App.activity?.runOnUiThread {
                        VideoUtils.sendVideoBroadcast(App.activity, 2)
                        T.show("p2p链路断开，尝试重连")
                        L.e("=========p2p链路断开，尝试重连")
                        requestXp2pInfo()
                        startReconnectCycle()
                        isP2PConnect = false
                    }
                }
                1004 -> {
                    App.activity?.runOnUiThread {
                        T.show("p2p链路初始化成功")
                        L.e("=========p2p链路初始化成功")
                        getDeviceStatus()
                    }
                }
                1005 -> {
                    App.activity?.runOnUiThread {
                        T.show("p2p链路初始化失败")
                        L.e("=========p2p链路初始化失败")
                        isP2PConnect = false
                    }
                }
            }
        }

        override fun avDataRecvHandle(id: String?, data: ByteArray?, len: Int) {}

        override fun avDataCloseHandle(id: String?, msg: String?, errorCode: Int) {}

        override fun onDeviceMsgArrived(id: String?, data: ByteArray?, len: Int): String {
            return ""
        }
    }

    private fun startReconnectCycle() {
        L.e("=========startReconnectCycle: $isP2PConnect")
        if (reconnectCycleTask == null) {
            L.e("=========reconnectCycleTask: $isP2PConnect")
            var reconnectTimer = Timer("p2p connect cycle")
            reconnectCycleTask = object :TimerTask() {
                override fun run() {
                    L.e("=========isP2PConnect: $isP2PConnect")
                    if (!isP2PConnect) {
                        // 请求最新Xp2pInfo去初始化p2p
                        requestXp2pInfo()
                    } else {
                        removeReconnectCycleTasktask()
                    }
                }
            }
            reconnectTimer.schedule(reconnectCycleTask, 5000, 5000)
        }
    }

    private fun getDeviceStatus(): Boolean {
        val command =
            "action=inner_define&channel=0&cmd=get_device_st&type=live&quality=standard".toByteArray()
        val repStatus = XP2P.postCommandRequestSync(
            deviceId,
            command, command.size.toLong(), (2 * 1000 * 1000).toLong()
        )
        val deviceStatuses = JSONArray.parseArray(
            repStatus,
            DeviceStatus::class.java
        )
        // 0   接收请求
        // 1   拒绝请求
        // 404 error request message
        // 405 connect number too many
        // 406 current command don't support
        // 407 device process error
        if (deviceStatuses != null && deviceStatuses.size > 0) {
            when (deviceStatuses[0].status) {
                0 -> {
                    T.show("设备状态正常")
                    VideoUtils.sendVideoBroadcast(App.activity, 1)
                    isP2PConnect = true
                    return true
                }
                1 -> {
                    T.show("设备状态异常, 拒绝请求: $repStatus")
                    return false
                }
                404 -> {
                    T.show("设备状态异常, error request message: $repStatus")
                    return false
                }
                405 -> {
                    T.show("设备状态异常, connect number too many: $repStatus")
                    return false
                }
                406 -> {
                    T.show("设备状态异常, current command don't support: $repStatus")
                    return false
                }
                407 -> {
                    T.show("设备状态异常, device process error: $repStatus")
                    return false
                }
            }
        } else {
            T.show("获取设备状态失败")
            return false
        }
        return false
    }

    fun getBleDeviceStatus(): ByteArray? {
        var totalSpecsByteArr = ByteArray(0)
        devicePropertyList.forEachIndexed { _, devicePropertyEntity ->
            if (devicePropertyEntity.getValue() != "") {
                var tempSpecs = LLSyncTLVEntity().getControlBleDevicebyteArr(devicePropertyEntity.valueType, devicePropertyEntity.index, devicePropertyEntity.getValue(), "")
                var lastTotalSpecsByteArr = totalSpecsByteArr.copyOf()
                totalSpecsByteArr = ByteArray(lastTotalSpecsByteArr.size +tempSpecs.size)
                System.arraycopy(lastTotalSpecsByteArr, 0, totalSpecsByteArr, 0, lastTotalSpecsByteArr.size)
                System.arraycopy(tempSpecs, 0, totalSpecsByteArr, lastTotalSpecsByteArr.size, tempSpecs.size)
            }
        }
//        var byteArr = ByteArray(4+totalSpecsByteArr.size)
//        byteArr[0] = 0x22.toByte()
//        byteArr[1] = 0x00.toByte()  //Reply_Result  0x00成功  0x01失败  0x02数据解析错误
//        var lengthByteArr = BleConfigService.get().number2Bytes(totalSpecsByteArr.size.toLong(), 2)
//        System.arraycopy(lengthByteArr, 0, byteArr, 2, lengthByteArr.size)
//        System.arraycopy(totalSpecsByteArr, 0, byteArr, 2+lengthByteArr.size, totalSpecsByteArr.size)
        return totalSpecsByteArr
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
                    uiList.clear()
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
                netType = Products[0].NetType
                categoryId = Products[0].CategoryId
                if (categoryId == 567) { //双向音视频类别
                    view?.refreshCategrayId(categoryId)
                    requestXp2pInfo()
                }
                if (uiList.size == 0) {
                    processPropertyList()
                    mergeData()
                }
            }
        }
    }

    private fun requestXp2pInfo() {
        HttpRequest.instance.deviceData(productId, deviceName, this)
    }

    private fun processPropertyList() {
        uiList.clear()
        var firstProperty = true
        propertyList.forEach {
            var defineObject = org.json.JSONObject(it.define)
            if (defineObject.has(CommonField.DEFINE_TYPE)) {
                val type = defineObject.get(CommonField.DEFINE_TYPE)
                if (type != CommonField.DEFINE_TYPE_STRING && type != CommonField.DEFINE_TYPE_TIMESTAMP) { //过滤掉string和timestamp类型，他们不需要ui
                    var property = Property()
                    property.big = firstProperty
                    property.id = it.id
                    property.ui = UI()
                    if (type != CommonField.DEFINE_TYPE_STRUCT) {
                        property.ui.icon = "create"
                        property.ui.type = "btn-col-1"
                    } else {
                        property.ui.icon = "create"
                        property.ui.type = "btn-col-1"
                    }
                    uiList.add(property)

                    firstProperty = false;
                }
            }
        }
        if (uiList.size != 0) {
            hasPanel = true
        }
    }

    /**
     * 合并面板及产品数据
     */
    @Synchronized
    private fun mergeData() {
        if (!hasPanel || !hasProduct) return
        devicePropertyList.clear()
        var tmpList = LinkedList<DevicePropertyEntity>()
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
                    tmpList.addFirst(devicePropertyEntity)
                } else {
                    tmpList.add(devicePropertyEntity)
                }
        }
        tmpList = LinkedList(LinkedHashSet(tmpList))
        devicePropertyList.addAll(tmpList)
        L.e("devicePropertyList", JsonManager.toJson(devicePropertyList) ?: "")
        view?.showControlPanel(navBar, hasTimerCloud)
    }

    private fun completeProperty(entity: DevicePropertyEntity) {
        propertyList.forEachIndexed { index, it ->
            if (it.id == entity.id) {
                entity.index = index
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
        TRTCUIManager.getInstance().callingDeviceId = "" //暂时打电话的入口只在控制面板内，所以销毁了控制面板，就重置一下callingDeviceId为空字符串，代表没有在打电话了。
        IoTAuth.removeActivePushCallback(ArrayString(deviceId), this)
    }

}