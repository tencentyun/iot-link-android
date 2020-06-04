package com.kitlink.entity

import android.text.TextUtils

open class DeviceEntity {
    var DeviceId = ""
    var ProductId = ""
    var DeviceName = ""
    var AliasName = ""
    var UserID = ""
    var FamilyId = ""
    var RoomId = ""
    var IconUrl = ""
    var DeviceType = ""
    var CreateTime = 0L
    var UpdateTIme = 0L

    //在线状态
    var online = 0
    //共享设备
    var shareDevice = false

    fun getAlias(): String {
        return if (TextUtils.isEmpty(AliasName)) {
            DeviceName
        } else {
            AliasName
        }
    }
}