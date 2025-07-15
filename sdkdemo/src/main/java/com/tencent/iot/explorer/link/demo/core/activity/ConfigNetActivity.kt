package com.tencent.iot.explorer.link.demo.core.activity

import com.tencent.iot.explorer.link.demo.BaseActivity
import com.tencent.iot.explorer.link.demo.R
import com.tencent.iot.explorer.link.demo.databinding.ActivityConfigNetTypeBinding

class ConfigNetActivity : BaseActivity<ActivityConfigNetTypeBinding>() {

    override fun getViewBinding(): ActivityConfigNetTypeBinding = ActivityConfigNetTypeBinding.inflate(layoutInflater)

    override fun initView() {

    }

    override fun setListener() {
        with(binding) {
            tvQrcodeConfigNet.setOnClickListener{
                jumpActivity(QrcodeConfigNetActivity::class.java)
            }

            tvApConfigNet.setOnClickListener {
                jumpActivity(ApConfigNetActivity::class.java)
            }

            tvWiredConfigNet.setOnClickListener {
                jumpActivity(WiredConfigNetActivity::class.java)
            }
        }
    }

}
