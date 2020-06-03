package com.kitlink.util

import android.view.View

/**
 * 操作相机事件的接口
 */
interface OnCameraEventListener {

    // 相机窗口被打开
    fun onCameraOpened()

    /**
     * 拍照成功
     * view: 打开相机窗口的对象
     * path: 相机拍照保存的临时路径
     */
    fun onCaptureSuccess(view: View, path: String)

    // 相机窗口已关闭
    fun onCameraClosed()
}