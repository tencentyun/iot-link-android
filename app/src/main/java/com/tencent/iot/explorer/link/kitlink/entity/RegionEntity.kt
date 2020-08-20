package com.tencent.iot.explorer.link.kitlink.entity

import com.tencent.iot.explorer.link.kitlink.util.CommonUtils
import com.tencent.iot.explorer.link.util.T
import java.util.*

class RegionEntity : Comparable<RegionEntity>{
    var Title = ""
    var TitleEN = ""
    var RegionID = ""
    var Region = ""
    var CountryCode = ""
    override fun compareTo(other: RegionEntity): Int {
        return if (CommonUtils.isChineseSystem()) {
            -Title.compareTo(other.Title)
        } else {
            TitleEN.compareTo(other.TitleEN)
        }
    }
}