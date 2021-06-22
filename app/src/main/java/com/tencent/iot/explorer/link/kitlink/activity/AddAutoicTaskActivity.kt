package com.tencent.iot.explorer.link.kitlink.activity

import android.app.Activity
import android.content.Intent
import android.text.TextUtils
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.fastjson.JSON
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.T
import com.tencent.iot.explorer.link.core.auth.entity.DevModeInfo
import com.tencent.iot.explorer.link.core.auth.entity.DeviceEntity
import com.tencent.iot.explorer.link.customview.dialog.ListOptionsDialog
import com.tencent.iot.explorer.link.customview.dialog.SelectWoringTimeDialog
import com.tencent.iot.explorer.link.customview.dialog.entity.WorkTimeMode
import com.tencent.iot.explorer.link.kitlink.adapter.ManualTaskAdapter
import com.tencent.iot.explorer.link.kitlink.consts.CommonField
import com.tencent.iot.explorer.link.kitlink.entity.*
import kotlinx.android.synthetic.main.activity_add_autoic_task.*
import kotlinx.android.synthetic.main.add_new_item_layout.view.*
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
    private var manualConditions: MutableList<ManualTask> = LinkedList()
    private var conditionAdapter: ManualTaskAdapter? = null

    override fun getContentView(): Int {
        return R.layout.activity_add_autoic_task
    }

    override fun initView() {
        tv_title.setText(R.string.add_automic_smart)
        add_automic_item_layout.tv_tip_add_condition.setText(R.string.add_task)
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

        val conditionLayoutManager = LinearLayoutManager(this)
        lv_condition_task.setLayoutManager(conditionLayoutManager)
        conditionAdapter = ManualTaskAdapter(manualConditions, false)
        conditionAdapter?.setOnItemClicked(onConditionListItemClicked)
        lv_condition_task.setAdapter(conditionAdapter)
    }

    private var onConditionListItemClicked = object : ManualTaskAdapter.OnItemClicked {
        override fun onItemClicked(pos: Int, manualTask: ManualTask?) {
            if (manualTask!!.type == 4) {  // 延时任务启动后跳转修改延时的窗口
                var intent = Intent(this@AddAutoicTaskActivity, TimerActivity::class.java)
                var timerExtra = TimerExtra.convertManualTask2TimerExtra(manualTask!!)
                timerExtra.pos = pos
                intent.putExtra(CommonField.EDIT_EXTRA, JSON.toJSONString(timerExtra))
                startActivityForResult(intent, CommonField.EDIT_TIMER_REQ_CODE)
            } else if (manualTask!!.type == 5) {
                var intent = Intent(this@AddAutoicTaskActivity, SmartSelectDevActivity::class.java)
                manualTask.pos = pos
                intent.putExtra(CommonField.EDIT_EXTRA, JSON.toJSONString(manualTask))
                intent.putExtra(CommonField.EXTRA_ROUTE_TYPE, RouteType.EDIT_AUTOMIC_CONDITION_ROUTE)
                startActivity(intent)
            }
        }

        override fun onAddTaskClicked() {}

        override fun onDeletedClicked(pos: Int) {
            manualConditions.removeAt(pos)
            conditionAdapter?.notifyDataSetChanged()
            refreshView()
        }
    }

    private var onListItemClicked = object : ManualTaskAdapter.OnItemClicked {
        override fun onItemClicked(pos: Int, manualTask: ManualTask?) {
            if (manualTask!!.type == 1) {  // 延时任务启动后跳转修改延时的窗口
                var intent = Intent(this@AddAutoicTaskActivity, DelayTimeActivity::class.java)
                var delayTimeExtra = DelayTimeExtra.convertManualTask2DelayTimeExtra(manualTask!!)
                delayTimeExtra.pos = pos
                intent.putExtra(CommonField.EDIT_EXTRA, JSON.toJSONString(delayTimeExtra))
                startActivityForResult(intent, CommonField.EDIT_DELAY_TIME_REQ_CODE)
            } else if (manualTask!!.type == 0) {
                var intent = Intent(this@AddAutoicTaskActivity, SmartSelectDevActivity::class.java)
                manualTask.pos = pos
                intent.putExtra(CommonField.EDIT_EXTRA, JSON.toJSONString(manualTask))
                intent.putExtra(CommonField.EXTRA_ROUTE_TYPE, RouteType.EDIT_AUTOMIC_TASK_ROUTE)
                startActivity(intent)
            } else if (manualTask!!.type == 3) {
                var intent = Intent(this@AddAutoicTaskActivity, SelectManualTaskActivity::class.java)
                manualTask.pos = pos
                intent.putExtra(CommonField.EDIT_EXTRA, JSON.toJSONString(manualTask))
                intent.putExtra(CommonField.EXTRA_SINGLE_CHECK, true)
                startActivityForResult(intent, CommonField.EDIT_MANUAL_TASK_REQ_CODE)
            }
        }

        override fun onAddTaskClicked() {}

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
            var workTimeDialog = SelectWoringTimeDialog(this@AddAutoicTaskActivity, workTimeMode)
            workTimeDialog.show()
            workTimeDialog.setOnDismisListener(workTimeDialogListener)
        }

        layout_manual_task.setOnClickListener{
            var optionsDialog = ListOptionsDialog(this, fitConditionType)
            optionsDialog?.setOnDismisListener(fitTypeOptionsClicked)
            optionsDialog.show()
        }

        tv_next.setOnClickListener {
            if (manualTasks.size <= 0) {
                T.show(getString(R.string.no_task_to_add))
            } else if (manualTasks[manualTasks!!.lastIndex].type == 1) {
                T.show(getString(R.string.delay_time_can_not_be_last_one))
            } else if (manualConditions.size <= 0) {
                T.show(getString(R.string.at_least_one_condition_2_start))
            } else {

                var intent = Intent(this@AddAutoicTaskActivity, CompleteTaskInfoActivity::class.java)

                var automicTaskEntity = AutomicTaskEntity()
                if (tv_tip_title.text.toString().equals(fitConditionType.get(0))) {
                    automicTaskEntity.matchType = 0
                } else {
                    automicTaskEntity.matchType = 1
                }
                automicTaskEntity.status = 1
                automicTaskEntity.conditionsItem = manualConditions
                automicTaskEntity.tasksItem = manualTasks
                automicTaskEntity.workTimeMode = workTimeMode
                intent.putExtra(CommonField.EXTRA_ALL_AUTOMIC_TASK, JSON.toJSONString(automicTaskEntity))

                intent.putExtra(CommonField.EXTRA_ROUTE_TYPE, RouteType.AUTOMIC_TASK_ROUTE)
                startActivity(intent)
            }
        }
    }

    private fun resetWorkTimeTip() {
        if (workTimeMode!!.timeType == 0) {
            tv_working_time_value.setText(R.string.work_time_all_day_tip)
        } else {
            tv_working_time_value.setText(String.format("%02d:%02d-%02d:%02d", workTimeMode.startTimeHour,
                    workTimeMode.startTimerMin, workTimeMode.endTimeHour, workTimeMode.endTimeMin))
        }
    }

    private var workTimeDialogListener = object: SelectWoringTimeDialog.OnDismisListener {
        override fun onSaveClicked(workTimeMode: WorkTimeMode?) {
            this@AddAutoicTaskActivity.workTimeMode = workTimeMode!!
            resetWorkTimeTip()
        }

        override fun onCancelClicked() {}
    }

    private var fitTypeOptionsClicked = object : ListOptionsDialog.OnDismisListener {
        override fun onItemClicked(pos: Int) {
            tv_tip_title.setText(fitConditionType.get(pos))
        }
    }

    override fun onResume() {
        super.onResume()
        resetWorkTimeTip()
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
            if (manualConditions.size >= 20) {
                T.show(getString(R.string.condition_can_not_more_then_20))
                return
            }
            if (pos == 0) {
                var intent = Intent(this@AddAutoicTaskActivity, SmartSelectDevActivity::class.java)
                intent.putExtra(CommonField.EXTRA_ROUTE_TYPE, RouteType.AUTOMIC_CONDITION_ROUTE)
                startActivity(intent)
            } else {
                var intent = Intent(this@AddAutoicTaskActivity, TimerActivity::class.java)
                startActivityForResult(intent, CommonField.ADD_TIMER_REQ_CODE)
            }
        }
    }

    private var listTasksClicked = object : ListOptionsDialog.OnDismisListener {
        override fun onItemClicked(pos: Int) {
            if (manualTasks.size >= 20) {
                T.show(getString(R.string.task_can_not_more_then_20))
                return
            }
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
                intent.putExtra(CommonField.EXTRA_ROUTE_TYPE, RouteType.AUTOMIC_TASK_ROUTE)
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
            var manualTaskStr = data?.getStringExtra(CommonField.EXTRA_ADD_MANUAL_TASK)
            var tasks = JSON.parseArray(manualTaskStr, ManualTask::class.java)
            if (manualTasks.size + tasks.size > 20) {
                T.show(getString(R.string.task_can_not_more_then_20))
            } else {
                manualTasks.addAll(tasks)
            }
        } else if (CommonField.EDIT_TIMER_REQ_CODE == requestCode &&
            resultCode == Activity.RESULT_OK && data != null) {
            var timerTaskStr = data?.getStringExtra(CommonField.TIMER_TASK)
            var task = JSON.parseObject(timerTaskStr, ManualTask::class.java)
            if (task.pos < 0) { // 异常情况不更新
                return
            }
            manualConditions.set(task.pos, task)

        } else if (CommonField.ADD_TIMER_REQ_CODE == requestCode &&
            resultCode == Activity.RESULT_OK && data != null) {
            var timerTaskStr = data?.getStringExtra(CommonField.TIMER_TASK)
            var task = JSON.parseObject(timerTaskStr, ManualTask::class.java)
            manualConditions.add(task)
        } else if (CommonField.EDIT_MANUAL_TASK_REQ_CODE == requestCode &&
            resultCode == Activity.RESULT_OK && data != null) {
            var manualTaskStr = data?.getStringExtra(CommonField.EXTRA_ADD_MANUAL_TASK)
            var tasks = JSON.parseArray(manualTaskStr, ManualTask::class.java)
            for (ele in tasks) {
                manualTasks.set(ele.pos, ele)
            }
        }

        conditionAdapter?.notifyDataSetChanged()
        adapter?.notifyDataSetChanged()
        refreshView()
    }

    private fun refreshView() {
        if (manualTasks.size > 0) {
            add_automic_item_layout.visibility = View.GONE
        } else {
            add_automic_item_layout.visibility = View.VISIBLE
        }

        if (manualConditions.size > 0) {
            add_manual_item_layout.visibility = View.GONE
        } else {
            add_manual_item_layout.visibility = View.VISIBLE
        }

        showAddBtn()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        var str = intent.getStringExtra(CommonField.EXTRA_DEV_MODES)
        var routeType = intent.getIntExtra(CommonField.EXTRA_ROUTE_TYPE, RouteType.MANUAL_TASK_ROUTE)
        var devModeInfos = JSON.parseArray(str, DevModeInfo::class.java)
        if (devModeInfos == null || devModeInfos.size <= 0) {
            return
        }

        if (routeType == RouteType.AUTOMIC_CONDITION_ROUTE && manualConditions.size + devModeInfos.size > 20) {
            T.show(getString(R.string.condition_can_not_more_then_20))
            return
        } else if (routeType == RouteType.AUTOMIC_TASK_ROUTE && manualTasks.size + devModeInfos.size > 20) {
            T.show(getString(R.string.task_can_not_more_then_20))
            return
        }

        var devDetailStr = intent.getStringExtra(CommonField.EXTRA_DEV_DETAIL)
        for (i in 0 .. devModeInfos.size - 1) {
            var task = ManualTask()
            if (!TextUtils.isEmpty(devDetailStr)) {
                var dev = JSON.parseObject(devDetailStr, DeviceEntity::class.java)
                task.iconUrl = dev.IconUrl
//                task.devName = dev.getAlias()
                task.productId = dev.ProductId
                task.deviceName = dev.DeviceName
                task.aliasName = dev.AliasName
            }
            task.actionId = devModeInfos.get(i).id
            task.taskTip = devModeInfos.get(i).name
            task.task = devModeInfos.get(i).value
            task.unit = devModeInfos.get(i).unit
            task.taskKey = devModeInfos.get(i).key
            if (routeType == RouteType.AUTOMIC_CONDITION_ROUTE) {
                task.type = 5
                task.op = devModeInfos.get(i).op
                manualConditions.add(task)
            } else if (routeType == RouteType.AUTOMIC_TASK_ROUTE) {
                task.type = 0
                manualTasks.add(task)
            } else if (routeType == RouteType.EDIT_AUTOMIC_TASK_ROUTE) {
                task.type = 0
                manualTasks.set(devModeInfos.get(i).pos, task)
            } else if (routeType == RouteType.EDIT_AUTOMIC_CONDITION_ROUTE) {
                task.op = devModeInfos.get(i).op
                task.type = 5
                manualConditions.set(devModeInfos.get(i).pos, task)
            }
        }

        adapter?.notifyDataSetChanged()
        conditionAdapter?.notifyDataSetChanged()
        refreshView()
    }

}