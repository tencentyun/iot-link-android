package com.tencent.iot.explorer.link.core.auth.entity

class StandardPanelPropertyConfig {
    var id = "" // 快捷键对应物模型id
    var ui: StandardPanelPropertyUIConfig? = null // 快捷键物模型的ui配置
    var deviceData: DeviceDataEntity? = null
}