package com.tencent.iot.explorer.link.core.link.entity

import android.location.Location

class SmartConfigTask {

    var mSsid: String = ""
    var mBssid: String = ""
    var mPassword: String = ""
    var mAccessToken: String = ""
    var mLocation: Location? = null
    var mTimeoutSecond: Int = 10

}