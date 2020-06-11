package com.tencent.iot.explorer.link.kitlink.device.softap

import com.tencent.iot.explorer.link.kitlink.device.DeviceInfo

interface SoftAPListener {

    fun onSuccess(deviceInfo: DeviceInfo)

    fun reconnectedSuccess(deviceInfo: DeviceInfo)

    fun reconnectedFail(deviceInfo: DeviceInfo, ssid: String)

    fun onStep(step: SoftAPStep)

    fun onFail(code: String, msg: String)

}