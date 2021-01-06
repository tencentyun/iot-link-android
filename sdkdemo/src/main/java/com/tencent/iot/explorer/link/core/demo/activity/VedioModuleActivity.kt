package com.tencent.iot.explorer.link.core.demo.activity

import android.content.Intent
import com.tencent.iot.explorer.link.core.demo.R
import kotlinx.android.synthetic.main.activity_module.*
import kotlinx.android.synthetic.main.activity_vedio_module.*

class VedioModuleActivity : BaseActivity() {

    override fun getContentView(): Int {
        return R.layout.activity_vedio_module
    }

    override fun initView() {
    }

    override fun setListener() {
        btn_1_vedio.setOnClickListener {
            jumpActivity(ConfigNetActivity::class.java)
        }

        btn_2_vedio.setOnClickListener {
            var intent = Intent(this@VedioModuleActivity, IPCActivity::class.java)
            intent.putExtra(IPCActivity.URL, "")
            startActivity(intent)
        }

        btn_3_vedio.setOnClickListener {
//            jumpActivity()
        }
    }

}
