package com.tencent.iot.explorer.link.core.demo.activity

import android.content.Intent
import android.net.Uri
import android.text.TextUtils
import android.widget.MediaController
import com.tencent.iot.explorer.link.core.demo.R
import kotlinx.android.synthetic.main.activity_ipc.*

class IPCActivity : BaseActivity() {
    var url = ""

    override fun getContentView(): Int {
        return R.layout.activity_ipc
    }

    override fun initView() {
        url = intent.getStringExtra(URL)
        if (!TextUtils.isEmpty(url)) {
//            url = "http://devimages.apple.com/iphone/samples/bipbop/gear1/prog_index.m3u8"
            playVideo(url)
        }

        palyback_info.setText("xxxxxxxxxxx")
    }

    override fun setListener() {
        test_btn.setOnClickListener {
            var intent = Intent(this@IPCActivity, DateIPCActivity::class.java)
            intent.putExtra(IPCActivity.URL, "")
            startActivity(intent)
        }
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

    companion object {
        const val URL = "extraUrl"
    }

}
