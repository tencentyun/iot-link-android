package com.tencent.iot.explorer.link.core.demo.mvp

interface IModel {
    fun onCreate()
    fun onDestroy()
    fun onPause()
    fun onResume()
    fun onStop()
}