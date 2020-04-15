package com.kitlink.response

import com.kitlink.entity.TimerListEntity

/**
 * 云端定时列表响应实体
 */
class TimerListResponse {

    var RequestId = ""
    var Total = 0
    var TimerList = arrayListOf<TimerListEntity>()

}