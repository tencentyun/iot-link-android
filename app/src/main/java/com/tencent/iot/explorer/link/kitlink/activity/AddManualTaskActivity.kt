package com.tencent.iot.explorer.link.kitlink.activity

import android.app.Activity
import android.content.Intent
import android.text.TextUtils
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.fastjson.JSON
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.T
import com.tencent.iot.explorer.link.core.auth.entity.DeviceEntity
import com.tencent.iot.explorer.link.customview.dialog.ListOptionsDialog
import com.tencent.iot.explorer.link.kitlink.adapter.ManualTaskAdapter
import com.tencent.iot.explorer.link.kitlink.consts.CommonField
import com.tencent.iot.explorer.link.kitlink.entity.DelayTimeExtra
import com.tencent.iot.explorer.link.core.auth.entity.DevModeInfo
import com.tencent.iot.explorer.link.kitlink.entity.ManualTask
import com.tencent.iot.explorer.link.kitlink.entity.RouteType
import kotlinx.android.synthetic.main.activity_add_manual_task.*
import kotlinx.android.synthetic.main.menu_back_layout.*
import java.util.*
import kotlin.collections.ArrayList

class AddManualTaskActivity : BaseActivity() {

    private var options = ArrayList<String>()
    private var optionsDialog: ListOptionsDialog? = null
    private var manualTasks: MutableList<ManualTask> = LinkedList()
    private var adapter: ManualTaskAdapter? = null

    override fun getContentView(): Int {
        return R.layout.activity_add_manual_task
    }

    override fun initView() {
        tv_title.setText(R.string.add_manual_smart)
        options.add(getString(R.string.control_dev))
        options.add(getString(R.string.delay_time))
        optionsDialog = ListOptionsDialog(this, options)
        optionsDialog?.setOnDismisListener(onItemClickedListener)

        val layoutManager = LinearLayoutManager(this)
        lv_manual_task.setLayoutManager(layoutManager)
        adapter = ManualTaskAdapter(manualTasks)
        adapter?.setOnItemClicked(onListItemClicked)
        lv_manual_task.setAdapter(adapter)

        refreshView()
    }

    private var onListItemClicked = object : ManualTaskAdapter.OnItemClicked {
        override fun onItemClicked(pos: Int, manualTask: ManualTask?) {
            if (manualTask!!.type == 1) {  // 延时任务启动后跳转修改延时的窗口
                var intent = Intent(this@AddManualTaskActivity, DelayTimeActivity::class.java)
                var delayTimeExtra = DelayTimeExtra.convertManualTask2DelayTimeExtra(manualTask!!)
                delayTimeExtra.pos = pos
                intent.putExtra(CommonField.EDIT_EXTRA, JSON.toJSONString(delayTimeExtra))
                startActivityForResult(intent, CommonField.EDIT_DELAY_TIME_REQ_CODE)
            } else if (manualTask!!.type == 0) {  // 编辑设备控制任务
                var intent = Intent(this@AddManualTaskActivity, SmartSelectDevActivity::class.java)
                manualTask.pos = pos
                intent.putExtra(CommonField.EDIT_EXTRA, JSON.toJSONString(manualTask))
                intent.putExtra(CommonField.EXTRA_ROUTE_TYPE, RouteType.EDIT_MANUAL_TASK_ROUTE)
                startActivity(intent)
            }
        }

        override fun onAddTaskClicked() {
            optionsDialog!!.show()
        }

        override fun onDeletedClicked(pos: Int) {
            manualTasks.removeAt(pos)
            adapter?.notifyDataSetChanged()
            refreshView()
        }

    }

    private var onItemClickedListener = ListOptionsDialog.OnDismisListener {
        if (manualTasks.size >= 20) {
            T.show(getString(R.string.task_can_not_more_then_20))
            return@OnDismisListener
        }
        if (it == 0) {
            jumpActivity(SmartSelectDevActivity::class.java)
        } else if (it == 1) {
            var intent = Intent(this, DelayTimeActivity::class.java)
            startActivityForResult(intent, CommonField.ADD_DELAY_TIME_REQ_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CommonField.ADD_DELAY_TIME_REQ_CODE &&  // 添加任务
            resultCode == Activity.RESULT_OK && data != null) {
            var delayTaskStr = data?.getStringExtra(CommonField.DELAY_TIME_TASK)
            var task = JSON.parseObject(delayTaskStr, ManualTask::class.java)
            manualTasks.add(task)

        } else if (requestCode == CommonField.EDIT_DELAY_TIME_REQ_CODE && // 修改任务
            resultCode == Activity.RESULT_OK && data != null) {
            var delayTaskStr = data?.getStringExtra(CommonField.DELAY_TIME_TASK)
            var task = JSON.parseObject(delayTaskStr, ManualTask::class.java)
            if (task.pos < 0) { // 异常情况不更新
                return
            }
            manualTasks.set(task.pos, task)
        }

        adapter?.notifyDataSetChanged()
        refreshView()
    }

    private fun refreshView() {
        if (manualTasks.size <= 0) {
            layout_no_data.visibility = View.VISIBLE
            layout_btn.visibility = View.GONE
        } else {
            layout_no_data.visibility = View.GONE
            layout_btn.visibility = View.VISIBLE
        }
    }

    override fun setListener() {
        iv_back.setOnClickListener { finish() }
        tv_add_now_btn.setOnClickListener{ optionsDialog!!.show() }
        tv_next.setOnClickListener {
            if (manualTasks.size <= 0) {
                T.show(getString(R.string.no_task_to_add))
            } else if (manualTasks[manualTasks!!.lastIndex].type == 1) {
                T.show(getString(R.string.delay_time_can_not_be_last_one))
            } else {

                var intent = Intent(this@AddManualTaskActivity, CompleteTaskInfoActivity::class.java)
                intent.putExtra(CommonField.EXTRA_ALL_MANUAL_TASK, JSON.toJSONString(manualTasks))
                startActivity(intent)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        var str = intent.getStringExtra(CommonField.EXTRA_DEV_MODES)
        var type = intent.getIntExtra(CommonField.EXTRA_ROUTE_TYPE, RouteType.MANUAL_TASK_ROUTE)
        var devModeInfos = JSON.parseArray(str, DevModeInfo::class.java)
        if (devModeInfos == null || devModeInfos.size <= 0) {
            return
        }

        var devDetailStr = intent.getStringExtra(CommonField.EXTRA_DEV_DETAIL)
        if (type == RouteType.MANUAL_TASK_ROUTE && devModeInfos.size + manualTasks.size > 20) {
            T.show(getString(R.string.task_can_not_more_then_20))
            return
        }
        for (i in 0 .. devModeInfos.size - 1) {
            var task = ManualTask()
            task.type = 0
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
            if (type == RouteType.MANUAL_TASK_ROUTE) {
                manualTasks.add(task)
            } else {
                manualTasks.set(devModeInfos.get(i).pos, task)
            }
        }

        adapter?.notifyDataSetChanged()
        refreshView()
    }
}