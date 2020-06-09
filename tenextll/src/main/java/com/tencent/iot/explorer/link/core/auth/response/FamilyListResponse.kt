package com.tencent.iot.explorer.link.core.auth.response

import com.tencent.iot.explorer.link.core.auth.entity.Family

class FamilyListResponse {

    var RequestId = ""
    var FamilyList = arrayListOf<Family>()
    var Total = 0

}