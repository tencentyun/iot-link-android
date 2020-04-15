package com.mvp.view

import com.mvp.ParentView

interface HomeFragmentView : ParentView {

    fun showFamily()

    fun showRoomList()

    fun showDeviceList(
        deviceSize: Int,
        roomId: String,
        deviceListEnd: Boolean,
        shareDeviceListEnd: Boolean
    )

    fun showDeviceOnline()

}