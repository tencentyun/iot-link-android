package com.tencent.iot.explorer.link.core.link.listener

import com.tencent.iot.explorer.link.core.link.entity.DeviceInfo

interface IotLinkListener {

    fun onSuccess(deviceInfo: DeviceInfo)

    fun onFail(code: String, msg: String)

}