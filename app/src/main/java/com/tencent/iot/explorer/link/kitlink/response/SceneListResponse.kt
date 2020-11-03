package com.tencent.iot.explorer.link.kitlink.response

import com.tencent.iot.explorer.link.kitlink.entity.SceneListItem
import com.tencent.iot.explorer.link.kitlink.entity.TimerListEntity

/**
 * 云端定时列表响应实体
 */
class SceneListResponse {

    var RequestId = ""
    var Total = 0
    var SceneList = arrayListOf<SceneListItem>()

}