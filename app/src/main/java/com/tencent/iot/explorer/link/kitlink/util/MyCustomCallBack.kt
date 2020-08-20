package com.tencent.iot.explorer.link.kitlink.util

interface MyCustomCallBack {
    fun fail(msg: String?, reqCode: Int)

    fun success(str: String, reqCode: Int)
}