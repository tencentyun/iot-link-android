package com.tencent.iot.explorer.link.core.auth.socket.callback

import com.tencent.iot.explorer.link.core.link.entity.TRTCParamsEntity

interface EnterRoomCallback {

    fun enterRoom(callingType: Int, params: TRTCParamsEntity, deviceId: String)
}