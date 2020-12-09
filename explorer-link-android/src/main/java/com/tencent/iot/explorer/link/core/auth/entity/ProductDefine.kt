package com.tencent.iot.explorer.link.core.auth.entity

abstract class ProductDefine {

    var type = ""

    abstract fun getText(value: String): String

}