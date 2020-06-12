package com.tencent.iot.explorer.link.core.link.service

import android.content.Context
import com.tencent.iot.explorer.link.core.link.entity.DeviceTask
import com.tencent.iot.explorer.link.core.auth.util.Weak

abstract class DeviceService(context: Context, task: DeviceTask) {

    internal var context by Weak {
        context.applicationContext
    }

    internal var mTask = task

    internal abstract fun start()

    internal abstract fun stop()

}