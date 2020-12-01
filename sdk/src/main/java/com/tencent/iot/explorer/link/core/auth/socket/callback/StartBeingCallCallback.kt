package com.tencent.iot.explorer.link.core.auth.socket.callback

import com.tencent.iot.explorer.link.core.link.entity.TRTCParamsEntity

interface StartBeingCallCallback {

    fun startBeingCall(callingType: Int, deviceId: String)
}