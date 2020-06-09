package com.auth.message.upload

import com.auth.IoTAuth
import com.auth.consts.SocketField

open class IotMsg {

    var reqId = -1

    internal var action = ""

    internal var commonParams = ParamMap()

    private val params = ParamMap()

    init {
        params["params"] = commonParams
        commonParams[SocketField.APP_KEY] = IoTAuth.appKey
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