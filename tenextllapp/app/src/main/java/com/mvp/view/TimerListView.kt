package com.mvp.view

import com.mvp.ParentView

interface TimerListView : ParentView {

    fun showTimerList(size: Int)

}