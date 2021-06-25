package com.tencent.iot.explorer.link.core.demo.video.entity

import java.util.ArrayList

class VideoProductInfo {
    companion object {
        var DEV_TYPE_IPC = 1
        var DEV_TYPE_NVR = 2
    }

    var ProductId = ""
    var ProductName = ""
    var DeviceType = 1  //产品设备类型（普通设备) 1.普通设备
    var EncryptionType = 0 //认证方式：2：PSK
    var Features = ArrayList<String>() //设备功能码
    var ChipOs = "" //操作系统
    var ChipManufactureId = "" //芯片厂商id
    var ChipId = "" //芯片id
    var ProductDescription = "" //产品描述信息
    var CreateTime = 0L //创建时间unix时间戳
    var UpdateTime = 0L //修改时间unix时间戳
}