package com.tencent.iot.explorer.link.kitlink.util

import android.content.Context
import android.text.TextUtils
import com.tencent.iot.explorer.link.App
import com.tencent.iot.explorer.link.core.log.L
import com.tencent.iot.explorer.link.kitlink.activity.BaseActivity
import com.tencent.iot.explorer.link.kitlink.consts.CommonField
import com.tencent.iot.explorer.link.kitlink.entity.*
import com.tencent.iot.explorer.link.util.SharePreferenceUtil
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
        if (!TextUtils.isEmpty(token)) {
            user = User()
            user?.Token = token
            user?.ExpireAt = expireAt
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