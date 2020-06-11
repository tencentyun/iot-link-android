package com.tencent.iot.explorer.link.mvp

/**
 * Created by lurs on 2018/3/23 0023.
 */
interface IPresenter {
    fun onCreate()
    fun onDestroy()
    fun onPause()
    fun onResume()
    fun onStop()
}