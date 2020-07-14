package com.tencent.iot.explorer.link.core.link.entity

import android.location.Location

class SoftApTask {
    var mSsid = ""
    var mBssid = ""
    var mPassword = ""
    var mAccessToken = ""
    var mLocation: Location? = null
    var mTimeoutSecond = 60
}