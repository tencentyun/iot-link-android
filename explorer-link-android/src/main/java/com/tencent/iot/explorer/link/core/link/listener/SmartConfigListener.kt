package com.tencent.iot.explorer.link.core.link.listener

import com.espressif.iot.esptouch.IEsptouchResult
import com.tencent.iot.explorer.link.core.link.entity.DeviceInfo
import com.tencent.iot.explorer.link.core.link.entity.SmartConfigStep
import com.tencent.iot.explorer.link.core.link.exception.TCLinkException
import java.net.InetAddress

interface SmartConfigListener {

    fun onSuccess(deviceInfo: DeviceInfo)

    fun deviceConnectToWifi(result: IEsptouchResult)

    fun onStep(step: SmartConfigStep)

    fun deviceConnectToWifiFail()

    fun onFail(exception: TCLinkException)

}