package com.kitlink.activity

import com.kitlink.R
import kotlinx.android.synthetic.main.menu_back_layout.*

/**
 * 设备信息
 */
class DeviceInfoActivity : BaseActivity() {

    override fun getContentView(): Int {
        return R.layout.activity_device_info
    }

    override fun initView() {
        tv_title.text = getString(R.string.device_info)
    }

    override fun setListener() {
        iv_back.setOnClickListener { finish() }
    }
}
