package com.tencent.iot.explorer.link.kitlink.fragment

import android.view.View
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.mvp.IPresenter
import kotlinx.android.synthetic.main.fragment_soft_hotspot.*

class SoftHotspotFragment : BaseFragment() {

    var onNextListener: OnNextListener? = null

    override fun getPresenter(): IPresenter? {
        return null
    }

    override fun getContentView(): Int {
        return R.layout.fragment_soft_hotspot
    }

    override fun startHere(view: View) {
        tv_soft_connect_hotspot.setOnClickListener {
            if (onNextListener != null) {
                onNextListener?.onNext()
            }
        }
    }

    interface OnNextListener {
        fun onNext()
    }
}