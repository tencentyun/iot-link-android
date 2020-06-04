package com.tenext.demo.response

import com.tenext.demo.entity.TimingProject


/**
 * 云端定时列表响应实体
 */
class TimingListResponse {

    var RequestId = ""
    var Total = 0
    var TimerList = arrayListOf<TimingProject>()

}