package com.kitlink.util

import com.kitlink.response.BaseResponse

/**
 *  响应回调
 */
interface MyCallback {

    fun fail(msg: String?, reqCode: Int)

    fun success(response: BaseResponse, reqCode: Int)

}