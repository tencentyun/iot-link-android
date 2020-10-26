package com.tencent.iot.explorer.link.kitlink.fragment

import android.view.View
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.mvp.IPresenter

/**
 * 智能日志页面
 */
class SmartLogFragment() : BaseFragment() {


    override fun getPresenter(): IPresenter? {
        return null
    }

    override fun getContentView(): Int {
        return R.layout.fragment_smart_log
    }

    override fun startHere(view: View) {

    }

}