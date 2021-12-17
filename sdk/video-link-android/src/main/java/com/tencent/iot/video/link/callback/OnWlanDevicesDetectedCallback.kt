package com.tencent.iot.video.link.callback

import com.tencent.iot.video.link.entity.WlanRespBody

interface OnWlanDevicesDetectedCallback {
    fun onMessage(version: String, resp: WlanRespBody): Boolean
}