package com.tencent.iot.explorer.link.core.auth.entity

/**
 * 设备所属产品类型：event
 */
class ProductEvent {

    var id = ""
    var name = ""
    var desc = ""
    var type = ""
    var required = false
    var params = arrayListOf<EventParam>()

}