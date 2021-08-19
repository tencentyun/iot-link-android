package com.tencent.iot.explorer.link.demo

import android.os.Bundle
import com.tencent.iot.explorer.link.demo.common.util.StatusBarUtil

abstract class VideoBaseActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkStyle()
    }

    private fun checkStyle() {
        StatusBarUtil.setRootViewFitsSystemWindows(this, false)
        StatusBarUtil.setTranslucentStatus(this)
        if (!StatusBarUtil.setStatusBarDarkTheme(this, true)) {
            StatusBarUtil.setStatusBarColor(this, 0x55000000)
        }
    }
}