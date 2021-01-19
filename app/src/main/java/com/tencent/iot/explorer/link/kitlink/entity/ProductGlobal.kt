package com.tencent.iot.explorer.link.kitlink.entity

import android.text.TextUtils
import android.util.Log
import com.alibaba.fastjson.JSON

class ProductGlobal {

    companion object {
        fun isProductGlobalLegal(productGlobalStr: String) : Boolean {
            if (TextUtils.isEmpty(productGlobalStr)) {
                return false
            }

            var json = JSON.parseObject(productGlobalStr)
            if (json == null) {
                return false
            }

            if (!json.containsKey("AddDeviceHintMsg") && !json.containsKey("IconUrlAdvertise")) {
                return false
            }
            return true
        }
    }

    var addDeviceHintMsg = ""
    var IconUrlAdvertise = ""
}