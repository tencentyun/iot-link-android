package com.tencent.iot.explorer.link.demo

import android.Manifest
import android.os.Bundle
import com.tencent.iot.explorer.link.demo.common.util.LogcatHelper
import com.tencent.iot.explorer.link.demo.core.activity.LoginActivity
import com.tencent.iot.explorer.link.demo.databinding.ActivityModuleBinding
import com.tencent.iot.explorer.link.demo.video.VideoOptionsActivity

class ModuleActivity : BaseActivity<ActivityModuleBinding>() {

    private var permissions = arrayOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.CAMERA
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LogcatHelper.getInstance(this).start()
    }

    override fun onDestroy() {
        super.onDestroy()
        LogcatHelper.getInstance(this).stop()
    }

    override fun getViewBinding(): ActivityModuleBinding = ActivityModuleBinding.inflate(layoutInflater)

    override fun initView() {}

    override fun setListener() {
        with(binding) {
            btn1.setOnClickListener {
                jumpActivity(LoginActivity::class.java)
            }

            btn2.setOnClickListener {
                if (checkPermissions(permissions)) {
                    jumpActivity(VideoOptionsActivity::class.java)
                } else {
                    requestPermission(permissions)
                }
            }

            btn3.setOnClickListener {}
        }
    }

    override fun permissionAllGranted() {
        jumpActivity(VideoOptionsActivity::class.java)
    }

}
