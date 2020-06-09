package com.mvp.view

import com.mvp.ParentView

interface DeviceDetailView : ParentView {

    fun deleteSuccess()

    fun fail(message: String)

}