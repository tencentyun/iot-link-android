package com.tencent.iot.explorer.link.mvp

interface IModel {
    fun onCreate()
    fun onDestroy()
    fun onPause()
    fun onResume()
    fun onStop()
}