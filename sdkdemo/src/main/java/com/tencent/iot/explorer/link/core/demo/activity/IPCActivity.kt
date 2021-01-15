package com.tencent.iot.explorer.link.core.demo.activity

import android.net.Uri
import android.widget.MediaController
import com.tencent.iot.explorer.link.core.demo.R
import kotlinx.android.synthetic.main.activity_ipc.*

class IPCActivity : BaseActivity() {

    override fun getContentView(): Int {
        return R.layout.activity_ipc
    }

    override fun initView() {
    }

    override fun setListener() {
        test_btn.setOnClickListener {}
    }

    private fun playVideo(url: String) {
        val uri: Uri = Uri.parse(url)
        vedio_player.setVideoURI(uri)
        var mediaController = MediaController(this)
        vedio_player.setMediaController(mediaController)
        vedio_player.start()
    }

    private fun stopPlay() {
        vedio_player.stopPlayback()
    }

}
