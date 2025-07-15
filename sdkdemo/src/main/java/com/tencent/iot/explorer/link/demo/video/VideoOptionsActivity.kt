package com.tencent.iot.explorer.link.demo.video

import com.tencent.iot.explorer.link.demo.R
import com.tencent.iot.explorer.link.demo.VideoBaseActivity
import com.tencent.iot.explorer.link.demo.databinding.ActivityVideoOptionsBinding

class VideoOptionsActivity : VideoBaseActivity<ActivityVideoOptionsBinding>() {
    override fun getViewBinding(): ActivityVideoOptionsBinding = ActivityVideoOptionsBinding.inflate(layoutInflater)

    override fun initView() {}

    override fun setListener() {
        with(binding) {
            btnVideo.setOnClickListener { jumpActivity(VideoInputAuthorizeActivity::class.java) }
            btnVideoWlan.setOnClickListener { jumpActivity(VideoWlanDetectActivity::class.java) }
            btnVideoLink.setOnClickListener { jumpActivity(VideoTestInputActivity::class.java) }
        }
    }

}