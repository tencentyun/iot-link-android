package com.tencent.iot.explorer.link.demo.common.mvp

open class ParentModel<V : IView>(view: V?) : IModel {

    var view: V? = null

    init {
        this.view = view
    }
    
    override fun onCreate() {
    }

    override fun onDestroy() {
        view = null
    }

    override fun onPause() {
    }

    override fun onResume() {
    }

    override fun onStop() {
    }
}
