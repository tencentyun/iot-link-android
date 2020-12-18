package com.tencent.iot.explorer.link.core.demo.activity

import com.tencent.iot.explorer.link.core.demo.R
import kotlinx.android.synthetic.main.activity_config_net_type.*

class ConfigNetActivity : BaseActivity() {

    override fun getContentView(): Int {
        return R.layout.activity_config_net_type
    }

    override fun initView() {

    }

    override fun setListener() {
        tv_qrcode_config_net.setOnClickListener{
            jumpActivity(QrcodeConfigNetActivity::class.java)
        }
    }

}
