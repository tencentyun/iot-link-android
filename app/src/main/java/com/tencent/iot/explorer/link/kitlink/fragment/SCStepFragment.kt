package com.tencent.iot.explorer.link.kitlink.fragment

import android.view.View
import com.tencent.iot.explorer.link.kitlink.R
import com.tencent.iot.explorer.link.mvp.IPresenter
import kotlinx.android.synthetic.main.smart_config_first.*

/**
 * SmartConfig步骤展示
 */
class SCStepFragment : BaseFragment() {

    var onNextListener: OnNextListener? = null

    override fun getPresenter(): IPresenter? {
        return null
    }

    override fun getContentView(): Int {
        return R.layout.fragment_smart_config_step
    }

    override fun startHere(view: View) {

        tv_smart_first_commit.setOnClickListener { onNextListener?.onNext() }
    }

    interface OnNextListener {
        fun onNext()
    }

}