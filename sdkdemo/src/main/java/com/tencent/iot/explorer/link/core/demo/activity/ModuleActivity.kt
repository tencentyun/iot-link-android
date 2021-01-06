package com.tencent.iot.explorer.link.core.demo.activity

import android.content.Intent
import com.tencent.iot.explorer.link.core.demo.R
import kotlinx.android.synthetic.main.activity_module.*

class ModuleActivity : BaseActivity() {

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
            jumpActivity(VedioModuleActivity::class.java)
        }

        btn_3.setOnClickListener {
//            jumpActivity()
        }
    }

}
