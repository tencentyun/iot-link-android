package com.tencent.iot.explorer.link.core.link.listener

import com.tencent.iot.explorer.link.core.link.entity.*
import com.tencent.iot.explorer.link.core.link.exception.TCLinkException

interface BleDeviceConnectionListener {
    fun onBleDeviceConnected()
    fun onBleDeviceFounded(bleDevice: BleDevice)
    fun onBleDeviceDisconnected(exception : TCLinkException)
    fun onBleDeviceInfo(bleDeviceInfo: BleDeviceInfo)
    fun onBleSetWifiModeResult(success: Boolean)
    fun onBleSendWifiInfoResult(success: Boolean)
    fun onBleWifiConnectedInfo(wifiConnectInfo: BleWifiConnectInfo)
    fun onBlePushTokenResult(success: Boolean)
    fun onMtuChanged(mtu: Int, status: Int)
    fun onBleBindSignInfo(bleDevBindCondition: BleDevBindCondition)
    fun onBleSendSignInfo(bleDevSignResult: BleDevSignResult)
    fun onBleUnbindSignInfo(signInfo: String)
    fun onBlePropertyValue(bleDeviceProperty: BleDeviceProperty)
    fun onBleControlPropertyResult(result: Int)
    fun onBleRequestCurrentProperty()
    fun onBleNeedPushProperty(eventId: Int, bleDeviceProperty: BleDeviceProperty)
    fun onBleReportActionResult(reason: Int, actionId: Int, bleDeviceProperty: BleDeviceProperty)
}