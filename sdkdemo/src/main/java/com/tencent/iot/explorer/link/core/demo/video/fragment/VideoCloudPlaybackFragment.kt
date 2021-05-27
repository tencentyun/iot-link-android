package com.tencent.iot.explorer.link.core.demo.video.fragment

import android.view.View
import com.tencent.iot.explorer.link.core.demo.R
import com.tencent.iot.explorer.link.core.demo.fragment.BaseFragment
import kotlinx.android.synthetic.main.fragment_video_cloud_playback.*

class VideoCloudPlaybackFragment : BaseFragment() {

    override fun getContentView(): Int {
        return R.layout.fragment_video_cloud_playback
    }

    override fun startHere(view: View) {
        tv_text.text = System.currentTimeMillis().toString()
    }

    override fun onResume() {
        super.onResume()
    }

    private fun setListener() {
    }

}