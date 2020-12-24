package com.tencent.iot.explorer.link.mvp.view

import com.tencent.iot.explorer.link.mvp.ParentView
import com.tencent.iot.explorer.link.rtc.model.RoomKey

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

    fun enterRoom(room: RoomKey, deviceId: String)

}