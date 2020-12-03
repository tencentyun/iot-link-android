package com.tencent.iot.explorer.link.core.demo

import com.tencent.iot.explorer.link.core.auth.IoTAuth
import com.tencent.iot.explorer.link.core.auth.entity.FamilyEntity
import com.tencent.iot.explorer.link.core.auth.entity.RoomEntity
import com.tencent.iot.explorer.link.core.demo.activity.BaseActivity
import com.tencent.iot.explorer.link.core.demo.entity.UserInfo
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

    //用户
    var userInfo = UserInfo()
    //当前家庭
    private var currentFamily = FamilyEntity()
    //当前房间
    private var currentRoom = RoomEntity()

    var bindDeviceToken = ""

    val activityList = LinkedList<BaseActivity>()

    var callingDeviceId = "" //主动呼叫的设备的id


    /**
     * 切换家庭
     */
    fun tabFamily(position: Int) {
        if (position < IoTAuth.familyList.size)
            this.currentFamily = IoTAuth.familyList[position]
    }

    /**
     * 获取当前家庭
     */
    fun getCurrentFamily(): FamilyEntity {
        //判断当前family是否还存在列表中：去除删除操作的bug
        IoTAuth.familyList.forEachIndexed { _, entity ->
            if (entity.FamilyId == currentFamily.FamilyId) {
                return entity
            }
        }
        currentFamily = if (IoTAuth.familyList.isNotEmpty()) {
            IoTAuth.familyList[0]
        } else {
            FamilyEntity()
        }
        return currentFamily
    }

    /**
     * 切换房间
     */
    fun tabRoom(position: Int) {
        if (position < IoTAuth.roomList.size)
            currentRoom = IoTAuth.roomList[position]
    }

    /**
     * 获取当前房间
     */
    fun getCurrentRoom(): RoomEntity {
        //判断当前room是否还存在列表中：去除删除操作的bug
        IoTAuth.roomList.forEachIndexed { _, entity ->
            if (entity.RoomId == currentRoom.RoomId) {
                return entity
            }
        }
        currentRoom = if (IoTAuth.roomList.isNotEmpty()) {
            IoTAuth.roomList[0]
        } else {
            RoomEntity()
        }
        return currentRoom
    }

    /**
     * 退出登录时调用
     */
    fun clear() {
        currentFamily = FamilyEntity()
        IoTAuth.familyList.clear()
        IoTAuth.roomList.clear()
        IoTAuth.deviceList.clear()
    }
}