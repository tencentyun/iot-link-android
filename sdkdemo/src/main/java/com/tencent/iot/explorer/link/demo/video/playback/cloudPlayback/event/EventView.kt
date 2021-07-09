package com.tencent.iot.explorer.link.demo.video.playback.cloudPlayback.event

import com.tencent.iot.explorer.link.demo.common.mvp.ParentView


interface EventView : ParentView {
    fun eventReady(events : MutableList<ActionRecord>)
}