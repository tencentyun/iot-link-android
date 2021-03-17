package com.tencent.iot.explorer.link.kitlink.entity

import com.tencent.iot.explorer.link.core.auth.message.upload.ArrayString

class BluetoothDevicesRequired {
    var services = ArrayString()
    var allowDuplicatesKey = false
    var interval = 0
    var powerLevel = "medium"
}