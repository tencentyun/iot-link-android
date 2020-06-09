package com.tencent.iot.explorer.link.core.auth.entity

class PanelConfig {

    var profile = Profile()
    var Global = ""
    var Panel = PanelEntity()
    var ShortCut = ""
    var WifiSoftAP = ""
    var DeviceInfo = ""

}

class Profile {
    var ProductId = ""
}

class PanelEntity {
    var type = ""
    var standard = Standard()
}