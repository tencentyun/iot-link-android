package com.tencent.iot.explorer.link.kitlink.activity

import android.content.Intent
import android.os.Handler
import android.text.TextUtils
import android.util.Log
import com.alibaba.fastjson.JSON
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.kitlink.consts.CommonField
import com.tencent.iot.explorer.link.kitlink.entity.DelayTimeExtra
import com.tencent.iot.explorer.link.kitlink.entity.ManualTask
import kotlinx.android.synthetic.main.activity_delay_time.*
import kotlinx.android.synthetic.main.menu_back_layout.*

class DelayTimeActivity : BaseActivity() {

    private var editExtra: DelayTimeExtra = DelayTimeExtra()
    private var handler: Handler = Handler()

    override fun getContentView(): Int {
        return R.layout.activity_delay_time
    }

    override fun initView() {
        var editExtraStr = intent.getStringExtra(CommonField.EDIT_EXTRA)
        if (!TextUtils.isEmpty(editExtraStr)) {
            editExtra = JSON.parseObject(editExtraStr, DelayTimeExtra::class.java)
        }
        tv_title.setText(R.string.delay_time)
        initDatePicker()
    }

    override fun setListener() {
        iv_back.setOnClickListener { finish() }
        tv_ok.setOnClickListener {
            val intent = Intent()
            var manualTask = ManualTask()
            manualTask.aliasName = getString(R.string.delay_time)
            manualTask.hour = wheel_delay_time_hour.currentItemPosition
            manualTask.min = wheel_delay_time_min.currentItemPosition
            manualTask.pos = editExtra.pos
            intent.putExtra(CommonField.DELAY_TIME_TASK, JSON.toJSONString(manualTask))
            setResult(RESULT_OK, intent)
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

    }

    private fun initDatePicker() {
        var hours = ArrayList<String>()
        for (i in editExtra?.startHours .. editExtra?.endHours) {
            hours.add("$i" + getString(R.string.unit_h))
        }

        var minutes = ArrayList<String>()
        for (i in editExtra?.startMinute .. editExtra?.endMinute) {
            minutes.add("$i" + getString(R.string.unit_m))
        }
        wheel_delay_time_hour.setData(hours)
        wheel_delay_time_hour.setSelected(true)
        wheel_delay_time_min.setData(minutes)
        wheel_delay_time_min.setSelected(true)

        if (editExtra.editAble) {
            handler.postDelayed( {
                wheel_delay_time_hour.setSelectedItemPosition(editExtra.currentHour, false) },
                10)
            handler.postDelayed( {
                wheel_delay_time_min.setSelectedItemPosition(editExtra.currentMinute, false) },
                10)
        }

        wheel_delay_time_hour.setIndicator(true)
        wheel_delay_time_hour.setAtmospheric(true)
        wheel_delay_time_min.setIndicator(true)
        wheel_delay_time_min.setAtmospheric(true)
    }
}