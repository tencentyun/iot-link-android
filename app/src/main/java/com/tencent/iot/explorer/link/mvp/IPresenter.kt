package com.tencent.iot.explorer.link.mvp

interface IPresenter {
    fun onCreate()
    fun onDestroy()
    fun onPause()
    fun onResume()
    fun onStop()
}