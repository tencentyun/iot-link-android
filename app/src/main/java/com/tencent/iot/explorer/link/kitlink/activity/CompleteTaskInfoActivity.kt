package com.tencent.iot.explorer.link.kitlink.activity

import android.app.Activity
import android.content.Intent
import android.os.Handler
import android.text.TextUtils
import android.util.Log
import android.view.View
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
import com.tencent.iot.explorer.link.kitlink.entity.ManualTask
import com.tencent.iot.explorer.link.kitlink.entity.SceneEntity
import com.tencent.iot.explorer.link.kitlink.util.HttpRequest
import com.tencent.iot.explorer.link.kitlink.util.MyCallback
import kotlinx.android.synthetic.main.activity_complete_task_info.*
import kotlinx.android.synthetic.main.menu_back_layout.*


class CompleteTaskInfoActivity : BaseActivity(),MyCallback {

    @Volatile
    private var smartPicUrl = ""
    @Volatile
    private var smartName = ""
    private var handler = Handler()
    private var taskList: MutableList<ManualTask>? = null

    override fun getContentView(): Int {
        return R.layout.activity_complete_task_info
    }

    override fun initView() {
        tv_title.setText(R.string.complete_task_info)
        var str = intent.getStringExtra(CommonField.EXTRA_ALL_MANUAL_TASK)
        taskList = JSON.parseArray(str, ManualTask::class.java)
        loadView()
    }

    override fun setListener() {
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
        tv_ok.setOnClickListener {

            if (TextUtils.isEmpty(smartPicUrl)) {
                T.show(getString(R.string.please_input_smart_pic))
                return@setOnClickListener
            }

            if (TextUtils.isEmpty(smartName)) {
                T.show(getString(R.string.please_input_smart_name))
                return@setOnClickListener
            }

            if (taskList == null) {
                return@setOnClickListener
            }

            createManualTask()
        }
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
                    dialog.dismiss()
                    jumpActivity(MainActivity::class.java)
                }
            }.start()
        }
    }
}