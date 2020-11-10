package com.tencent.iot.explorer.link.kitlink.response

import com.tencent.iot.explorer.link.kitlink.entity.AutomationListItem
import com.tencent.iot.explorer.link.kitlink.entity.SceneListItem
import com.tencent.iot.explorer.link.kitlink.entity.TimerListEntity

class AutomationListResponse {

    var RequestId = ""
    var List = arrayListOf<AutomationListItem>()

}