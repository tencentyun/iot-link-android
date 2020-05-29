package com.kitlink.device.softap

import com.kitlink.device.DeviceInfo

interface SoftAPListener {

    fun onSuccess(deviceInfo: DeviceInfo)

    fun reconnectedSuccess(deviceInfo: DeviceInfo)

    fun reconnectedFail(deviceInfo: DeviceInfo, ssid: String)

    fun onStep(step: SoftAPStep)

    fun onFail(code: String, msg: String)

}