package com.tencent.iot.explorer.link.kitlink.response

import com.tencent.iot.explorer.link.kitlink.entity.SceneListItem

/**
 * 云端定时列表响应实体
 */
class SceneListResponse {

    var RequestId = ""
    var Total = 0
    var SceneList = arrayListOf<SceneListItem>()

}