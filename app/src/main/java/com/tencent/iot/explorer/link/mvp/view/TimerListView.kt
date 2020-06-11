package com.tencent.iot.explorer.link.mvp.view

import com.tencent.iot.explorer.link.mvp.ParentView

interface TimerListView : ParentView {

    fun showTimerList(size: Int)

}