package com.mvp

/**
 * Created by lurs on 2018/3/23 0023.
 */
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
