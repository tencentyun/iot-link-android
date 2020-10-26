package com.tencent.iot.explorer.link.kitlink.fragment

import android.view.View
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.mvp.IPresenter

/**
 * 我的智能
 */
class MySmartFragment() : BaseFragment() {

    override fun getPresenter(): IPresenter? {
        return null
    }

    override fun getContentView(): Int {
        return R.layout.fragment_my_smart
    }

    override fun startHere(view: View) {

    }


}