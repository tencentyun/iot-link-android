package com.tencent.iot.explorer.link.kitlink.device.smartconfig

import com.espressif.iot.esptouch.IEsptouchResult
import com.tencent.iot.explorer.link.kitlink.device.DeviceInfo
import com.tencent.iot.explorer.link.kitlink.device.TCLinkException

interface SmartConfigListener {

    fun onSuccess(deviceInfo: DeviceInfo)

    fun deviceConnectToWifi(result: IEsptouchResult)

    fun onStep(step: SmartConfigStep)

    fun deviceConnectToWifiFail()

    fun onFail(exception: TCLinkException)

}