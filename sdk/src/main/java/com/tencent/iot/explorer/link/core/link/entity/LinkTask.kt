package com.tencent.iot.explorer.link.core.link.entity

import android.location.Location

open class LinkTask {
    var mSsid: String = ""
    var mBssid: String = ""
    var mPassword: String = ""
    var mAccessToken: String = ""
    var mLocation: Location? = null
    var mTimeoutSecond: Int = 10
    var mRegion: String = ""
}