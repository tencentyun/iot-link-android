package com.tenext.auth.entity

abstract class ProductDefine {

    var type = ""

    abstract fun getText(value: String): String

}