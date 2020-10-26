package com.tencent.iot.explorer.link.kitlink.activity

import com.tencent.iot.explorer.link.R
import kotlinx.android.synthetic.main.menu_back_layout.*


class AddManualTaskActivity : BaseActivity() {

    override fun getContentView(): Int {
        return R.layout.activity_web
    }

    override fun initView() {
        tv_title.setText(R.string.add_manual_smart)
    }


    override fun setListener() {
        iv_back.setOnClickListener { finish() }
    }
}