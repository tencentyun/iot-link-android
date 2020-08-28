package com.tencent.iot.explorer.link.kitlink.consts

object CommonField {

    /**************通用参数****************/
    const val ACTION = "Action"
    const val REQUEST_ID = "RequestId"
    //APP KEY
    const val APP_KEY = "AppKey"
    //安卓params
    const val ACTION_PARAM = "ActionParams"
    const val PATH = "path"
    //客户端平台
    const val PLATFORM = "Platform"
    //
    const val ACCESS_TOKEN = "AccessToken"
    //用户登录成功后返回的token
    const val TOKEN = "Token"
    //用户登录过期时间
    const val EXPIRE_AT = "ExpireAt"
    //用户申请注销的时间
    const val CANCEL_ACCOUNT_TIME = "CancelAccountTime"
    //时间戳
    const val TIMESTAMP = "Timestamp"
    //随机整数
    const val NONCE = "Nonce"
    //客户端IP地址
    const val CLIENT_IP = "ClientIp"
    //签名
    const val SIGNATURE = "Signature"
    //
    const val MSG_ID = "MsgID"
    const val MSG_TIMESTAMP = "MsgTimestamp"
    const val LIMIT = "Limit"
    const val FROM = "from"
    const val WAY_SOURCE = "fromH5"
    const val TOKEN_TICKET = "TokenTicket"
    const val TICKET = "ticket"
    const val HANDLER_NAME = "handlerName"
    const val MSG_TYPE = "msgType"

    /*******上传文件********/
    const val CREDENTIALS = "credentials"
    const val COS_CONFIG = "cosConfig"
    const val REGION = "region"
    const val BUCKET = "bucket"


    /*************** 注册 ****************/
    const val TYPE = "Type"
    const val COUNTRY_CODE = "CountryCode"
    const val REGION_ID = "RegionID"
    const val PHONE_NUMBER = "PhoneNumber"
    const val VERIFY_CODE = "VerificationCode"
    const val PWD = "Password"
    const val NEW_PWD = "NewPassword"
    const val EMAIL = "Email"
    const val PHONE = "phone"
    const val WX_OPENID = "WxOpenID"
    const val REGION_CHINA = "ap-guangzhou"
    const val REGION_USA = "na-ashburn"
    const val REGION_LIST_URL = "https://imgcache.qq.com/qzone/qzactStatics/qcloud/data/37/config1.js"


    /************返回结果通用字段*************/
    const val RESPONSE = "Response"
    const val ERROR = "Error"
    const val DATA = "Data"
    const val CODE = "Code"
    const val MESSAGE = "Message"
    const val APP_DEVICES = "AppDevices"

    /*****************用户信息***************/
    const val USER_ID = "UserID"
    const val NICK_NAME = "NickName"
    //头像
    const val AVATAR = "Avatar"
    const val REGION_LIST_CN = "RegionListCN"
    const val REGION_LIST_EN = "RegionListEN"
    const val REGISTER_REGION_LIST_CN = "RegisterRegionListCN"
    const val REGISTER_REGION_LIST_EN = "RegisterRegionListEN"
    const val TIME_ZONE = "Timezone"
    const val CHINESE = "zh"
    const val ENGLISH = "en"


    /***********设备**********/
    const val DEVICE_SIGNATURE = "DeviceSignature"
    const val PRODUCT_ID = "ProductID"
    const val DEVICE_NAME = "DeviceName"
    const val DEVICE_IDS = "DeviceIds"
    const val DEVICE_ID = "DeviceId"
    const val CONN_ID = "ConnId"
    const val TIME_STAMP = "TimeStamp"
    const val DEVICE_TIME_STAMP = "DeviceTimestamp"
    const val ALIAS_NAME = "AliasName"

    const val DEVICE_STATUSES = "DeviceStatuses"
    const val PRODUCT_IDS = "ProductIds"
    const val PRODUCTS = "Products"
    const val DATA_TEMPLATE = "DataTemplate"
    const val PROPERTIES = "properties"

    /***********请求类型标记参数************/
    const val NULL_STR = "null"
    const val IOT_APP_KEY = "请输入从物联网开发平台申请的 App key，正式发布前务必填写"

    /***********运行环境参数************/
    const val DEBUG_FLAG = "debug"
    const val OPEN_SOURCE_TAG = "com.tencent.iot.explorer.link.opensource"
    const val PUBLISH_TAG = "com.tencent.iot.explorer.link"

    /***********窗口跳转码************/
    const val QR_CODE_REQUEST_CODE = 103
    const val LOGIN_REQUEST_CODE = 108
    const val H5_REQUEST_LOGIN_CODE = 10

    const val SSID_UNKNOWN = "unknown ssid"
    const val EXTRA_TITLE = "title"
    const val EXTRA_TEXT = "text"
    const val POLICY_PREFIX = "https://iot.cloud.tencent.com/explorer-h5/about-policy/"
    const val PRIVACY_POLICY_SUFFIX = "#?type=privacy"
    const val SERVICE_POLICY_SUFFIX = "#?type=service"
    const val CANCEL_POLICY_SUFFIX = "#?type=cancel"

}