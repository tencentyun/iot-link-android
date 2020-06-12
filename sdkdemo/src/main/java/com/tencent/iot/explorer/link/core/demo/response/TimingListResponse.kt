package com.tencent.iot.explorer.link.core.demo.response

import com.tencent.iot.explorer.link.core.demo.entity.TimingProject


/**
 * 云端定时列表响应实体
 */
class TimingListResponse {

    var RequestId = ""
    var Total = 0
    var TimerList = arrayListOf<TimingProject>()

}