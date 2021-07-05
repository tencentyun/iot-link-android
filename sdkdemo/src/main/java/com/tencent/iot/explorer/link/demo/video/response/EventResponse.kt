package com.tencent.iot.explorer.link.demo.video.response

import com.tencent.iot.explorer.link.demo.video.entity.Event

class EventResponse {
    var requestId = ""
    var total = 0
    var events : MutableList<Event> = ArrayList()
}