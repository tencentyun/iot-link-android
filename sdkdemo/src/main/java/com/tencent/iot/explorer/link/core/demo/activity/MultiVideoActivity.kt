package com.tencent.iot.explorer.link.core.demo.activity

import com.tencent.iot.explorer.link.core.demo.R
import com.tencent.iot.explorer.link.core.demo.log.L
import com.tencent.iot.video.link.consts.VideoConst


class MultiVideoActivity : BaseActivity() {

    private lateinit var secretId: String
    private lateinit var secretKey: String
    private lateinit var productId: String
    private lateinit var deviceName01: String
    private lateinit var deviceName02: String


    override fun getContentView(): Int {
        return R.layout.activity_multi_video
    }

    override fun initView() {
        val bundle = this.intent.extras
        secretId = bundle?.get(VideoConst.MULTI_VIDEO_SECRET_ID) as String
        secretKey = bundle.get(VideoConst.MULTI_VIDEO_SECRET_KEY) as String
        productId = bundle.get(VideoConst.MULTI_VIDEO_PROD_ID) as String
        deviceName01 = bundle.get(VideoConst.MULTI_VIDEO_DEVICE_NAME01) as String
        deviceName02 = bundle.get(VideoConst.MULTI_VIDEO_DEVICE_NAME02) as String

        L.e("$secretId, $secretKey, $productId, $deviceName01, $deviceName02")
    }

    override fun setListener() {
    }

}