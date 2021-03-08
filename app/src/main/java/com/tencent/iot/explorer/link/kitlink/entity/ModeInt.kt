package com.tencent.iot.explorer.link.kitlink.entity

import com.tencent.iot.explorer.link.core.auth.entity.OpValue

class ModeInt {
    var type = ""
    var min = 0.0F
    var max = 0.0F
    var start = 0.0F
    var step = 0.0F
    var unit = ""
    var showOp = false
    var ifInteger = true
    var op = OpValue.OP_EQ //  eq 等于  ne 不等于  gt 大于  lt 小于  ge 大等于  le 小等于
}