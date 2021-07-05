package com.tencent.iot.explorer.link.demo.common.mvp

abstract class ParentPresenter<M : IModel, V : IView> : IPresenter {

    var model: M? = null

    constructor(view: V) {
        model = getIModel(view)
    }

    abstract fun getIModel(view: V): M

    override fun onCreate() {
        model?.onCreate()
    }

    override fun onDestroy() {
        model?.onDestroy()
        model = null
    }

    override fun onPause() {
        model?.onPause()
    }

    override fun onResume() {
        model?.onResume()
    }

    override fun onStop() {
        model?.onStop()
    }
}