package com.mvp

/**
 * Created by lurs on 2018/3/23 0023.
 */
interface IModel {
    fun onCreate()
    fun onDestroy()
    fun onPause()
    fun onResume()
    fun onStop()
}