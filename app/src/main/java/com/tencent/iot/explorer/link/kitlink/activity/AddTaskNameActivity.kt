package com.tencent.iot.explorer.link.kitlink.activity

import android.app.Activity
import android.content.Intent
import android.text.TextUtils
import android.view.View
import com.alibaba.fastjson.JSON
import com.squareup.picasso.Picasso
import com.tencent.iot.explorer.link.App
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.kitlink.consts.CommonField
import com.tencent.iot.explorer.link.kitlink.entity.ManualTask
import kotlinx.android.synthetic.main.activity_add_task_name.*
import kotlinx.android.synthetic.main.activity_complete_task_info.*
import kotlinx.android.synthetic.main.activity_complete_task_info.tv_ok
import kotlinx.android.synthetic.main.menu_back_layout.*


class AddTaskNameActivity : BaseActivity() {

    override fun getContentView(): Int {
        return R.layout.activity_add_task_name
    }

    override fun initView() {
        tv_title.setText(R.string.add_smart_name)
        ev_task_name.setText(intent.getStringExtra(CommonField.EXYRA_TASK_NAME));
    }

    override fun setListener() {
        iv_back.setOnClickListener { finish() }
        tv_ok.setOnClickListener {
            val intent = Intent()
            intent.putExtra(CommonField.EXYRA_TASK_NAME, ev_task_name.text.toString())
            setResult(RESULT_OK, intent)
            finish()
        }
    }
}