package com.tencent.iot.explorer.link.demo.common.mvp

interface IModel {
    fun onCreate()
    fun onDestroy()
    fun onPause()
    fun onResume()
    fun onStop()
}