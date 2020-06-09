package com.tenext.auth.entity

class NumberDefine : ProductDefine() {

    var min = 0
    var max = 100
    var start = ""
    var step = ""
    var unit = ""

    override fun getText(value: String): String {
        return value + unit
    }
}