package com.tencent.iot.explorer.link.core.demo.upload

import android.content.Context

interface UploadImpl {

    /**
     * 上传单张图片
     */
    fun uploadSingleFile(context: Context, srcPath: String, callback: UploadCallback)

    /**
     * 上传多张图片
     */
    fun uploadMultiFile(context: Context, srcPathList: List<String>, callback: UploadCallback)

}