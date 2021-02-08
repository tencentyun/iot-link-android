package com.tencent.iot.explorer.link.core.demo.activity

import android.Manifest
import android.content.Intent
import com.tencent.iot.explorer.link.core.demo.R
import com.tencent.iot.explorer.link.core.demo.util.LogcatHelper
import kotlinx.android.synthetic.main.activity_module.*

class ModuleActivity : BaseActivity() {

    private var permissions = arrayOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )

    override fun getContentView(): Int {
        return R.layout.activity_module
    }

    override fun initView() {
    }

    override fun setListener() {
        btn_1.setOnClickListener {
            jumpActivity(LoginActivity::class.java)
        }

        btn_2.setOnClickListener {
            if (checkPermissions(permissions)) {
                jumpActivity(InputAuthorizeActivity::class.java)
            } else {
                requestPermission(permissions)
            }
        }

        btn_3.setOnClickListener {
//            jumpActivity()
        }
    }

    override fun permissionAllGranted() {
        jumpActivity(InputAuthorizeActivity::class.java)
    }

}
