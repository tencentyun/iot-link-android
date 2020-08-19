package com.tencent.iot.explorer.link.kitlink.entity

class RegionEntity : Comparable<RegionEntity>{
    var Title = ""
    var RegionID = ""
    var Region = ""
    override fun compareTo(other: RegionEntity): Int {
        return Region.compareTo(other.Region)
    }
}