package com.tencent.iot.explorer.link.kitlink.activity

import android.app.Activity
import android.content.Intent
import android.os.Handler
import android.text.TextUtils
import android.view.View
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.squareup.picasso.Picasso
import com.tencent.iot.explorer.link.App
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.T
import com.tencent.iot.explorer.link.core.auth.response.BaseResponse
import com.tencent.iot.explorer.link.customview.dialog.SuccessToastDialog
import com.tencent.iot.explorer.link.kitlink.consts.CommonField
import com.tencent.iot.explorer.link.kitlink.entity.AutomicTaskEntity
import com.tencent.iot.explorer.link.kitlink.entity.ManualTask
import com.tencent.iot.explorer.link.kitlink.entity.RouteType
import com.tencent.iot.explorer.link.kitlink.entity.SceneEntity
import com.tencent.iot.explorer.link.kitlink.util.HttpRequest
import com.tencent.iot.explorer.link.core.auth.callback.MyCallback
import com.tencent.iot.explorer.link.kitlink.util.Utils
import kotlinx.android.synthetic.main.activity_complete_task_info.*
import kotlinx.android.synthetic.main.menu_back_layout.*


class CompleteTaskInfoActivity : BaseActivity(),MyCallback {

    @Volatile
    private var smartPicUrl = ""
    @Volatile
    private var smartName = ""
    private var handler = Handler()
    private var taskList: MutableList<ManualTask>? = null
    private var routeType = RouteType.MANUAL_TASK_ROUTE  // 0 手动路由至此窗口   1 2 自动路由至此窗口
    private var automicTaskEntity: AutomicTaskEntity? = null

    override fun getContentView(): Int {
        return R.layout.activity_complete_task_info
    }

    override fun initView() {
        tv_title.setText(R.string.complete_task_info)
        routeType = intent.getIntExtra(CommonField.EXTRA_ROUTE_TYPE, RouteType.MANUAL_TASK_ROUTE)
        if (routeType == RouteType.MANUAL_TASK_ROUTE) {
            var str = intent.getStringExtra(CommonField.EXTRA_ALL_MANUAL_TASK)
            taskList = JSON.parseArray(str, ManualTask::class.java)
        } else {
            var str = intent.getStringExtra(CommonField.EXTRA_ALL_AUTOMIC_TASK)
            automicTaskEntity = JSON.parseObject(str, AutomicTaskEntity::class.java)
        }
        loadView()
    }

    override fun setListener() {
        iv_back.setOnClickListener { finish() }
        tv_cancel.setOnClickListener { finish() }
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
        tv_ok.setOnClickListener {

            if (TextUtils.isEmpty(smartPicUrl)) {
                T.show(getString(R.string.please_input_smart_pic))
                return@setOnClickListener
            }

            if (TextUtils.isEmpty(smartName)) {
                T.show(getString(R.string.please_input_smart_name))
                return@setOnClickListener
            }

            if (routeType == RouteType.MANUAL_TASK_ROUTE && taskList == null) {
                return@setOnClickListener
            }

            if (routeType == RouteType.MANUAL_TASK_ROUTE) {
                createManualTask()
            } else {
                createAutomicTask()
            }
        }
    }

    private fun createAutomicTask() {
        automicTaskEntity?.familyId = App.data.getCurrentFamily().FamilyId
        automicTaskEntity?.icon = this.smartPicUrl
        automicTaskEntity?.name = this.smartName
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
                    propertyJson.put("Op", automicTaskEntity?.conditionsItem?.get(i)?.op)
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

        HttpRequest.instance.createAutomicTask(automicTaskEntity!!, this)

    }

    private fun createManualTask() {
        var sceneEntity = SceneEntity()
        sceneEntity.familyId = App.data.getCurrentFamily().FamilyId
        sceneEntity.sceneIcon = this.smartPicUrl
        sceneEntity.sceneName = this.smartName
        var jsonArr = JSONArray()
        for (i in 0 until taskList!!.size) {
            var jsonObj = JSONObject()
            jsonObj.put("ActionType", taskList!!.get(i).type)
            if (taskList!!.get(i).type == 1) {
                jsonObj.put("Data", taskList!!.get(i).hour * 60 * 60 + taskList!!.get(i).min * 60) // 单位是s
            } else {
                jsonObj.put("ProductId", taskList!!.get(i).productId)
                jsonObj.put("DeviceName", taskList!!.get(i).deviceName)
                var jsonAction = JSONObject()
                // 存在 key 值的使用 key，不存在 key 的使用 value，进度没有 key，bool 和 enum 存在 key
                if (TextUtils.isEmpty(taskList!!.get(i).taskKey)) {
                    jsonAction.put(taskList!!.get(i).actionId, taskList!!.get(i).task)
                } else {
                    jsonAction.put(taskList!!.get(i).actionId, taskList!!.get(i).taskKey)
                }
                jsonObj.put("Data", jsonAction.toJSONString())
            }
            jsonArr.add(jsonObj)
        }
        sceneEntity.actions = jsonArr

        HttpRequest.instance.createManualTask(sceneEntity, this)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
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

    override fun fail(msg: String?, reqCode: Int) {
        T.show(msg)
    }

    override fun success(response: BaseResponse, reqCode: Int) {
        if (response.code == 0) {

            var dialog = SuccessToastDialog(this)
            dialog.show()
            Thread {
                Thread.sleep(1000)
                handler.post {
                    Utils.sendRefreshBroadcast(this@CompleteTaskInfoActivity)
                    dialog.dismiss()
                    jumpActivity(MainActivity::class.java)
                }
            }.start()
        }
    }
}