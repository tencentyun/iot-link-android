package com.tencent.iot.explorer.link.kitlink.entity

/**
 *  消息实体
 */
class MessageEntity {

    var UserID = ""
    var FromUserID = ""
    var MsgID = ""
    var Category = 1  // 1设备，2家庭，3通知
    var MsgType = 0
    var MsgTitle = ""
    var MsgContent = ""
    var MsgTimestamp = 0L
    var ProductId = ""
    var DeviceName = ""
    var DeviceAlias = ""
    var FamilyId = ""
    var FamilyName = ""
    var RelateUserID = ""
    var Attachments: Attachment? = null
    var CreateAt = ""

    class Attachment {
        var ShareToken = ""
    }
}