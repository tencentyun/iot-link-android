package com.tenext.auth.response

import com.tenext.auth.entity.Family

class FamilyListResponse {

    var RequestId = ""
    var FamilyList = arrayListOf<Family>()
    var Total = 0

}