package com.kitlink.fragment

import android.view.View
import com.kitlink.R
import com.mvp.IPresenter

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