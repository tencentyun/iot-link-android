package com.tencent.iot.explorer.link.kitlink.activity

import android.app.Activity
import android.content.Intent
import android.text.TextUtils
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.fastjson.JSON
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.T
import com.tencent.iot.explorer.link.core.auth.entity.DeviceEntity
import com.tencent.iot.explorer.link.customview.dialog.ListOptionsDialog
import com.tencent.iot.explorer.link.customview.dialog.SelectWoringTimeDialog
import com.tencent.iot.explorer.link.customview.dialog.WorkTimeMode
import com.tencent.iot.explorer.link.kitlink.adapter.ManualTaskAdapter
import com.tencent.iot.explorer.link.kitlink.consts.CommonField
import com.tencent.iot.explorer.link.kitlink.entity.DelayTimeExtra
import com.tencent.iot.explorer.link.kitlink.entity.DevModeInfo
import com.tencent.iot.explorer.link.kitlink.entity.ManualTask
import kotlinx.android.synthetic.main.activity_add_autoic_task.*
import kotlinx.android.synthetic.main.menu_back_layout.*
import java.util.*
import kotlin.collections.ArrayList


class AddAutoicTaskActivity : BaseActivity() {
    private var options = ArrayList<String>()
    private var tasks = ArrayList<String>()
    private var fitConditionType = ArrayList<String>()
    private var workTimeMode = WorkTimeMode()
    private var manualTasks: MutableList<ManualTask> = LinkedList()
    private var adapter: ManualTaskAdapter? = null

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

        fitConditionType.add(getString(R.string.fit_all_condition))
        fitConditionType.add(getString(R.string.fit_one_of_all_condition))
        showAddBtn()

