package com.tencent.iot.explorer.link.rtc.model

open class RoomKey {
    var appId = 0
    var roomId = ""
    var callType = TRTCCalling.TYPE_AUDIO_CALL
    var userId = ""
    var userSig = ""
}