package com.tencent.iot.explorer.link.kitlink.device.softap

import android.location.Location

class SoftApTask {
    var mSsid = ""
    var mBssid = ""
    var mPassword = ""
    var mAccessToken = ""
    var mLocation: Location? = null
    var mTimeoutSecond = 60
}