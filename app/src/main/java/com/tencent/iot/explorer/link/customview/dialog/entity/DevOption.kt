package com.tencent.iot.explorer.link.customview.dialog.entity

import com.alibaba.fastjson.JSONObject
import com.tencent.iot.explorer.link.kitlink.entity.ModeInt

class DevOption {
    companion object {
        const val TYPE_LIST = 1
        const val TYPE_BAR = 2
    }

    var res = ""
    var optionName = ""
    var value = ""
    var key = ""
    var id = ""
    var mapJson: JSONObject? = null  // type 1 生效
    var modeInt: ModeInt? = null  // type 2 生效
    var type = 0 // 当前只有 1 和 2 可以操作
}