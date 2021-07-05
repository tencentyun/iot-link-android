package com.tencent.iot.explorer.link.demo.video.mvp.view

import com.tencent.iot.explorer.link.demo.common.mvp.ParentView
import com.tencent.iot.explorer.link.demo.video.entity.ActionRecord


interface EventView : ParentView {
    fun eventReady(events : MutableList<ActionRecord>)
}