package com.tencent.iot.explorer.link.core.auth.response

import com.tencent.iot.explorer.link.core.link.entity.ShareUserEntity

/**
 * 分享用户响应实体
 */
class ShareUserResponse {

    var Users: List<ShareUserEntity>? = null
    var RequestId = ""
    var Total = 0

}