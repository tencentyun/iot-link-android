package com.tencent.iot.explorer.link.kitlink.activity

import com.tencent.iot.explorer.link.R
import kotlinx.android.synthetic.main.menu_back_layout.*

class ProductIntroduceActivity : BaseActivity() {

    override fun getContentView(): Int {
        return R.layout.activity_product_introducation
    }

    override fun initView() {
        tv_title.setText(getString(R.string.bind_dev))
    }

    private fun loadRemoteRes() {

    }

    override fun setListener() {
        iv_back.setOnClickListener { finish() }
    }
}