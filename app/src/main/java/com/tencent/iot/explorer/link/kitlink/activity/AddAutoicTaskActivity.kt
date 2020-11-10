package com.tencent.iot.explorer.link.kitlink.activity

import android.view.View
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.customview.dialog.ListOptionsDialog
import com.tencent.iot.explorer.link.customview.dialog.SelectWoringTimeDialog
import com.tencent.iot.explorer.link.customview.dialog.WorkTimeMode
import kotlinx.android.synthetic.main.activity_add_autoic_task.*
import kotlinx.android.synthetic.main.menu_back_layout.*


class AddAutoicTaskActivity : BaseActivity() {
    private var options = ArrayList<String>()
    private var tasks = ArrayList<String>()
    private var workTimeMode = WorkTimeMode()

    override fun getContentView(): Int {
        return R.layout.activity_add_autoic_task
    }

    override fun initView() {
        tv_title.setText(R.string.add_automic_smart)
        options.add(getString(R.string.dev_status_changed))
        options.add(getString(R.string.dev_timer))

        tasks.add(getString(R.string.control_dev))
        tasks.add(getString(R.string.delay_time))
        tasks.add(getString(R.string.sel_manual_task))
        tasks.add(getString(R.string.send_notification))

        showAddBtn()
    }

    private fun showAddBtn() {
        if (iv_add_condition_task.visibility == View.VISIBLE) {
            iv_add_condition_task.visibility = View.GONE
        } else {
            iv_add_condition_task.visibility = View.VISIBLE
        }

        if (add_automic_item_layout.visibility == View.VISIBLE) {
            iv_automiac_add_task.visibility = View.GONE
        } else {
            iv_automiac_add_task.visibility = View.VISIBLE
        }
    }

    override fun setListener() {
        iv_back.setOnClickListener { finish() }
        add_manual_item_layout.setOnClickListener {showAddConditionDialog()}
        iv_add_condition_task.setOnClickListener{showAddConditionDialog()}
        add_automic_item_layout.setOnClickListener {showAddTaskDialog()}
        iv_automiac_add_task.setOnClickListener {showAddTaskDialog()}
        working_time_layout.setOnClickListener {
            SelectWoringTimeDialog(this@AddAutoicTaskActivity, workTimeMode).show()
        }
    }

    override fun onResume() {
        super.onResume()
        if (workTimeMode.timeType == 0) {
            tv_working_time_value.setText(R.string.work_time_all_day_tip)
        } else {
            tv_working_time_value.setText(
                String.format("%02d", workTimeMode.startTimeHour) + ":" +
                String.format("%02d", workTimeMode.startTimerMin) + "-" +
                String.format("%02d", workTimeMode.endTimeHour) + ":" +
                String.format("%02d", workTimeMode.endTimeMin))
        }
    }

    private fun showAddConditionDialog() {
        var optionsDialog = ListOptionsDialog(this, options)
        optionsDialog?.setOnDismisListener(listOptionsClicked)
        optionsDialog.show()
    }

    private fun showAddTaskDialog() {
        var optionsDialog = ListOptionsDialog(this, tasks)
        optionsDialog?.setOnDismisListener(listTasksClicked)
        optionsDialog.show()
    }

    private var listOptionsClicked = object : ListOptionsDialog.OnDismisListener {
        override fun onItemClicked(pos: Int) {

        }
    }

    private var listTasksClicked = object : ListOptionsDialog.OnDismisListener {
        override fun onItemClicked(pos: Int) {

        }
    }
}