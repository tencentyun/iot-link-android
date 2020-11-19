package com.tencent.iot.explorer.link.core.demo.trtc

import android.Manifest
import android.content.Intent
import com.tencent.iot.explorer.link.core.demo.R
import com.tencent.iot.explorer.link.core.demo.activity.BaseActivity
import com.tencent.liteav.trtccalling.model.TRTCCalling
import com.tencent.liteav.trtccalling.ui.TRTCCallingEntranceActivity
import kotlinx.android.synthetic.main.activity_t_r_t_c_main.*
import kotlinx.android.synthetic.main.menu_back_layout.*

class TRTCMainActivity : BaseActivity() {

    private var permissions = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.CAMERA
    )

    private var type = 0

    override fun getContentView(): Int {
        return R.layout.activity_t_r_t_c_main
    }

    override fun initView() {
        tv_title.text = "IoT&TRTC"
    }

    override fun setListener() {
        iv_back.setOnClickListener {
            CallService.stop(this@TRTCMainActivity)
            finish()
        }
        tv_video_call.setOnClickListener {
            request(0)
        }
        tv_audio_call.setOnClickListener {
            request(1)
        }
    }


    override fun permissionAllGranted() {
        jump()
    }

    override fun permissionDenied(permission: String) {
        requestPermission(arrayOf(permission))
    }

    private fun request(type: Int) {
        this.type = type
        if (checkPermissions(permissions)) {
            jump()
        } else {
            requestPermission(permissions)
        }
    }

    /**
     * 跳转
     */
    private fun jump() {
        when (type) {
            0 -> {
                val intent = Intent(this, TRTCCallingEntranceActivity::class.java)
                intent.putExtra("TITLE", "视频通话")
                intent.putExtra("TYPE", TRTCCalling.TYPE_VIDEO_CALL)
                startActivity(intent)
            }
            1 -> {
                val intent = Intent(this, TRTCCallingEntranceActivity::class.java)
                intent.putExtra("TITLE", "语音通话")
                intent.putExtra("TYPE", TRTCCalling.TYPE_AUDIO_CALL)
                startActivity(intent)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        CallService.start(this)
    }
}