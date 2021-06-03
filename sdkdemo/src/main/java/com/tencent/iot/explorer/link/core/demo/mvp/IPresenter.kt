package com.tencent.iot.explorer.link.core.demo.mvp

interface IPresenter {
    fun onCreate()
    fun onDestroy()
    fun onPause()
    fun onResume()
    fun onStop()
}