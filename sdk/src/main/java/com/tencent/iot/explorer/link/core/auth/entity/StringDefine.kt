package com.tencent.iot.explorer.link.core.auth.entity

class StringDefine:ProductDefine() {

    var min = 0
    var max = 0

    override fun getText(value: String): String {
        return value
    }
}