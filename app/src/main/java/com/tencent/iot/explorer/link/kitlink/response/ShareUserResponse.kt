package com.tencent.iot.explorer.link.kitlink.response

import com.tencent.iot.explorer.link.kitlink.entity.ShareUserEntity

/**
 * 分享用户响应实体
 */
class ShareUserResponse {

    var Users: List<ShareUserEntity>? = null
    var RequestId = ""
    var Total = 0

}