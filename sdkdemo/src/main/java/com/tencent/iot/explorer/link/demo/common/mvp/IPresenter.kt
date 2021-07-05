package com.tencent.iot.explorer.link.demo.common.mvp

interface IPresenter {
    fun onCreate()
    fun onDestroy()
    fun onPause()
    fun onResume()
    fun onStop()
}