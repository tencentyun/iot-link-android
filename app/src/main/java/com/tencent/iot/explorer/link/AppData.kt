package com.tencent.iot.explorer.link

import android.content.Context
import android.text.TextUtils
import com.tencent.iot.explorer.link.core.auth.IoTAuth
import com.tencent.iot.explorer.link.core.auth.entity.DeviceEntity
import com.tencent.iot.explorer.link.core.auth.entity.FamilyEntity
import com.tencent.iot.explorer.link.core.auth.entity.RoomEntity
import com.tencent.iot.explorer.link.core.auth.entity.User
import com.tencent.iot.explorer.link.core.log.L
import com.tencent.iot.explorer.link.kitlink.activity.BaseActivity
import com.tencent.iot.explorer.link.kitlink.consts.CommonField
import com.tencent.iot.explorer.link.kitlink.entity.*
import com.tencent.iot.explorer.link.core.utils.SharePreferenceUtil
import com.tencent.iot.explorer.link.core.utils.Utils
import com.tencent.iot.explorer.link.customview.recyclerview.SelectedArrayList
import java.util.*

/**
 * APP数据
 */
class AppData private constructor() {

    private object AppDataHolder {
        val data = AppData()
    }

    companion object {
        val instance = AppDataHolder.data
    }

    //信鸽token
    var xg_token = ""
    var bindDeviceToken = ""
    //用户
    private var user: User? = null
    var userInfo = UserInfo()
    var userSetting = UserSetting()
    var regionId = "1"
    var region = "ap-guangzhou"
    var appLifeCircleId = "0"
    var notificationId = 0
    var isForeground = false
    var callingDeviceId = "" //主动呼叫的设备的id

    //activity列表
    val activityList = LinkedList<BaseActivity>()
    //当前家庭
    private var familyEntity = FamilyEntity()
    //当前房间
    private var currentRoom = RoomEntity()

    //首页刷新级别:0刷新家庭、房间、设备;1刷新房间、设备；2刷新设备
    private var refreshLevel = 0

    //是否需要更新
    var refresh = true

    //家庭列表
    val familyList = SelectedArrayList<FamilyEntity>()
    val roomList = SelectedArrayList<RoomEntity>()
    val deviceList = arrayListOf<DeviceEntity>()
    // 共享设备列表
    val shareDeviceList = arrayListOf<DeviceEntity>()

    // 推荐设备分类列表
    var recommendDeviceCategoryList = arrayListOf<DeviceCategoryEntity>()

    // 屏幕宽度
    var screenWith = 0

    // 当前VerticalTab位置
    var tabPosition = 0

    // 设备的类目数
    var numOfCategories = 0

    /**
     * 重置刷新级别到设备级别
     */
    fun resetRefreshLevel() {
        refresh = false
        refreshLevel = 2
    }

    /**
     * 设置刷新级别
     */
    fun setRefreshLevel(value: Int) {
        refresh = true
        if (value <= refreshLevel) {
            refreshLevel = value
        }
        L.e("setRefreshLevel() refreshLevel=$refreshLevel,refresh=$refresh")
    }

    fun getRefreshLevel(): Int {
        return refreshLevel
    }

    fun setCurrentFamily(position: Int) {
        this.familyEntity = familyList[position]
    }

    fun setCurrentRoom(position: Int) {
        this.currentRoom = roomList[position]
    }

    /**
     * 获取当前房间
     */
    fun getCurrentRoom(): RoomEntity {
        //判断当前room是否还存在列表中：去除删除操作的bug
        roomList.forEachIndexed { _, entity ->
            if (entity.RoomId == currentRoom.RoomId) {
                return entity
            }
        }
        currentRoom = if (roomList.isNotEmpty()) {
            roomList[0]
        } else {
            RoomEntity()
        }
        return currentRoom
    }

    fun getCurrentFamily(): FamilyEntity {
        familyList.forEachIndexed { index, entity ->
            if (entity.FamilyId == familyEntity.FamilyId) {
                familyList.addSingleSelect(index)
                return entity
            }
        }
        familyEntity = if (familyList.isNotEmpty()) {
            familyList.addSingleSelect(0)
            familyList[0]
        } else {
            FamilyEntity()
        }
        return familyEntity
    }

    /**
     * 设置用户信息
     */
    fun setAppUser(user: User?) {
        this.user = user
    }

    /**
     * 获取token
     */
    @Synchronized
    fun getToken(): String {
        user?.let {
            if (System.currentTimeMillis() / 1000 >= it.ExpireAt) {
                L.e("用户登录信息过期，请重新登录")
                return ""
            }
            return it.Token
        }
        return ""
    }

    /**
     * 读取本地用户数据
     */
    fun readLocalUser(context: Context) {
        val token = SharePreferenceUtil.getString(context, App.CONFIG, CommonField.TOKEN)
        val expireAt = SharePreferenceUtil.getLong(context, App.CONFIG, CommonField.EXPIRE_AT)
        var countryInfo = Utils.getStringValueFromXml(T.getContext(), CommonField.COUNTRY_INFO, CommonField.COUNTRY_INFO)

        if (!TextUtils.isEmpty(token)) {
            user = User()
            user?.Token = token
            user?.ExpireAt = expireAt
            IoTAuth.user.Token = token
            IoTAuth.user.ExpireAt = expireAt
            if (countryInfo != null) {
                if (!countryInfo.contains("+")) return
                countryInfo.split("+").let {
                    App.data.regionId = it[1]
                    App.data.region = it[3]
                }
            }
        }
    }

    fun clear() {
        refreshLevel = 0
        refresh = true
        xg_token = ""
        familyEntity = FamilyEntity()
        familyList.clear()
        roomList.clear()
        deviceList.clear()
        shareDeviceList.clear()
        SharePreferenceUtil.clearString(App.activity, App.CONFIG, CommonField.USER_ID)
        SharePreferenceUtil.clearString(App.activity, App.CONFIG, CommonField.EXPIRE_AT)
        SharePreferenceUtil.clearString(App.activity, App.CONFIG, CommonField.TOKEN)
        user = User()
    }
}