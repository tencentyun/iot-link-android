package com.tencent.iot.explorer.link.core.demo.upload

interface UploadCallback {

    /**
     *  上传成功
     *  @param url 图片链接
     *  @param filePath 图片本地路径
     *  @param isOver 图片是否上传完毕
     */
    fun onSuccess(url: String, filePath: String, isOver: Boolean)

    /**
     *  上传失败
     *  @param filePath 图片本地路径
     *  @param isOver 图片是否上传完毕
     */
    fun onFail(filePath: String, isOver: Boolean)

}