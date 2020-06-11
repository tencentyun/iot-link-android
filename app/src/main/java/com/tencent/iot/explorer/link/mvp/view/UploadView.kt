package com.tencent.iot.explorer.link.mvp.view

import com.tencent.cos.xml.exception.CosXmlClientException

interface UploadView {

    fun successAll()

    fun uploadSuccess(loadPath: String, fileUrl: String, all: Boolean)

    fun uploadFail(loadPath: String, exception: CosXmlClientException?)

}