package com.tencent.iot.explorer.link.core.test.callback

import com.tencent.iot.explorer.link.core.auth.response.BaseResponse

/**
 *  响应回调
 */
interface VideoCallback {

    fun fail(msg: String?, reqCode: Int)

    fun success(response: String?, reqCode: Int)

}