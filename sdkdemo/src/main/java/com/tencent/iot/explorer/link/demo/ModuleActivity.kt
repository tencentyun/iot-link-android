package com.tencent.iot.explorer.link.demo

import android.Manifest
import android.os.Bundle
import com.tencent.iot.explorer.link.demo.common.util.LogcatHelper
import com.tencent.iot.explorer.link.demo.core.activity.LoginActivity
import com.tencent.iot.explorer.link.demo.video.VideoOptionsActivity
import kotlinx.android.synthetic.main.activity_module.*

class ModuleActivity : BaseActivity() {

    private var permissions = arrayOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.CAMERA
    )

    override fun getContentView(): Int {
        return R.layout.activity_module
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LogcatHelper.getInstance(this).start()
    }

    override fun onDestroy() {
        super.onDestroy()
        LogcatHelper.getInstance(this).stop()
    }

    override fun initView() {}

    override fun setListener() {
        btn_1.setOnClickListener {
            jumpActivity(LoginActivity::class.java)
        }

        btn_2.setOnClickListener {
            if (checkPermissions(permissions)) {
                jumpActivity(VideoOptionsActivity::class.java)
            } else {
                requestPermission(permissions)
            }
        }

        btn_3.setOnClickListener {}
    }

    override fun permissionAllGranted() {
        jumpActivity(VideoOptionsActivity::class.java)
    }

}
