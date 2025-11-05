package com.tencent.iot.explorer.link.demo.video

import android.app.AlertDialog
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

    private fun showConnectionTypeDialog() {
        val options = arrayOf("单设备直连", "多设备直连")
        AlertDialog.Builder(this)
            .setTitle("选择连接方式")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> jumpActivity(VideoTestInputActivity::class.java)
                    1 -> jumpActivity(MultiVideoTestInputActivity::class.java)
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }
}