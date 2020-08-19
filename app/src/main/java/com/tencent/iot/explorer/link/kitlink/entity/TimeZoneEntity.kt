package com.tencent.iot.explorer.link.kitlink.entity

import android.service.quicksettings.Tile
import com.tencent.iot.explorer.link.App
import com.tencent.iot.explorer.link.kitlink.consts.CommonField
import com.tencent.iot.explorer.link.util.SharePreferenceUtil
import java.util.*

class TimeZoneEntity: Comparable<TimeZoneEntity> {
    var TZ = ""
    var Title = ""
    override fun compareTo(other: TimeZoneEntity): Int {
        if (TZ != other.TZ) {
            return TZ.compareTo(other.TZ)
        } else {
            if (Title.matches(Regex("[a-zA-Z]+")))
                return Title.compareTo(other.Title)
            else
                return -Title.compareTo(other.Title)
        }
    }
}