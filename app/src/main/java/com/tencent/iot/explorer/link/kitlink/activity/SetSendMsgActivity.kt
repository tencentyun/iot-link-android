package com.tencent.iot.explorer.link.kitlink.activity

import android.content.Intent
import android.util.Log
import com.alibaba.fastjson.JSON
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.T
import com.tencent.iot.explorer.link.kitlink.consts.CommonField
import com.tencent.iot.explorer.link.kitlink.entity.ManualTask
import kotlinx.android.synthetic.main.activity_set_notification_type.*
import kotlinx.android.synthetic.main.activity_set_notification_type.tv_ok
import kotlinx.android.synthetic.main.menu_back_layout.*

class SetSendMsgActivity : BaseActivity(){

    override fun getContentView(): Int {
        return R.layout.activity_set_notification_type
    }

    override fun initView() {
        iv_back.setColorFilter(resources.getColor(R.color.black_333333))
        tv_title.text = getString(R.string.select_notification_type)
    }

    override fun setListener() {
        iv_back.setOnClickListener { finish() }
        tv_cancel.setOnClickListener { finish() }
        iv_switch_btn.setOnCheckedChangeListener { buttonView, isChecked ->
        }
        tv_ok.setOnClickListener {
            if (iv_switch_btn.isChecked) {
                val intent = Intent()
                var manualTask = ManualTask()
                manualTask.type = 2
                manualTask.devName = getString(R.string.send_notification)
                manualTask.task = getString(R.string.msg_center)
                intent.putExtra(CommonField.EXTRA_SEND_MSG, JSON.toJSONString(manualTask))
                setResult(RESULT_OK, intent)
                finish()
            } else {
                T.show(getString(R.string.at_least_select_one_msg_type))
            }
        }
    }

}
