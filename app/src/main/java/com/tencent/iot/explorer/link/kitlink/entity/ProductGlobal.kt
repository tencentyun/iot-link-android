package com.tencent.iot.explorer.link.kitlink.entity

import com.alibaba.fastjson.JSON

class ProductGlobal {

    companion object {
        fun isProductGlobalLegal(productGlobalStr: String) : Boolean {
            var json = JSON.parseObject(productGlobalStr)
            if (!json.containsKey("AddDeviceHintMsg") && !json.containsKey("IconUrlAdvertise")) {
                return false
            }
            return true
        }
    }

    var addDeviceHintMsg = ""
    var IconUrlAdvertise = ""
}