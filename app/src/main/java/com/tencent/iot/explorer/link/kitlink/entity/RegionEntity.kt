package com.tencent.iot.explorer.link.kitlink.entity

import com.tencent.iot.explorer.link.T
import com.tencent.iot.explorer.link.core.utils.Utils

class RegionEntity : Comparable<RegionEntity>{
    var Title = ""
    var TitleEN = ""
    var RegionID = ""
    var Region = ""
    var CountryCode = ""
    override fun compareTo(other: RegionEntity): Int {
        return if (Utils.isChineseSystem(T.getContext())) {
            -Title.compareTo(other.Title)
        } else {
            TitleEN.compareTo(other.TitleEN)
        }
    }
}