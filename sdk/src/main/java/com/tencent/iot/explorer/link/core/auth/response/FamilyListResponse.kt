package com.tencent.iot.explorer.link.core.auth.response

import com.tencent.iot.explorer.link.core.auth.entity.FamilyEntity

class FamilyListResponse {

    var RequestId = ""
    var FamilyList = arrayListOf<FamilyEntity>()
    var Total = 0

}