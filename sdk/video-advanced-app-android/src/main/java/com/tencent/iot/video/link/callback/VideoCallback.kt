package com.tencent.iot.video.link.callback

/**
 *  响应回调
 */
interface VideoCallback {

    fun fail(msg: String?, reqCode: Int)

    fun success(response: String?, reqCode: Int)

}