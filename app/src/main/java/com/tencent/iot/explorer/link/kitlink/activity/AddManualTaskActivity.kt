package com.tencent.iot.explorer.link.kitlink.activity

import android.app.Activity
import android.content.Intent
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.fastjson.JSON
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.T
import com.tencent.iot.explorer.link.customview.dialog.ListOptionsDialog
import com.tencent.iot.explorer.link.kitlink.adapter.IntelligenceAdapter
import com.tencent.iot.explorer.link.kitlink.adapter.ManualTaskAdapter
import com.tencent.iot.explorer.link.kitlink.consts.CommonField
import com.tencent.iot.explorer.link.kitlink.entity.DelayTimeExtra
import com.tencent.iot.explorer.link.kitlink.entity.ManualTask
import kotlinx.android.synthetic.main.activity_add_manual_task.*
import kotlinx.android.synthetic.main.activity_add_manual_task.layout_no_data
import kotlinx.android.synthetic.main.activity_add_manual_task.tv_add_now_btn
import kotlinx.android.synthetic.main.fragment_my_smart.*
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
            if (manualTask!!.type == 0) {  // 延时任务启动后跳转修改延时的窗口
                var intent = Intent(this@AddManualTaskActivity, DelayTimeActivity::class.java)
                var delayTimeExtra = DelayTimeExtra.convertManualTask2DelayTimeExtra(manualTask!!)
                delayTimeExtra.pos = pos
                intent.putExtra(CommonField.EDIT_EXTRA, JSON.toJSONString(delayTimeExtra))
                startActivityForResult(intent, CommonField.EDIT_DELAY_TIME_REQ_CODE)
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
        if (it == 0) {

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
        } else {
            layout_no_data.visibility = View.GONE
        }
    }

    override fun setListener() {
        iv_back.setOnClickListener { finish() }
        tv_add_now_btn.setOnClickListener{ optionsDialog!!.show() }
        tv_next.setOnClickListener {
            if (manualTasks.size <= 0) {
                T.show(getString(R.string.no_task_to_add))
            } else if (manualTasks[manualTasks!!.lastIndex].type == 0) {
                T.show(getString(R.string.delay_time_can_not_be_last_one))
                jumpActivity(CompleteTaskInfoActivity::class.java)  // 调试使用
            } else {
                jumpActivity(CompleteTaskInfoActivity::class.java)
                finish()
            }
        }
    }
}