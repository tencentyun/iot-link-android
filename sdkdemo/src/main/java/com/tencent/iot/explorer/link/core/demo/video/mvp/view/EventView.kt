package com.tencent.iot.explorer.link.core.demo.video.mvp.view

import com.tencent.iot.explorer.link.core.demo.mvp.ParentView
import com.tencent.iot.explorer.link.core.demo.video.entity.ActionRecord


interface EventView : ParentView {
    fun eventReady(events : MutableList<ActionRecord>)
}