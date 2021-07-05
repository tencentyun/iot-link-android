package com.tencent.iot.explorer.link.demo.core.entity

/**
 * 用户信息
 */
class UserInfo {

    var UserID = ""
    var CountryCode = ""
    var PhoneNumber = ""
    var Email = ""
    var WxOpenID = ""
    var NickName = ""
    var Avatar = ""

    fun update(userInfo: UserInfo){
        NickName = userInfo.NickName
        Avatar = userInfo.Avatar
        UserID = userInfo.UserID
        CountryCode = userInfo.CountryCode
        PhoneNumber = userInfo.PhoneNumber
        Email = userInfo.Email
        WxOpenID = userInfo.WxOpenID
    }

}