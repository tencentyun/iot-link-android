package com.tencent.iot.explorer.link.core.auth

import android.content.Context
import android.content.SharedPreferences
import android.text.TextUtils
import com.tencent.iot.explorer.link.core.auth.entity.Device
import com.tencent.iot.explorer.link.core.auth.entity.Family
import com.tencent.iot.explorer.link.core.auth.entity.Room
import com.tencent.iot.explorer.link.core.auth.entity.User
import com.tencent.iot.explorer.link.core.auth.impl.*
import com.tencent.iot.explorer.link.core.auth.listener.LoginExpiredListener
import com.tencent.iot.explorer.link.core.auth.message.upload.ActivePushMessage
import com.tencent.iot.explorer.link.core.auth.message.upload.ArrayString
import com.tencent.iot.explorer.link.core.auth.service.*
import com.tencent.iot.explorer.link.core.auth.socket.WSClientManager
import com.tencent.iot.explorer.link.core.auth.socket.callback.ActivePushCallback
import com.tencent.iot.explorer.link.core.auth.socket.callback.MessageCallback
import com.tencent.iot.explorer.link.core.log.L

/**
 * SDK授权认证管理
 */
object IoTAuth {

    var APP_KEY = ""
    var APP_SECRET = ""

    //登录过期监听器
    internal var loginExpiredListener: LoginExpiredListener? = null

    /**
     * 家庭列表
     */
    val familyList by lazy {
        arrayListOf<Family>()
    }

    /**
     * 设备列表
     */
    val roomList by lazy {
        arrayListOf<Room>()
    }

    /**
     * 设备列表
     */
    val deviceList by lazy {
        arrayListOf<Device>()
    }

    /**
     * 用户实体
     */
    val user: User by lazy {
        User()
    }

    /**
     * 登录
     */
    val loginImpl: LoginImpl by lazy {
        LoginService()
    }

    /**
     * 注册
     */
    val registerImpl: RegisterImpl by lazy {
        RegisterService()
    }

    /**
     * 密码
     */
    val passwordImpl: PasswordImpl by lazy {
        PasswordService()
    }

    /**
     * 用户模块
     */
    val userImpl: UserImpl by lazy {
        UserService()
    }

    /**
     * 设备模块
     */
    val deviceImpl: DeviceImpl by lazy {
        DeviceService()
    }

    /**
     * 设备分享模块
     */
    val shareImpl: ShareImpl by lazy {
        ShareService()
    }

    /**
     * 云端定时模块
     */
    val timingImpl: TimingImpl by lazy {
        TimingService()
    }

    /**
     * 家庭模块
     */
    val familyImpl: FamilyImpl by lazy {
        FamilyService()
    }

    /**
     * 成员模块
     */
    val memberImpl: MemberImpl by lazy {
        MemberService()
    }

    /**
     * 房间模块
     */
    val roomImpl: RoomImpl by lazy {
        RoomService()
    }

    /**
     * 消息模块
     */
    val messageImpl: MessageImpl by lazy {
        MessageService()
    }

    /**
     * 验证码模块
     */
    internal val verifyImpl: VerifyImpl by lazy {
        VerifyService()
    }

    /**
     * 初始化WebSocket
     */
    fun init(APP_KEY: String, APP_SECRET: String) {
        if (TextUtils.isEmpty(APP_KEY)) {
            throw Exception("APP_KEY can not be empty")
        }
        if (TextUtils.isEmpty(APP_SECRET)) {
            throw Exception("APP_SECRET can not be empty")
        }
        this.APP_KEY = APP_KEY
        this.APP_SECRET = APP_SECRET
        WSClientManager.instance.init()
    }

    /**
     * 接入共享式bugly
     */
    fun registerSharedBugly(c: Context) {
        val context = c.applicationContext
        val settings = context.getSharedPreferences("BuglySdkInfos", Context.MODE_PRIVATE)
        val editor = settings.edit()
        // TODO: 版本号配置成动态获取
        editor.putString("c1302acd44", "1.3") //SDK_APP_ID, SDK_VERSION
        editor.apply()
    }

    /**
     * 打开日志
     */
    fun openLog(isOpen: Boolean) {
        L.isLog = isOpen
    }

    /**
     * 添加登录过期监听器
     */
    fun addLoginExpiredListener(listener: LoginExpiredListener) {
        loginExpiredListener = listener
    }

    /**
     * 设置长连接日志tag,物联平台后台调试用
     */
    fun setWebSocketTag(tag: String) {
        WSClientManager.setDebugTag(tag)
    }

    /**
     * 注册监听
     * @param deviceIds 多个设备的deviceId
     * @param callback MessageCallback注册监听请求回调，可为null
     */
    fun registerActivePush(deviceIds: ArrayString, callback: MessageCallback?) {
        val msg = ActivePushMessage(deviceIds)
        if (callback == null)
            WSClientManager.instance.sendMessage(msg.toString())
        else
            WSClientManager.instance.sendRequestMessage(msg, callback)
        WSClientManager.instance.addDeviceIds(deviceIds)
        WSClientManager.instance.sendHeartMessage()
    }

    /**
     * 添加监听器
     * @param callback ActivePushCallback，设备数据推送到客户端的回调。在调用registerActivePush成功后，
     * 添加有监听器的页面可以收到推送数据，最多30个页面，超出时会移除最先添加的监听器，即先进先出（FIFO）
     */
    fun addActivePushCallback(callback: ActivePushCallback) {
        WSClientManager.instance.addActivePushCallback(callback)
    }

    /**
     * 移除监听器
     */
    fun removeActivePushCallback(deviceIds: ArrayString, callback: ActivePushCallback) {
        WSClientManager.instance.removeActivePushCallback(callback)
        WSClientManager.instance.removeDeviceIds(deviceIds)
    }

    /**
     * 移除所有监听器
     */
    fun removeAllActivePushCallback() {
        WSClientManager.instance.removeAllActivePushCallback()
    }

    /**
     * 退出登录
     */
    fun logout() {
        familyList.clear()
        roomList.clear()
        deviceList.clear()
        user.clear()
    }

    /**
     *  销毁
     */
    fun destroy() {
        logout()
        APP_KEY = ""
        APP_SECRET = ""
        WSClientManager.instance.destroy()
    }

}