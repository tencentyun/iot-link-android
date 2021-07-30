package com.tencent.iot.explorer.link.core.link.listener

import com.tencent.iot.explorer.link.core.link.exception.TCLinkException

interface BleDeviceConnectionListener {
    fun onBleDeviceConnected() {}
    fun onBleDeviceDisconnected(exception : TCLinkException)
}