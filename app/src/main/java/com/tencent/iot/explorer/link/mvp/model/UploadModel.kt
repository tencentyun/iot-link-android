package com.tencent.iot.explorer.link.mvp.model

import android.content.Context
import android.text.TextUtils
import com.alibaba.fastjson.JSONObject
import com.tencent.iot.explorer.link.kitlink.response.AppCosAuthResponse
import com.tencent.iot.explorer.link.kitlink.response.BaseResponse
import com.tencent.iot.explorer.link.kitlink.util.HttpRequest
import com.tencent.iot.explorer.link.kitlink.util.MyCallback
import com.tencent.iot.explorer.link.mvp.view.UploadView
import com.tencent.cos.xml.CosXmlServiceConfig
import com.tencent.cos.xml.CosXmlSimpleService
import com.tencent.cos.xml.exception.CosXmlClientException
import com.tencent.cos.xml.exception.CosXmlServiceException
import com.tencent.cos.xml.listener.CosXmlResultListener
import com.tencent.cos.xml.model.CosXmlRequest
import com.tencent.cos.xml.model.CosXmlResult
import com.tencent.cos.xml.transfer.COSXMLUploadTask
import com.tencent.cos.xml.transfer.TransferConfig
import com.tencent.cos.xml.transfer.TransferManager
import com.tencent.qcloud.core.auth.QCloudCredentialProvider
import com.tencent.qcloud.core.auth.QCloudCredentials
import com.tencent.qcloud.core.auth.SessionQCloudCredentials
import com.tencent.iot.explorer.link.util.L

/**
 * 上传文件
 */
class UploadModel(view: UploadView) {

    private val view = view
    private var region = ""
    //本地文件的绝对路径  如 srcPath=Environment.getExternalStorageDirectory().getPath() + "/text.txt";
    private var bucket = ""
    private var path = ""
    //即对象到 COS 上的绝对路径, 格式如 cosPath = "text.txt"
    private var cosPath = ""
    //若存在初始化分片上传的 UploadId，则赋值对应 uploadId 值用于续传，否则，赋值 null。
    private var uploadId: String? = null
    private var credentials: JSONObject? = null
    private var context: Context? = null

    private val uploadList = arrayListOf<UploadEntity>()

    fun uploadSingleFile(context: Context, srcPath: String) {
        this.context = context
        uploadList.add(UploadEntity(srcPath))
        if (TextUtils.isEmpty(bucket))
            appCosAuth()
        else
            unload()
    }

    fun uploadMultiFile(context: Context, srcPathList: List<String>) {
        this.context = context
        srcPathList.forEachIndexed { _, path ->
            uploadList.add(UploadEntity(path))
        }
        if (TextUtils.isEmpty(bucket))
            appCosAuth()
        else
            unload()
    }

    /**
     * 上传图片第一步：获取签名
     */
    private fun appCosAuth() {
        HttpRequest.instance.appCosAuth(object : MyCallback {
            override fun fail(msg: String?, reqCode: Int) {
                L.e("获取签名${msg ?: ""}")
            }

            override fun success(response: BaseResponse, reqCode: Int) {
                if (response.isSuccess()) {
                    response.parse(AppCosAuthResponse::class.java)?.run {
                        this@UploadModel.credentials = credentials
                        cosConfig?.run {
                            this@UploadModel.region = region
                            this@UploadModel.bucket = bucket
                            this@UploadModel.path = path
                            unload()
                        }
                    }
                }
            }
        })
    }

    /**
     * 上传图片第二步：上传
     */
    private fun unload() {
        val config = CosXmlServiceConfig.Builder()
            .setRegion(region)
            .isHttps(true)
            .setDebuggable(true)
            .builder()
        val provider = CredentialProvider(credentials!!)
        val cosXmlService = CosXmlSimpleService(context, config, provider)
        // 初始化 TransferConfig
        val transferConfig = TransferConfig.Builder().build()
        //初始化 TransferManager
        val transferManager = TransferManager(cosXmlService, transferConfig)
        uploadList.forEachIndexed { _, entity ->
            val srcPath = entity.path
            cosPath = path + srcPath.substring(srcPath.lastIndexOf("/") + 1)
            //上传对象
            transferManager.upload(bucket, cosPath, srcPath, uploadId).let {
                entity.task = it
                L.e("开始上传：$srcPath")
                it.setCosXmlResultListener(object : CosXmlResultListener {
                    override fun onSuccess(request: CosXmlRequest?, result: CosXmlResult?) {
                        L.e("上传成功")
                        uploadList.remove(entity)
                        if (result != null)
                            onSuccess(srcPath, result.accessUrl)
                    }

                    override fun onFail(
                        request: CosXmlRequest?,
                        exception: CosXmlClientException?,
                        serviceException: CosXmlServiceException?
                    ) {
                        view.uploadFail(entity.path, exception)
                        onFail(entity)
                    }
                })
            }
        }
    }


    /**
     * 文件上传成功
     */
    @Synchronized
    private fun onSuccess(path: String, url: String) {
        view.uploadSuccess(path, url, uploadList.size <= 0)
    }

    /**
     * 文件上传失败
     */
    private fun onFail(entity: UploadEntity) {
        uploadList.remove(entity)
    }

    internal class UploadEntity(path: String) {
        var path = path
        var url = ""
        var task: COSXMLUploadTask? = null
    }

    /**
     * 自定义临时签名
     */
    internal class CredentialProvider(credentials: JSONObject) : QCloudCredentialProvider {
        private var secretId: String = ""
        private var secretKey: String = ""
        private var token: String = ""
        private var startTime: Long
        private var expiredTime: Long

        init {
            secretId = credentials.getString("tmpSecretId")
            secretKey = credentials.getString("tmpSecretKey")
            startTime = credentials.getLong("startTime")
            expiredTime = credentials.getLong("expiredTime")
            token = credentials.getString("sessionToken")
        }

        override fun getCredentials(): QCloudCredentials {
            return SessionQCloudCredentials(secretId, secretKey, token, startTime, expiredTime)
        }

        override fun refresh() {}
    }
}
