package com.tencent.iot.explorer.link.core.auth.entity

class ProductUIDevShortCutConfig {
    var powerSwitch = "" // 需要渲染开关的对应物模型id，如为空则不需要渲染开关
    var shortcut: MutableList<StandardPanelPropertyConfig> = ArrayList() // 快捷键列表，为空则不需要渲染
    var devModeInfos = ArrayList<DevModeInfo>()
}