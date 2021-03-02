package com.tencent.iot.explorer.link.kitlink.entity

class LocationResult {
    var location : Location? = null
    var address = ""
    var formatted_addresses: FormattedAddresses = FormattedAddresses()
    var poi_count = 0
    var pois : MutableList<Postion> = ArrayList()
}