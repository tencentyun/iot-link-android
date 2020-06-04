package com.tenext.auth.callback

import com.tenext.auth.response.BaseResponse

/**
 *  响应回调
 */
interface MyCallback {

    fun fail(msg: String?, reqCode: Int)

    fun success(response: BaseResponse, reqCode: Int)

}