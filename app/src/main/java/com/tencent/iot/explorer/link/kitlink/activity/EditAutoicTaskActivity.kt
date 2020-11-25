package com.tencent.iot.explorer.link.kitlink.activity

import android.app.Activity
import android.content.Intent
import android.text.TextUtils
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.squareup.picasso.Picasso
import com.tencent.iot.explorer.link.App
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.T
import com.tencent.iot.explorer.link.core.auth.entity.DeviceEntity
import com.tencent.iot.explorer.link.core.auth.response.BaseResponse
import com.tencent.iot.explorer.link.customview.dialog.ListOptionsDialog
import com.tencent.iot.explorer.link.customview.dialog.SelectWoringTimeDialog
import com.tencent.iot.explorer.link.customview.dialog.WorkTimeMode
import com.tencent.iot.explorer.link.kitlink.adapter.ManualTaskAdapter
import com.tencent.iot.explorer.link.kitlink.consts.CommonField
import com.tencent.iot.explorer.link.kitlink.entity.*
import com.tencent.iot.explorer.link.kitlink.util.HttpRequest
import com.tencent.iot.explorer.link.core.auth.callback.MyCallback
import com.tencent.iot.explorer.link.kitlink.util.RequestCode
import com.tencent.iot.explorer.link.kitlink.util.Utils
import kotlinx.android.synthetic.main.activity_add_autoic_task.*
import kotlinx.android.synthetic.main.activity_complete_task_info.*
import kotlinx.android.synthetic.main.activity_complete_task_info.iv_smart_background
import kotlinx.android.synthetic.main.activity_complete_task_info.layout_smart_name
import kotlinx.android.synthetic.main.activity_complete_task_info.layout_smart_pic
import kotlinx.android.synthetic.main.activity_complete_task_info.tv_unset_tip_1
import kotlinx.android.synthetic.main.activity_complete_task_info.tv_unset_tip_2
import kotlinx.android.synthetic.main.activity_edit_automic_task.*
import kotlinx.android.synthetic.main.activity_edit_automic_task.add_automic_item_layout
import kotlinx.android.synthetic.main.activity_edit_automic_task.add_manual_item_layout
import kotlinx.android.synthetic.main.activity_edit_automic_task.iv_add_condition_task
import kotlinx.android.synthetic.main.activity_edit_automic_task.iv_automiac_add_task
import kotlinx.android.synthetic.main.activity_edit_automic_task.layout_manual_task
import kotlinx.android.synthetic.main.activity_edit_automic_task.lv_automic_task
import kotlinx.android.synthetic.main.activity_edit_automic_task.lv_condition_task
import kotlinx.android.synthetic.main.activity_edit_automic_task.tv_next
import kotlinx.android.synthetic.main.activity_edit_automic_task.tv_tip_title
import kotlinx.android.synthetic.main.activity_edit_automic_task.tv_working_time_value
import kotlinx.android.synthetic.main.activity_edit_automic_task.working_time_layout
import kotlinx.android.synthetic.main.menu_back_layout.*
import java.util.*
import kotlin.collections.ArrayList


class EditAutoicTaskActivity : BaseActivity(), MyCallback {
    private var options = ArrayList<String>()
    private var tasks = ArrayList<String>()
    private var fitConditionType = ArrayList<String>()
    private var workTimeMode = WorkTimeMode()
    private var manualTasks: MutableList<ManualTask> = LinkedList()
    private var adapter: ManualTaskAdapter? = null
    private var manualConditions: MutableList<ManualTask> = LinkedList()
    private var conditionAdapter: ManualTaskAdapter? = null
    @Volatile
    private var smartPicUrl = ""
    @Volatile
    private var smartName = ""
    private var automation: Automation? = null

    override fun getContentView(): Int {
        return R.layout.activity_edit_automic_task
    }

    override fun initView() {
        tv_title.setText(R.string.edit_automic_smart)

        var infoStr = intent.getStringExtra(CommonField.EXTRA_INFO)
        if (TextUtils.isEmpty(infoStr)) return

        automation = JSON.parseObject(infoStr, Automation::class.java)
        if (automation == null) return

        if (automation!!.type == 1) {
            smartName = automation!!.Name
            smartPicUrl = automation!!.Icon
        } else {
            return
        }

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

        layout_task_detail.visibility = View.GONE
        loadTitleViewData()

        HttpRequest.instance.getAutomicTaskDetail(automation!!.id, this)
    }

