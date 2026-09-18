package com.tencent.iot.explorer.link.demo.video

import android.Manifest
import com.tencent.iot.explorer.link.demo.VideoBaseActivity
import com.tencent.iot.explorer.link.demo.databinding.ActivityVideoOptionsBinding

class VideoOptionsActivity : VideoBaseActivity<ActivityVideoOptionsBinding>() {

    private var permissions = arrayOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.CAMERA
    )

    override fun getViewBinding(): ActivityVideoOptionsBinding = ActivityVideoOptionsBinding.inflate(layoutInflater)

    override fun initView() {
        if (!checkPermissions(permissions)) {
            requestPermission(permissions)
        }
    }

    override fun setListener() {
        with(binding) {
            btnVideo.setOnClickListener { jumpActivity(VideoInputAuthorizeActivity::class.java) }
            btnVideoWlan.setOnClickListener { jumpActivity(VideoWlanDetectActivity::class.java) }
            btnVideoLink.setOnClickListener { jumpActivity(VideoTestInputActivity::class.java) }
        }
    }
}