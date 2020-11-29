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
import com.tencent.iot.explorer.link.customview.dialog.KeyBooleanValue
import com.tencent.iot.explorer.link.customview.dialog.ListOptionsDialog
import com.tencent.iot.explorer.link.kitlink.adapter.ManualTaskAdapter
import com.tencent.iot.explorer.link.kitlink.consts.CommonField
import com.tencent.iot.explorer.link.kitlink.entity.*
import com.tencent.iot.explorer.link.kitlink.util.HttpRequest
import com.tencent.iot.explorer.link.core.auth.callback.MyCallback
import com.tencent.iot.explorer.link.kitlink.util.RequestCode
import com.tencent.iot.explorer.link.kitlink.util.Utils
import kotlinx.android.synthetic.main.activity_complete_task_info.*
import kotlinx.android.synthetic.main.activity_complete_task_info.iv_smart_background
import kotlinx.android.synthetic.main.activity_complete_task_info.layout_smart_name
import kotlinx.android.synthetic.main.activity_complete_task_info.layout_smart_pic
import kotlinx.android.synthetic.main.activity_complete_task_info.tv_unset_tip_1
import kotlinx.android.synthetic.main.activity_complete_task_info.tv_unset_tip_2
import kotlinx.android.synthetic.main.activity_edit_manual_task.*
import kotlinx.android.synthetic.main.menu_back_layout.*
import java.util.*
import kotlin.collections.ArrayList

class EditManualTaskActivity : BaseActivity(), MyCallback {

    private var options = ArrayList<String>()
    private var optionsDialog: ListOptionsDialog? = null
    private var manualTasks: MutableList<ManualTask> = LinkedList()
    private var adapter: ManualTaskAdapter? = null
    @Volatile
    private var smartPicUrl = ""
    @Volatile
    private var smartName = ""
    private var automation: Automation? = null

    override fun getContentView(): Int {
        return R.layout.activity_edit_manual_task
    }

    override fun initView() {
        tv_title.setText(R.string.edit_manual_smart)
        var infoStr = intent.getStringExtra(CommonField.EXTRA_INFO)
        if (TextUtils.isEmpty(infoStr)) return

        automation = JSON.parseObject(infoStr, Automation::class.java)
        if (automation == null) return

        if (automation!!.type == 0) {
            smartName = automation!!.sceneListItem!!.SceneName
            smartPicUrl = automation!!.sceneListItem!!.SceneIcon
        } else {
            return
        }

        options.add(getString(R.string.control_dev))
        options.add(getString(R.string.delay_time))
        optionsDialog = ListOptionsDialog(this, options)
        optionsDialog?.setOnDismisListener(onItemClickedListener)

        val layoutManager = LinearLayoutManager(this)
        lv_manual_task.setLayoutManager(layoutManager)
        adapter = ManualTaskAdapter(manualTasks)
        adapter?.setOnItemClicked(onListItemClicked)
        lv_manual_task.setAdapter(adapter)

        loadAllTaskData()
        refreshView()
        loadView()
    }

    private fun loadAllTaskData() {

        for (i in 0 until automation!!.sceneListItem!!.Actions!!.size) {
            var task = ManualTask()
            var json = automation!!.sceneListItem!!.Actions!!.get(i) as JSONObject
            if (json == null) {
                continue
            }

            if (json.getIntValue("ActionType") == 1) {
                var time = json.getLongValue("Data")
                task.type = 1
                task.hour = time.toInt() / 3600
                var tmpSeconds = time.toInt() % 3600
                task.min = tmpSeconds / 60
                task.aliasName = getString(R.string.delay_time)
            } else {
                if (json.containsKey("ActionType")) {
                    task.type = json.getIntValue("ActionType")
                }
                if (json.containsKey("DeviceName")) {
                    task.deviceName = json.getString("DeviceName")
                }
                if (json.containsKey("ProductId")) {
                    task.productId = json.getString("ProductId")
                }
                if (json.containsKey("IconUrl")) {
                    task.iconUrl = json.getString("IconUrl")
                }
                var value = json.getString("Data")
                var dataJson = JSON.parseObject(value)
                for (keys in dataJson.keys) {
                    task.actionId = keys
                    task.taskKey = dataJson.getIntValue(keys).toString()
                    task.task = dataJson.getIntValue(keys).toString()
                }

                loadTaskVauleInfo(task)
            }
            manualTasks.add(task)
        }
        adapter?.notifyDataSetChanged()
    }