    fun loadTitleViewData() {
        if (TextUtils.isEmpty(smartName.trim())) {
            tv_unset_tip_2.setText(R.string.unset)
        } else {
            tv_unset_tip_2.setText(smartName)
        }

        if (TextUtils.isEmpty(smartPicUrl.trim())) {
            iv_smart_background.visibility = View.GONE
            tv_unset_tip_1.visibility = View.VISIBLE
        } else {
            iv_smart_background.visibility = View.VISIBLE
            tv_unset_tip_1.visibility = View.GONE
            Picasso.get().load(smartPicUrl).into(iv_smart_background)
        }
    }

    fun loadTaskDeatils() {
        resetWorkTimeTip()
    }

    private var onConditionListItemClicked = object : ManualTaskAdapter.OnItemClicked {
        override fun onItemClicked(pos: Int, manualTask: ManualTask?) {
            if (manualTask!!.type == 4) {  // 延时任务启动后跳转修改延时的窗口
                var intent = Intent(this@EditAutoicTaskActivity, TimerActivity::class.java)
                var timerExtra = TimerExtra.convertManualTask2TimerExtra(manualTask!!)
                timerExtra.pos = pos
                intent.putExtra(CommonField.EDIT_EXTRA, JSON.toJSONString(timerExtra))
                startActivityForResult(intent, CommonField.EDIT_TIMER_REQ_CODE)
            } else if (manualTask!!.type == 5) {
                var intent = Intent(this@EditAutoicTaskActivity, SmartSelectDevActivity::class.java)
                manualTask.pos = pos
                intent.putExtra(CommonField.EDIT_EXTRA, JSON.toJSONString(manualTask))
                intent.putExtra(CommonField.EXTRA_ROUTE_TYPE, RouteType.EDIT_AUTOMIC_CONDITION_DETAIL_ROUTE)
                startActivity(intent)
            }
        }

        override fun onAddTaskClicked() {}

        override fun onDeletedClicked(pos: Int) {
            manualConditions.removeAt(pos)
            refreshView()
        }
    }

    private var onListItemClicked = object : ManualTaskAdapter.OnItemClicked {
        override fun onItemClicked(pos: Int, manualTask: ManualTask?) {
            if (manualTask!!.type == 1) {  // 延时任务启动后跳转修改延时的窗口
                var intent = Intent(this@EditAutoicTaskActivity, DelayTimeActivity::class.java)
                var delayTimeExtra = DelayTimeExtra.convertManualTask2DelayTimeExtra(manualTask!!)
                delayTimeExtra.pos = pos
                intent.putExtra(CommonField.EDIT_EXTRA, JSON.toJSONString(delayTimeExtra))
                startActivityForResult(intent, CommonField.EDIT_DELAY_TIME_REQ_CODE)
            } else if (manualTask!!.type == 0) {
                var intent = Intent(this@EditAutoicTaskActivity, SmartSelectDevActivity::class.java)
                manualTask.pos = pos
                intent.putExtra(CommonField.EDIT_EXTRA, JSON.toJSONString(manualTask))
                intent.putExtra(CommonField.EXTRA_ROUTE_TYPE, RouteType.EDIT_AUTOMIC_TASK_DETAIL_ROUTE)
                startActivity(intent)
            } else if (manualTask!!.type == 3) {
                var intent = Intent(this@EditAutoicTaskActivity, SelectManualTaskActivity::class.java)
                manualTask.pos = pos
                intent.putExtra(CommonField.EDIT_EXTRA, JSON.toJSONString(manualTask))
                intent.putExtra(CommonField.EXTRA_SINGLE_CHECK, true)
                startActivityForResult(intent, CommonField.EDIT_MANUAL_TASK_REQ_CODE)
            }
        }

        override fun onAddTaskClicked() {}

        override fun onDeletedClicked(pos: Int) {
            manualTasks.removeAt(pos)
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
            var workTimeDialog = SelectWoringTimeDialog(this@EditAutoicTaskActivity, workTimeMode)
            workTimeDialog.show()
            workTimeDialog.setOnDismisListener(workTimeDialogListener)
        }

        layout_manual_task.setOnClickListener{
            var optionsDialog = ListOptionsDialog(this, fitConditionType)
            optionsDialog?.setOnDismisListener(fitTypeOptionsClicked)
            optionsDialog.show()
        }

        tv_next.setOnClickListener {
            if (TextUtils.isEmpty(smartPicUrl)) {
                T.show(getString(R.string.please_input_smart_pic))
                return@setOnClickListener
            }

            if (TextUtils.isEmpty(smartName)) {
                T.show(getString(R.string.please_input_smart_name))
                return@setOnClickListener
            }

            if (manualTasks.size <= 0) {
                T.show(getString(R.string.no_task_to_add))
            } else if (manualTasks[manualTasks!!.lastIndex].type == 1) {
                T.show(getString(R.string.delay_time_can_not_be_last_one))
            } else if (manualConditions.size <= 0) {
                T.show(getString(R.string.at_least_one_condition_2_start))
            } else {
                updateAutomicTask()
            }
        }

        iv_back.setOnClickListener { finish() }
        layout_smart_pic.setOnClickListener {
            var intent = Intent(this, SelectTaskPicActivity::class.java)
            intent.putExtra(CommonField.EXTRA_PIC_URL, smartPicUrl)
            startActivityForResult(intent, CommonField.REQUEST_PIC_REQ_CODE)
        }
        layout_smart_name.setOnClickListener {
            var intent = Intent(this, AddTaskNameActivity::class.java)
            intent.putExtra(CommonField.EXYRA_TASK_NAME, smartName)
            startActivityForResult(intent, CommonField.REQUEST_TASK_NAME_REQ_CODE)
        }
    }

