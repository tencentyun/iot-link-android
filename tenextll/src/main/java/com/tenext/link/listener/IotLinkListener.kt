package com.tenext.link.listener

import com.tenext.link.entity.DeviceInfo

interface IotLinkListener {

    fun onSuccess(deviceInfo: DeviceInfo)

    fun onFail(code: String, msg: String)

}