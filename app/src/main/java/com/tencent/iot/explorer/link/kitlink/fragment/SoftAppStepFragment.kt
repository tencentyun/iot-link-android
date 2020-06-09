package com.kitlink.fragment

import android.view.View
import com.kitlink.R
import com.mvp.IPresenter
import kotlinx.android.synthetic.main.fragment_soft_ap_step.*

class SoftAppStepFragment : BaseFragment() {

    var onNextListener: OnNextListener? = null

    override fun getPresenter(): IPresenter? {
        return null
    }

    override fun getContentView(): Int {
        return R.layout.fragment_soft_ap_step
    }

    override fun startHere(view: View) {
        tv_soft_first_commit.setOnClickListener {
            onNextListener?.onNext()
        }
    }

    interface OnNextListener {
        fun onNext()
    }
}