package com.tencent.iot.explorer.link.kitlink.response

import com.tencent.iot.explorer.link.kitlink.entity.FamilyEntity

/**
 *  家庭响应实体
 */
class FamilyListResponse {

    var RequestId = ""
    var FamilyList = arrayListOf<FamilyEntity>()
    var Total = 0

}