    private fun loadTaskVauleInfo(task: ManualTask) {
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
                        Log.e("XXX", "dataTemplate.properties!!.get(i).toString() " + dataTemplate.properties!!.get(i).toString())
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
                                if (devModeInfo.define!!.containsKey("unit")) {
                                    task.unit = devModeInfo.define!!.getString("unit")
                                }
                            }
                        }
                    }
                    adapter?.notifyDataSetChanged()
                }
            }
        })
    }

    fun loadView() {
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

    private var onListItemClicked = object : ManualTaskAdapter.OnItemClicked {
        override fun onItemClicked(pos: Int, manualTask: ManualTask?) {
            if (manualTask!!.type == 1) {  // 延时任务启动后跳转修改延时的窗口
                var intent = Intent(this@EditManualTaskActivity, DelayTimeActivity::class.java)
                var delayTimeExtra = DelayTimeExtra.convertManualTask2DelayTimeExtra(manualTask!!)
                delayTimeExtra.pos = pos
                intent.putExtra(CommonField.EDIT_EXTRA, JSON.toJSONString(delayTimeExtra))
                startActivityForResult(intent, CommonField.EDIT_DELAY_TIME_REQ_CODE)

            } else if (manualTask!!.type == 0) {  // 编辑设备控制任务
                var intent = Intent(this@EditManualTaskActivity, SmartSelectDevActivity::class.java)
                manualTask.pos = pos
                intent.putExtra(CommonField.EDIT_EXTRA, JSON.toJSONString(manualTask))
                intent.putExtra(CommonField.EXTRA_ROUTE_TYPE, RouteType.EDIT_MANUAL_TASK_DETAIL_ROUTE)
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
            var intent = Intent(this, SmartSelectDevActivity::class.java)
            intent.putExtra(CommonField.EXTRA_ROUTE_TYPE, RouteType.ADD_MANUAL_TASK_DETAIL_ROUTE)
            startActivity(intent)
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

        if (requestCode == CommonField.REQUEST_PIC_REQ_CODE &&  // 添加任务
            resultCode == Activity.RESULT_OK && data != null) {
            var picUrl = data?.getStringExtra(CommonField.EXTRA_PIC_URL)
            smartPicUrl = picUrl
        } else if (requestCode == CommonField.REQUEST_TASK_NAME_REQ_CODE &&  // 添加任务
            resultCode == Activity.RESULT_OK && data != null) {
            var name = data?.getStringExtra(CommonField.EXYRA_TASK_NAME)
            smartName = name.trim()
        }

        loadView()
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
            } else {
                saveManualTaskDetail()
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

    private fun saveManualTaskDetail() {
        var sceneEntity = SceneEntity()
        sceneEntity.familyId = App.data.getCurrentFamily().FamilyId
        sceneEntity.sceneIcon = this.smartPicUrl
        sceneEntity.sceneName = this.smartName
        var jsonArr = JSONArray()
        for (i in 0 until manualTasks!!.size) {
            var jsonObj = JSONObject()
            jsonObj.put("ActionType", manualTasks!!.get(i).type)
            if (manualTasks!!.get(i).type == 1) {
                jsonObj.put("Data", manualTasks!!.get(i).hour * 60 * 60 + manualTasks!!.get(i).min * 60) // 单位是s
            } else {
                jsonObj.put("ProductId", manualTasks!!.get(i).productId)
                jsonObj.put("DeviceName", manualTasks!!.get(i).deviceName)
                var jsonAction = JSONObject()
                // 存在 key 值的使用 key，不存在 key 的使用 value，进度没有 key，bool 和 enum 存在 key
                if (TextUtils.isEmpty(manualTasks!!.get(i).taskKey)) {
                    jsonAction.put(manualTasks!!.get(i).actionId, manualTasks!!.get(i).task)
                } else {
                    jsonAction.put(manualTasks!!.get(i).actionId, manualTasks!!.get(i).taskKey)
                }
                jsonObj.put("Data", jsonAction.toJSONString())
            }
            jsonArr.add(jsonObj)
        }
        sceneEntity.actions = jsonArr
        sceneEntity.sceneId = automation!!.id
        HttpRequest.instance.updateManualTask(sceneEntity, this)
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
        if (type == RouteType.ADD_MANUAL_TASK_DETAIL_ROUTE && devModeInfos.size + manualTasks.size > 20) {
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
            task.taskKey = devModeInfos.get(i).key
            task.unit = devModeInfos.get(i).unit
            if (type == RouteType.ADD_MANUAL_TASK_DETAIL_ROUTE) {
                manualTasks.add(task)
            } else if (type == RouteType.EDIT_MANUAL_TASK_DETAIL_ROUTE) {
                manualTasks.set(devModeInfos.get(i).pos, task)
            }
        }

        adapter?.notifyDataSetChanged()
        refreshView()
    }

    override fun fail(msg: String?, reqCode: Int) {
        T.show(msg)
    }

    override fun success(response: BaseResponse, reqCode: Int) {
        when(reqCode) {
            RequestCode.update_manual_task -> {
                if (response.isSuccess()) {
                    T.show(getString(R.string.success_update))
                    Utils.sendRefreshBroadcast(this@EditManualTaskActivity)
                    finish()
                } else {
                    T.show(response.msg)
                }
            }
        }
    }
}