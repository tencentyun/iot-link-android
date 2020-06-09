package com.kitlink.activity

import com.mvp.IModel

abstract class MActivity : BaseActivity() {

    abstract fun getModel(): IModel?

    override fun onDestroy() {
        super.onDestroy()
        getModel()?.onDestroy()
    }

}