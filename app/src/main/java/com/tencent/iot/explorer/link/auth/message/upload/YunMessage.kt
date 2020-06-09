package com.auth.message.upload

import com.auth.consts.SocketField
import com.kitlink.App
import java.util.*

open class YunMessage : IotMsg() {

    var Action = ""
    val RequestId = UUID.randomUUID().toString()

    private val actionParams = ParamMap()

    init {
        action = "YunApi"
        commonParams[SocketField.ACTION_PARAM] = actionParams

        actionParams[SocketField.PLATFORM] = "Android"
        actionParams[SocketField.REQUEST_ID] = RequestId
        actionParams[SocketField.ACCESS_TOKEN] = App.data.getToken()
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
        commonParams[SocketField.ACTION] = Action
        actionParams.toString()
        return super.toString()
    }

    override fun getMyAction(): String {
        return Action
    }

}