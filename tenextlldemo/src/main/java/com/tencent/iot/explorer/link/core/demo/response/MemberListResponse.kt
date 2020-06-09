package com.tencent.iot.explorer.link.core.demo.response

import com.tencent.iot.explorer.link.core.demo.entity.Member

/**
 * 家庭成员列表响应实体
 */
class MemberListResponse {
    var MemberList = arrayListOf<Member>()
    var Total =0
    var RequestId =""
}