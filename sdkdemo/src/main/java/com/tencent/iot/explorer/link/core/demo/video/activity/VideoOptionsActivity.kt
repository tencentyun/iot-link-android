package com.tencent.iot.explorer.link.core.demo.video.activity

import com.tencent.iot.explorer.link.core.demo.R
import com.tencent.iot.explorer.link.core.demo.activity.BaseActivity
import kotlinx.android.synthetic.main.activity_video_options.*

class VideoOptionsActivity : BaseActivity() {
    override fun getContentView(): Int {
        return R.layout.activity_video_options
    }

    override fun initView() {}

    override fun setListener() {
        btn_video.setOnClickListener { jumpActivity(VideoInputAuthorizeActivity::class.java) }
    }

}