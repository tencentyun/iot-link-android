package com.tenext.link.service

import android.content.Context
import com.tenext.link.entity.DeviceTask
import com.tenext.auth.util.Weak

abstract class DeviceService(context: Context, task: DeviceTask) {

    internal var context by Weak {
        context.applicationContext
    }

    internal var mTask = task

    internal abstract fun start()

    internal abstract fun stop()

}