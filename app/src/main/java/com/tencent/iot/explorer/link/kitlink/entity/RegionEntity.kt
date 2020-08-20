package com.tencent.iot.explorer.link.kitlink.entity

class RegionEntity : Comparable<RegionEntity>{
    var Title = ""
    var RegionID = ""
    var Region = ""
    override fun compareTo(other: RegionEntity): Int {
        return if (Title.matches(Regex(".*[a-zA-Z]+.*")))
            Title.compareTo(other.Title)
        else
            -Title.compareTo(other.Title)
    }
}