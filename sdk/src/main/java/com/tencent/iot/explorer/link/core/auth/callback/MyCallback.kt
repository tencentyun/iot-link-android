package com.tencent.iot.explorer.link.core.auth.callback

import com.tencent.iot.explorer.link.core.auth.response.BaseResponse

/**
 *  响应回调
 */
interface MyCallback {

    fun fail(msg: String?, reqCode: Int)

    fun success(response: BaseResponse, reqCode: Int)

}