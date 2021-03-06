package com.tencent.iot.explorer.link.demo.core.activity

import com.tencent.iot.explorer.link.demo.BaseActivity
import com.tencent.iot.explorer.link.demo.R
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

        tv_ap_config_net.setOnClickListener {
            jumpActivity(ApConfigNetActivity::class.java)
        }

        tv_wired_config_net.setOnClickListener {
            jumpActivity(WiredConfigNetActivity::class.java)
        }
    }

}