        val layoutManager = LinearLayoutManager(this)
        lv_automic_task.setLayoutManager(layoutManager)
        adapter = ManualTaskAdapter(manualTasks, false)
        adapter?.setOnItemClicked(onListItemClicked)
        lv_automic_task.setAdapter(adapter)
    }

    private var onListItemClicked = object : ManualTaskAdapter.OnItemClicked {
        override fun onItemClicked(pos: Int, manualTask: ManualTask?) {
            if (manualTask!!.type == 1) {  // 延时任务启动后跳转修改延时的窗口
                var intent = Intent(this@AddAutoicTaskActivity, DelayTimeActivity::class.java)
                var delayTimeExtra = DelayTimeExtra.convertManualTask2DelayTimeExtra(manualTask!!)
                delayTimeExtra.pos = pos
                intent.putExtra(CommonField.EDIT_EXTRA, JSON.toJSONString(delayTimeExtra))
                startActivityForResult(intent, CommonField.EDIT_DELAY_TIME_REQ_CODE)
            }
        }

        override fun onAddTaskClicked() {

        }

        override fun onDeletedClicked(pos: Int) {
            manualTasks.removeAt(pos)
            adapter?.notifyDataSetChanged()
            refreshView()

        }

    }

    private fun showAddBtn() {
        if (add_manual_item_layout.visibility == View.VISIBLE) {
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
        layout_manual_task.setOnClickListener{
            var optionsDialog = ListOptionsDialog(this, fitConditionType)
            optionsDialog?.setOnDismisListener(fitTypeOptionsClicked)
            optionsDialog.show()
        }
        //manualTasks
        tv_next.setOnClickListener {
            if (manualTasks.size <= 0) {
                T.show(getString(R.string.no_task_to_add))
            } else if (manualTasks[manualTasks!!.lastIndex].type == 1) {
                T.show(getString(R.string.delay_time_can_not_be_last_one))
            } else {

                var intent = Intent(this@AddAutoicTaskActivity, CompleteTaskInfoActivity::class.java)
                intent.putExtra(CommonField.EXTRA_ALL_MANUAL_TASK, JSON.toJSONString(manualTasks))
                intent.putExtra(CommonField.EXTRA_ROUTE_TYPE, 1)
                startActivity(intent)
            }
        }

    }

    private var fitTypeOptionsClicked = object : ListOptionsDialog.OnDismisListener {
        override fun onItemClicked(pos: Int) {
            tv_tip_title.setText(fitConditionType.get(pos))
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
            if (pos == 0) {

            } else {
                var intent = Intent(this@AddAutoicTaskActivity, TimerActivity::class.java)
                startActivityForResult(intent, CommonField.ADD_TIMER_REQ_CODE)
            }
        }
    }

    private var listTasksClicked = object : ListOptionsDialog.OnDismisListener {
        override fun onItemClicked(pos: Int) {
            if (pos == 3) {
                var intent = Intent(this@AddAutoicTaskActivity, SetSendMsgActivity::class.java)
                startActivityForResult(intent, CommonField.ADD_SEND_MSG_REQ_CODE)
            } else if (pos == 2) {
                var intent = Intent(this@AddAutoicTaskActivity, SelectManualTaskActivity::class.java)
                startActivityForResult(intent, CommonField.ADD_MANUAL_TASK_REQ_CODE)
            } else if (pos == 1) {
                var intent = Intent(this@AddAutoicTaskActivity, DelayTimeActivity::class.java)
                startActivityForResult(intent, CommonField.ADD_DELAY_TIME_REQ_CODE)
            } else if (pos == 0) {
                var intent = Intent(this@AddAutoicTaskActivity, SmartSelectDevActivity::class.java)
                intent.putExtra(CommonField.EXTRA_ROUTE_TYPE, 1)
                startActivity(intent)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CommonField.ADD_DELAY_TIME_REQ_CODE &&  // 添加任务
            resultCode == Activity.RESULT_OK && data != null
        ) {
            var delayTaskStr = data?.getStringExtra(CommonField.DELAY_TIME_TASK)
            var task = JSON.parseObject(delayTaskStr, ManualTask::class.java)
            manualTasks.add(task)

        } else if (requestCode == CommonField.EDIT_DELAY_TIME_REQ_CODE && // 修改任务
            resultCode == Activity.RESULT_OK && data != null
        ) {
            var delayTaskStr = data?.getStringExtra(CommonField.DELAY_TIME_TASK)
            var task = JSON.parseObject(delayTaskStr, ManualTask::class.java)
            if (task.pos < 0) { // 异常情况不更新
                return
            }
            manualTasks.set(task.pos, task)
        } else if (requestCode == CommonField.ADD_SEND_MSG_REQ_CODE && // 修改任务
            resultCode == Activity.RESULT_OK && data != null) {
            var delayTaskStr = data?.getStringExtra(CommonField.EXTRA_SEND_MSG)
            var task = JSON.parseObject(delayTaskStr, ManualTask::class.java)
            manualTasks.add(task)
        } else if (requestCode == CommonField.ADD_MANUAL_TASK_REQ_CODE &&
            resultCode == Activity.RESULT_OK && data != null) {
            var delayTaskStr = data?.getStringExtra(CommonField.EXTRA_ADD_MANUAL_TASK)
            var tasks = JSON.parseArray(delayTaskStr, ManualTask::class.java)
            manualTasks.addAll(tasks)
        }

        adapter?.notifyDataSetChanged()
        refreshView()
    }

    private fun refreshView() {
        if (manualTasks.size > 0) {
            add_automic_item_layout.visibility = View.GONE
        } else {
            add_automic_item_layout.visibility = View.VISIBLE
        }

        showAddBtn()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        var str = intent.getStringExtra(CommonField.EXTRA_DEV_MODES)
        var devModeInfos = JSON.parseArray(str, DevModeInfo::class.java)
        if (devModeInfos == null || devModeInfos.size <= 0) {
            return
        }

        var devDetailStr = intent.getStringExtra(CommonField.EXTRA_DEV_DETAIL)
        for (i in 0 .. devModeInfos.size - 1) {
            var task = ManualTask()
            task.type = 0
            if (!TextUtils.isEmpty(devDetailStr)) {
                var dev = JSON.parseObject(devDetailStr, DeviceEntity::class.java)
                task.iconUrl = dev.IconUrl
                task.devName = dev.getAlias()
                task.productId = dev.ProductId
                task.deviceName = dev.DeviceName
            }
            task.actionId = devModeInfos.get(i).id
            task.taskTip = devModeInfos.get(i).name
            task.task = devModeInfos.get(i).value
            task.taskKey = devModeInfos.get(i).key
            manualTasks.add(task)
        }

        adapter?.notifyDataSetChanged()
        refreshView()
    }

}