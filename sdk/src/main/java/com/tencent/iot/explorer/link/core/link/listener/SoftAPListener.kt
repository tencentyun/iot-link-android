package com.tencent.iot.explorer.link.core.link.listener

import com.tencent.iot.explorer.link.core.link.entity.DeviceInfo
import com.tencent.iot.explorer.link.core.link.entity.SoftAPStep

interface SoftAPListener {

    fun onSuccess(deviceInfo: DeviceInfo)

    fun reconnectedSuccess(deviceInfo: DeviceInfo)

    fun reconnectedFail(deviceInfo: DeviceInfo, ssid: String)

    fun onStep(step: SoftAPStep)

    fun onFail(code: String, msg: String)

}