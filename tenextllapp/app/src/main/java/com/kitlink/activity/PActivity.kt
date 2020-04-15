package com.kitlink.activity

import com.mvp.IPresenter

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
