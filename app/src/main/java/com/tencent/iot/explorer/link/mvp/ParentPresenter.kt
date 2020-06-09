package com.mvp

/**
 * Created by lurs on 2018/3/23 0023.
 */
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