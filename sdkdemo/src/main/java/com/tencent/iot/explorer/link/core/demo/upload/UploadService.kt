package com.tencent.iot.explorer.link.core.demo.upload

import android.content.Context
import android.text.TextUtils
import com.alibaba.fastjson.JSONObject
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
import com.tencent.iot.explorer.link.core.auth.callback.MyCallback
import com.tencent.iot.explorer.link.core.auth.response.AppCosAuthResponse
import com.tencent.iot.explorer.link.core.auth.response.BaseResponse
import com.tencent.iot.explorer.link.core.auth.service.BaseService
import com.tencent.iot.explorer.link.core.auth.util.JsonManager
import com.tencent.iot.explorer.link.core.demo.log.L

internal class UploadService : BaseService(), UploadImpl {

    private val app_cos_auth = 5001

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

    /**
     * 上传单张图片
     */
    override fun uploadSingleFile(context: Context, srcPath: String, callback: UploadCallback) {
        this.context = context
        uploadList.add(UploadEntity(srcPath))
        if (TextUtils.isEmpty(bucket))
            appCosAuth(callback)
        else
            unload(callback)
    }

    /**
     * 上传多张图片
     */
    override fun uploadMultiFile(
        context: Context,
        srcPathList: List<String>,
        callback: UploadCallback
    ) {
        this.context = context
        srcPathList.forEachIndexed { _, path ->
            uploadList.add(UploadEntity(path))
        }
        if (TextUtils.isEmpty(bucket))
            appCosAuth(callback)
        else
            unload(callback)
    }

    /**
     * 上传图片第一步获取签名
     */
    private fun appCosAuth(callback: UploadCallback) {
        val param = tokenParams("AppCosAuth")
        param["path"] = "iotexplorer-app-logs/user_{uin}/"
        tokenAppCosAuth(param, object : MyCallback {
            override fun fail(msg: String?, reqCode: Int) {
                L.d("获取签名失败:${msg ?: ""}")
            }

            override fun success(response: BaseResponse, reqCode: Int) {
                if (response.isSuccess()) {
                    response.parse(AppCosAuthResponse::class.java)?.run {
                        this@UploadService.credentials = credentials
                        cosConfig?.run {
                            this@UploadService.region = region
                            this@UploadService.bucket = bucket
                            this@UploadService.path = path
                            unload(callback)
                            return
                        }
                    }
                }
                L.d("获取签名返回:${JsonManager.toJson(response)}")
            }
        }, app_cos_auth) //上传图片
    }

    /**
     * 上传图片第二步：上传
     */
    private fun unload(callback: UploadCallback) {
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
                L.d("开始上传：$srcPath")
                it.setCosXmlResultListener(object : CosXmlResultListener {
                    override fun onSuccess(request: CosXmlRequest, result: CosXmlResult) {
                        L.d("上传成功")
                        entity.url = result.accessUrl
                        onSuccess(entity, callback)
                    }

                    override fun onFail(
                        request: CosXmlRequest?,
                        exception: CosXmlClientException?,
                        serviceException: CosXmlServiceException?
                    ) {
                        onFail(entity, callback)
                    }
                })
            }
        }
    }


    /**
     * 文件上传成功
     */
    @Synchronized
    private fun onSuccess(entity: UploadEntity, callback: UploadCallback) {
        uploadList.remove(entity)
        callback.onSuccess(entity.url, entity.path, uploadList.isEmpty())
    }

    /**
     * 文件上传失败
     */
    @Synchronized
    private fun onFail(entity: UploadEntity, callback: UploadCallback) {
        uploadList.remove(entity)
        callback.onFail(entity.path, uploadList.isEmpty())
    }

    /**
     * 自定义上传对象
     */
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