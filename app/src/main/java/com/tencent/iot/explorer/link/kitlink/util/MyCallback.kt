package com.tencent.iot.explorer.link.kitlink.util

import com.tencent.iot.explorer.link.kitlink.response.BaseResponse

/**
 *  响应回调
 */
interface MyCallback {

    fun fail(msg: String?, reqCode: Int)

    fun success(response: BaseResponse, reqCode: Int)

}