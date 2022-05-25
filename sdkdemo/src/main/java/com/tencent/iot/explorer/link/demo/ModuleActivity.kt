package com.tencent.iot.explorer.link.demo

import android.Manifest
import android.widget.Toast
import com.tencent.iot.explorer.link.demo.core.activity.LoginActivity
import com.tencent.iot.explorer.link.demo.video.VideoOptionsActivity
import kotlinx.android.synthetic.main.activity_module.*

class ModuleActivity : BaseActivity() {

    private var permissions = arrayOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )

    override fun getContentView(): Int {
        return R.layout.activity_module
    }

    override fun initView() {}

    override fun setListener() {
        btn_1.setOnClickListener {
            Toast.makeText(this, "2.4.22sdk合规仅检测video部分", Toast.LENGTH_LONG).show()
//            jumpActivity(LoginActivity::class.java)
        }

        btn_2.setOnClickListener {
            if (checkPermissions(permissions)) {
                jumpActivity(VideoOptionsActivity::class.java)
            } else {
                requestPermission(permissions)
            }
        }

        btn_3.setOnClickListener {
            Toast.makeText(this, "2.4.22sdk合规仅检测video部分", Toast.LENGTH_LONG).show()
        }
    }

    override fun permissionAllGranted() {
        jumpActivity(VideoOptionsActivity::class.java)
    }

}
