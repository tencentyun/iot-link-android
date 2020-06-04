package com.tenext.auth.message.payload

internal class PayloadMessage {

    var action = ""
    var params: Param? = null
    var push = false

    inner class Param {
        var Time = ""
        var Type = "Template"
        var SubType = "Report"
        var Topic = ""
        var Payload = ""
        var Seq = 0L
        var DeviceId = ""
    }

}