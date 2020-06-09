package com.tenext.auth.message.upload

import com.tenext.auth.IoTAuth
import java.util.*

open class YunMessage : IotMsg() {

    var Action = ""
    val RequestId = UUID.randomUUID().toString()

    private val actionParams = ParamMap()

    init {
        action = "YunApi"
        commonParams["ActionParams"] = actionParams

        actionParams["Platform"] = "Android"
        actionParams["RequestId"] = RequestId
        actionParams["AccessToken"] = IoTAuth.user.Token
    }

    /**
     * 把数据添加到actionParams中
     */
    fun addValue(key: String, value: Any): YunMessage {
        actionParams[key] = value
        return this
    }

    /**
     * 把数据添加到commonParams中
     */
    fun addCommonValue(key: String, value: Any?): YunMessage {
        commonParams[key] = value
        return this
    }

    override fun toString(): String {
        commonParams["Action"] = Action
        actionParams.toString()
        return super.toString()
    }

    override fun getMyAction(): String {
        return Action
    }

}