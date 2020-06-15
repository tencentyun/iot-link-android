package com.tencent.iot.explorer.link.kitlink.fragment

import android.view.View
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.mvp.IPresenter

class SceneFragment : BaseFragment() {
    override fun getContentView(): Int {
        return R.layout.fragment_scene
    }

    override fun getPresenter(): IPresenter? {
        return null
    }

    override fun startHere(view: View) {
    }
}