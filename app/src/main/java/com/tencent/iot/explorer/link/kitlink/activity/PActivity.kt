package com.tencent.iot.explorer.link.kitlink.activity

import com.tencent.iot.explorer.link.mvp.IPresenter

/**
 * baseActivity
 */
abstract class PActivity : BaseActivity() {

    abstract fun getPresenter(): IPresenter?

    override fun onDestroy() {
        super.onDestroy()
        getPresenter()?.onDestroy()
    }

}
