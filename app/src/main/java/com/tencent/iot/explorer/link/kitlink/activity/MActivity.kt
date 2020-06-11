package com.tencent.iot.explorer.link.kitlink.activity

import com.tencent.iot.explorer.link.mvp.IModel

abstract class MActivity : BaseActivity() {

    abstract fun getModel(): IModel?

    override fun onDestroy() {
        super.onDestroy()
        getModel()?.onDestroy()
    }

}