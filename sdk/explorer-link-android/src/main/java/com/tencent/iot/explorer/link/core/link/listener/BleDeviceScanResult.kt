package com.tencent.iot.explorer.link.core.link.listener

import com.tencent.iot.explorer.link.core.link.entity.BleDevice

interface BleDeviceScanResult {
    fun onBleDeviceFounded(bleDevice: BleDevice)
}