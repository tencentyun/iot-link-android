package com.tencent.iot.explorer.link.kitlink.activity

import com.tencent.iot.explorer.link.R
import kotlinx.android.synthetic.main.menu_back_layout.*


class AddAutoicTaskActivity : BaseActivity() {

    override fun getContentView(): Int {
        return R.layout.activity_add_autoic_task
    }

    override fun initView() {
        tv_title.setText(R.string.add_automic_smart)
    }


    override fun setListener() {
        iv_back.setOnClickListener { finish() }
    }
}