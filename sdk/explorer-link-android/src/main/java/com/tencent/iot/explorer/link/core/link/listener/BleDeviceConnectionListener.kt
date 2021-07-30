package com.tencent.iot.explorer.link.core.link.listener

import com.tencent.iot.explorer.link.core.link.entity.BleDeviceInfo
import com.tencent.iot.explorer.link.core.link.exception.TCLinkException

interface BleDeviceConnectionListener {
    fun onBleDeviceConnected()
    fun onBleDeviceDisconnected(exception : TCLinkException)
    fun onBleDeviceInfo(bleDeviceInfo: BleDeviceInfo)
    fun onBleSetWifiModeResult(success: Boolean)
}