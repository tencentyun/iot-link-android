package com.tencent.iot.explorer.link.kitlink.consts

object SocketConstants {


    //主机
    const val host = "wss://iot.cloud.tencent.com/ws/explorer"

    //获取手机验证码的类型
    const val register = "register"
    const val reset_pwd = "resetpass"
    const val login = "login"

    const val ok = "OK"

    //上传 path
    const val app_cos_auth_path = "iotexplorer-app-logs/user_{uin}/"

}