package com.tencent.iot.explorer.link.core.auth.entity

import android.text.TextUtils
import com.alibaba.fastjson.JSON

class Device {

    var DeviceId = ""
    var ProductId = ""
    var DeviceName = ""
    var AliasName = ""
    var UserID = ""
    var FamilyId = ""
    var RoomId = ""
    var IconUrl = ""
    var CreateTime = 0L
    var UpdateTIme = 0L

    var online = 0

    fun getAlias(): String {
        return if (TextUtils.isEmpty(AliasName)) {
            DeviceName
        } else {
            AliasName
        }
    }

    override fun toString(): String {
        return JSON.toJSONString(this)
    }

}