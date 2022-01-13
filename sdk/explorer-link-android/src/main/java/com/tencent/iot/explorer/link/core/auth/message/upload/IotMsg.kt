package com.tencent.iot.explorer.link.core.auth.message.upload

import com.tencent.iot.explorer.link.core.auth.IoTAuth
import java.util.*

open class IotMsg {

    var reqId = UUID.randomUUID().toString()

    internal var action = ""

    internal var commonParams = ParamMap()

    private val params = ParamMap()

    init {
        params["params"] = commonParams
        commonParams["AppKey"] = IoTAuth.APP_KEY
    }

    override fun toString(): String {
        params["action"] = action
        params["reqId"] = reqId
        commonParams.toString()
        return params.toString()
    }

    open fun getMyAction():String{
        return action
    }

}