    private fun updateAutomicTask() {
        var automicTaskEntity = AutomicTaskEntity()
        if (tv_tip_title.text.toString().equals(fitConditionType.get(0))) {
            automicTaskEntity.matchType = 0
        } else {
            automicTaskEntity.matchType = 1
        }
        automicTaskEntity.automationId = automation!!.id
        automicTaskEntity.familyId = App.data.getCurrentFamily().FamilyId
        automicTaskEntity.icon = smartPicUrl
        automicTaskEntity.name = smartName
        automicTaskEntity.status = automation!!.Status
        automicTaskEntity.tasksItem = manualTasks
        automicTaskEntity.conditionsItem = manualConditions
        automicTaskEntity.workTimeMode = workTimeMode
        if (automicTaskEntity?.tasksItem != null) {
            automicTaskEntity?.actions = JSONArray()
            for (i in 0 until automicTaskEntity?.tasksItem!!.size) {
                var taskJson = JSONObject()
                if (automicTaskEntity?.tasksItem?.get(i)?.type == 1) {  // 延时任务
                    taskJson.put("ActionType", 1)
                    taskJson.put("Data", automicTaskEntity?.tasksItem!!.get(i).hour * 60 * 60 + automicTaskEntity?.tasksItem!!.get(i).min * 60) // 单位是s

                } else if (automicTaskEntity?.tasksItem?.get(i)?.type == 0) {  // 设备控制
                    taskJson.put("ActionType", 0)
                    taskJson.put("ProductId", automicTaskEntity?.tasksItem?.get(i)?.productId)
                    taskJson.put("DeviceName", automicTaskEntity?.tasksItem?.get(i)?.deviceName)
                    taskJson.put("AliasName", automicTaskEntity?.tasksItem?.get(i)?.aliasName)
                    taskJson.put("IconUrl", automicTaskEntity?.tasksItem?.get(i)?.iconUrl)

                    var jsonAction = JSONObject()
                    // 存在 key 值的使用 key，不存在 key 的使用 value，进度没有 key，bool 和 enum 存在 key
                    if (TextUtils.isEmpty(automicTaskEntity?.tasksItem?.get(i)?.taskKey)) {
                        jsonAction.put(automicTaskEntity?.tasksItem?.get(i)?.actionId, automicTaskEntity?.tasksItem?.get(i)?.task)
                    } else {
                        jsonAction.put(automicTaskEntity?.tasksItem?.get(i)?.actionId, automicTaskEntity?.tasksItem?.get(i)?.taskKey)
                    }
                    taskJson.put("Data", jsonAction.toJSONString())
                } else if (automicTaskEntity?.tasksItem?.get(i)?.type == 2) {  // 通知
                    taskJson.put("ActionType", 3)
                    taskJson.put("Data", automicTaskEntity?.tasksItem?.get(i)?.notificationType)

                } else if (automicTaskEntity?.tasksItem?.get(i)?.type == 3) {  // 选择手动
                    taskJson.put("ActionType", 2)
                    taskJson.put("Data", automicTaskEntity?.tasksItem?.get(i)?.sceneId)
                    taskJson.put("DeviceName", automicTaskEntity?.tasksItem?.get(i)?.task)
                }
                automicTaskEntity?.actions?.add(taskJson)
            }
        }

        if (automicTaskEntity?.conditionsItem != null) {
            automicTaskEntity?.conditions = JSONArray()
            for (i in 0 until automicTaskEntity?.conditionsItem!!.size) {
                var conditionJson = JSONObject()
                conditionJson.put("CondId", System.currentTimeMillis().toString())

                if (automicTaskEntity?.conditionsItem?.get(i)?.type == 5) {  // 场景
                    conditionJson.put("CondType", 0)
                    var propertyJson = JSONObject()
                    propertyJson.put("ProductId", automicTaskEntity?.conditionsItem?.get(i)?.productId)
                    propertyJson.put("DeviceName", automicTaskEntity?.conditionsItem?.get(i)?.deviceName)
                    propertyJson.put("Op", "eq")
                    propertyJson.put("IconUrl", automicTaskEntity?.conditionsItem?.get(i)?.iconUrl)
                    if (TextUtils.isEmpty(automicTaskEntity?.conditionsItem?.get(i)?.taskKey)) {
                        propertyJson.put("Value", automicTaskEntity?.conditionsItem?.get(i)?.task)
                    } else {
                        propertyJson.put("Value", automicTaskEntity?.conditionsItem?.get(i)?.taskKey)
                    }
                    propertyJson.put("AliasName", automicTaskEntity?.conditionsItem?.get(i)?.aliasName)
                    propertyJson.put("PropertyId", automicTaskEntity?.conditionsItem?.get(i)?.actionId)
                    conditionJson.put("Property", propertyJson)

                } else if (automicTaskEntity?.conditionsItem?.get(i)?.type == 4) {  // 定时
                    conditionJson.put("CondType", 1)
                    var timerJson = JSONObject()
                    var dayStr = ""
                    if (automicTaskEntity?.conditionsItem?.get(i)?.workDayType == 0) {
                        dayStr = "0000000"
                    } else if (automicTaskEntity?.conditionsItem?.get(i)?.workDayType == 1) {
                        dayStr = "1111111"
                    } else if (automicTaskEntity?.conditionsItem?.get(i)?.workDayType == 2) {
                        dayStr = "0111110"
                    } else if (automicTaskEntity?.conditionsItem?.get(i)?.workDayType == 3) {
                        dayStr = "1000001"
                    } else {
                        dayStr = automicTaskEntity?.conditionsItem?.get(i)?.workDays!!
                    }
                    timerJson.put("Days", dayStr)
                    var time = String.format("%02d:%02d", automicTaskEntity?.conditionsItem?.get(i)?.hour,
                        automicTaskEntity?.conditionsItem?.get(i)?.min)
                    timerJson.put("TimePoint", time)
                    conditionJson.put("Timer", timerJson)
                }
                automicTaskEntity?.conditions?.add(conditionJson)
            }
        }

        HttpRequest.instance.updateAutomicTask(automicTaskEntity!!, this)
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
            this@EditAutoicTaskActivity.workTimeMode = workTimeMode!!
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
            if (pos == 0) {
                var intent = Intent(this@EditAutoicTaskActivity, SmartSelectDevActivity::class.java)
                intent.putExtra(CommonField.EXTRA_ROUTE_TYPE, RouteType.ADD_AUTOMIC_CONDITION_DETAIL_ROUTE)
                startActivity(intent)
            } else {
                var intent = Intent(this@EditAutoicTaskActivity, TimerActivity::class.java)
                startActivityForResult(intent, CommonField.ADD_TIMER_REQ_CODE)
            }
        }
    }

    private var listTasksClicked = object : ListOptionsDialog.OnDismisListener {
        override fun onItemClicked(pos: Int) {
            if (pos == 3) {
                var intent = Intent(this@EditAutoicTaskActivity, SetSendMsgActivity::class.java)
                startActivityForResult(intent, CommonField.ADD_SEND_MSG_REQ_CODE)
            } else if (pos == 2) {
                var intent = Intent(this@EditAutoicTaskActivity, SelectManualTaskActivity::class.java)
                startActivityForResult(intent, CommonField.ADD_MANUAL_TASK_REQ_CODE)
            } else if (pos == 1) {
                var intent = Intent(this@EditAutoicTaskActivity, DelayTimeActivity::class.java)
                startActivityForResult(intent, CommonField.ADD_DELAY_TIME_REQ_CODE)
            } else if (pos == 0) {
                var intent = Intent(this@EditAutoicTaskActivity, SmartSelectDevActivity::class.java)
                intent.putExtra(CommonField.EXTRA_ROUTE_TYPE, RouteType.ADD_AUTOMIC_TASK_DETAIL_ROUTE)
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

        refreshView()

        if (requestCode == CommonField.REQUEST_PIC_REQ_CODE &&  // 添加任务
            resultCode == Activity.RESULT_OK && data != null) {
            var picUrl = data?.getStringExtra(CommonField.EXTRA_PIC_URL)
            smartPicUrl = picUrl
        } else if (requestCode == CommonField.REQUEST_TASK_NAME_REQ_CODE &&  // 添加任务
            resultCode == Activity.RESULT_OK && data != null) {
            var name = data?.getStringExtra(CommonField.EXYRA_TASK_NAME)
            smartName = name.trim()
        }

        loadTitleViewData()
    }

    private fun refreshView() {

        conditionAdapter?.notifyDataSetChanged()
        adapter?.notifyDataSetChanged()

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
            task.taskKey = devModeInfos.get(i).key
            if (routeType == RouteType.ADD_AUTOMIC_CONDITION_DETAIL_ROUTE) {
                task.type = 5
                manualConditions.add(task)
            } else if (routeType == RouteType.ADD_AUTOMIC_TASK_DETAIL_ROUTE) {
                task.type = 0
                manualTasks.add(task)
            } else if (routeType == RouteType.EDIT_AUTOMIC_TASK_DETAIL_ROUTE) {
                task.type = 0
                manualTasks.set(devModeInfos.get(i).pos, task)
            } else if (routeType == RouteType.EDIT_AUTOMIC_CONDITION_DETAIL_ROUTE) {
                task.type = 5
                manualConditions.set(devModeInfos.get(i).pos, task)
            }
        }

        refreshView()
    }

    override fun fail(msg: String?, reqCode: Int) {
        T.show(msg)
    }

    override fun success(response: BaseResponse, reqCode: Int) {
        when (reqCode) {
            RequestCode.get_automic_task_detail -> {
                if (response.isSuccess()) {
                    layout_task_detail.visibility = View.VISIBLE

                    convertResp2LocalData(response.data as JSONObject)
                    loadTaskDeatils()
                } else {
                    T.show(response.msg)
                }
            }

            RequestCode.update_automic_task -> {
                if (response.isSuccess()) {
                    T.show(getString(R.string.success_update))
                    Utils.sendRefreshBroadcast(this)
                    finish()
                } else {
                    T.show(response.msg)
                }
            }
        }

    }

    private fun convertResp2LocalData(jsonObject: JSONObject) {
        if (jsonObject == null) return

        var automicTaskEntity = JSONObject.parseObject(jsonObject.getString("Data"), AutomicTaskEntity::class.java)
        Log.e("XXX", "automicTaskEntity " + JSON.toJSONString(automicTaskEntity))
        workTimeMode.workDays = automicTaskEntity.effectiveDays
        workTimeMode.startTimeHour = Integer.valueOf(automicTaskEntity.effectiveBeginTime.split(":").get(0))
        workTimeMode.startTimerMin = Integer.valueOf(automicTaskEntity.effectiveBeginTime.split(":").get(1))
        workTimeMode.endTimeHour = Integer.valueOf(automicTaskEntity.effectiveEndTime.split(":").get(0))
        workTimeMode.endTimeMin = Integer.valueOf(automicTaskEntity.effectiveEndTime.split(":").get(1))
        if (WorkTimeMode.isAllDay(workTimeMode)) {
            workTimeMode.timeType = 0
        } else {
            workTimeMode.timeType = 1
        }
        workTimeMode.workDayType = WorkTimeMode.getDayType(workTimeMode.workDays)

        if (automicTaskEntity.matchType == 0) {
            tv_tip_title.setText(fitConditionType.get(0))
        } else {
            tv_tip_title.setText(fitConditionType.get(1))
        }

        makeCondition(automicTaskEntity.conditions!!)
        makeTask(automicTaskEntity.actions!!)
    }

    private fun makeTask(jsonArr: JSONArray) {
        for (i in 0 until jsonArr.size) {
            var task = ManualTask()
            var json = jsonArr.get(i) as JSONObject
            if (json.getIntValue("ActionType") == 1) {
                var time = json.getLongValue("Data")
                task.type = 1
                task.hour = time.toInt() / 3600
                var tmpSeconds = time.toInt() % 3600
                task.min = tmpSeconds / 60
                task.aliasName = getString(R.string.delay_time)
            } else if (json.getIntValue("ActionType") == 0) {
                task.type = json.getIntValue("ActionType")
                task.deviceName = json.getString("DeviceName")
                task.productId = json.getString("ProductId")
                task.iconUrl = json.getString("IconUrl")
                var value = json.getString("Data")
                var dataJson = JSON.parseObject(value)
                for (keys in dataJson.keys) {
                    task.actionId = keys
                    task.taskKey = dataJson.getIntValue(keys).toString()
                    task.task = dataJson.getIntValue(keys).toString()
                }

                convertKey2Value(task)
            } else if (json.getIntValue("ActionType") == 2) {
                Log.e("XXX", "json " + json.toJSONString())
                task.type = 3
                task.aliasName = getString(R.string.sel_manual_task)
                task.sceneId = json.getString("Data")
                task.task = json.getString("DeviceName")
            } else if (json.getIntValue("ActionType") == 3) {
                task.type = 2
                task.aliasName = getString(R.string.send_notification)
                if (json.containsKey("Data") && json.getIntValue("Data") == 0) {
                    task.task = getString(R.string.msg_center)
                }
            }
            manualTasks.add(task)
        }
        refreshView()
    }

    private fun makeCondition(jsonArr: JSONArray) {

        for (i in 0 until jsonArr.size) {
            var jsonEle = jsonArr.get(i) as JSONObject
            var manualTask = ManualTask()
            var automicCondition = JSON.parseObject(jsonEle.toJSONString(), AutomicCondition::class.java)
            manualTask.condId = automicCondition.condId
            if (automicCondition.condType == 0) {  // 场景
                manualTask.type = 5
                manualTask.productId = automicCondition.property!!.productId
                manualTask.deviceName = automicCondition.property!!.deviceName
                manualTask.aliasName = automicCondition.property!!.aliasName
                manualTask.actionId = automicCondition.property!!.propertyId
                manualTask.taskKey = automicCondition.property!!.value.toString()
                manualTask.task = automicCondition.property!!.value.toString()
                manualTask.iconUrl = automicCondition.property!!.iconUrl
                convertKey2Value(manualTask)

            } else if (automicCondition.condType == 1) {  // 定时
                manualTask.aliasName = getString(R.string.dev_timer)
                manualTask.type = 4
                manualTask.workDayType = TimerExtra.getDayType(automicCondition.timer!!.days)
                manualTask.workDays = automicCondition.timer!!.days
                manualTask.hour = Integer.valueOf(automicCondition.timer!!.timePoint.split(":").get(0))
                manualTask.min = Integer.valueOf(automicCondition.timer!!.timePoint.split(":").get(1))
            }
            manualConditions.add(manualTask)
        }
        refreshView()
    }

    private fun convertKey2Value(task: ManualTask) {
        var products = ArrayList<String>()
        products.add(task.productId)
        HttpRequest.instance.deviceProducts(products, object : MyCallback{
            override fun fail(msg: String?, reqCode: Int) {}

            override fun success(response: BaseResponse, reqCode: Int) {
                if (response.isSuccess()) {

                    var dataTemplate: DataTemplate? = null
                    if (!TextUtils.isEmpty(response.data.toString())) {
                        var products = JSON.parseObject(response.data.toString(), ProductsEntity::class.java)
                        if (products == null || products.Products == null) return

                        for (i in 0 until products!!.Products!!.size) {
                            var productEntity = JSON.parseObject(products!!.Products!!.getString(i), ProductEntity::class.java)
                            if (productEntity.DataTemplate != null) {
                                dataTemplate = JSON.parseObject(productEntity.DataTemplate.toString(), DataTemplate::class.java)
                            }
                        }
                    }

                    if (dataTemplate == null || dataTemplate.properties == null || dataTemplate.properties!!.size == 0) { return }

                    for (i in 0 until dataTemplate.properties!!.size) {
                        var devModeInfo = JSON.parseObject(dataTemplate.properties!!.get(i).toString(), DevModeInfo::class.java)
                        if (devModeInfo.id == task.actionId) {
                            task.taskTip = devModeInfo.name
                            var type = devModeInfo.define!!.get("type")
                            if (type == "bool" || type == "enum") {
                                var mapJson = devModeInfo.define!!.getJSONObject("mapping")
                                for (key in mapJson.keys) {
                                    if (key == task.taskKey) {
                                        task.task = mapJson.getString(key)
                                        break
                                    }
                                }
                            } else if (type == "int" || type == "float") {
                                task.taskKey = ""
                            }
                        }
                    }
                    refreshView()
                }
            }
        })
    }
}