package com.tencent.iot.explorer.link.kitlink.activity

import android.content.Intent
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.fastjson.JSON
import com.tencent.iot.explorer.link.App
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.T
import com.tencent.iot.explorer.link.core.auth.response.BaseResponse
import com.tencent.iot.explorer.link.kitlink.adapter.SelectManualTaskAdapter
import com.tencent.iot.explorer.link.kitlink.consts.CommonField
import com.tencent.iot.explorer.link.kitlink.entity.Automation
import com.tencent.iot.explorer.link.kitlink.entity.ManualTask
import com.tencent.iot.explorer.link.kitlink.response.SceneListResponse
import com.tencent.iot.explorer.link.kitlink.util.HttpRequest
import com.tencent.iot.explorer.link.kitlink.util.MyCallback
import com.tencent.iot.explorer.link.kitlink.util.RequestCode
import kotlinx.android.synthetic.main.activity_delay_time.*
import kotlinx.android.synthetic.main.activity_select_manual_task.*
import kotlinx.android.synthetic.main.activity_select_manual_task.tv_ok
import kotlinx.android.synthetic.main.activity_set_notification_type.tv_cancel
import kotlinx.android.synthetic.main.menu_back_layout.*

class SelectManualTaskActivity : BaseActivity() , MyCallback {

    private var manualListOffset = 0
    private var manualList: MutableList<Automation> = ArrayList()
    private var adapter: SelectManualTaskAdapter? = null

    override fun getContentView(): Int {
        return R.layout.activity_select_manual_task
    }

    override fun initView() {
        iv_back.setColorFilter(resources.getColor(R.color.black_333333))
        tv_title.text = getString(R.string.select_manual_smart)

        adapter = SelectManualTaskAdapter(manualList)
        adapter?.setOnItemClicked(onItemClicked)
        val layoutManager = LinearLayoutManager(this)
        lv_manual_task.setLayoutManager(layoutManager)
        lv_manual_task.setAdapter(adapter)
        tv_select_all_btn.setText(R.string.select_all)

        HttpRequest.instance.queryManualTask(App.data.getCurrentFamily().FamilyId, manualListOffset, this)
    }

    private var onItemClicked = object: SelectManualTaskAdapter.OnItemClicked {
        override fun onItemClicked(pos: Int, url: Automation) {
            changeBtnStatus()
        }
    }

    private fun changeBtnStatus() {
        if (adapter?.index?.size == manualList.size) {
            tv_select_all_btn.setText(R.string.disselect_all)
        } else {
            tv_select_all_btn.setText(R.string.select_all)
        }
        adapter?.notifyDataSetChanged()
    }

    override fun setListener() {
        iv_back.setOnClickListener { finish() }
        tv_cancel.setOnClickListener { finish() }
        tv_select_all_btn.setOnClickListener {
            if (tv_select_all_btn.text.toString().equals(getString(R.string.disselect_all))) {
                adapter?.index?.clear()
                adapter?.notifyDataSetChanged()
            } else if (tv_select_all_btn.text.toString().equals(getString(R.string.select_all))) {
                for (i in 0 until manualList.size) {
                    adapter?.index?.add(i)
                }
                adapter?.notifyDataSetChanged()
            }
            changeBtnStatus()
        }
        tv_ok.setOnClickListener {
            val intent = Intent()
            var retList = ArrayList<ManualTask>()
            for (i in 0 until adapter?.index!!.size) {
                var tmp = manualList.get(adapter?.index?.elementAt(i) as Int)
                var manualTask = ManualTask()
                manualTask.type = 3
                manualTask.aliasName = getString(R.string.sel_manual_task)
                manualTask.task = tmp.Name
                manualTask.sceneId = tmp.id
                retList.add(manualTask)
            }
            intent.putExtra(CommonField.EXTRA_ADD_MANUAL_TASK, JSON.toJSONString(retList))
            setResult(RESULT_OK, intent)
            finish()
        }
    }

    override fun fail(msg: String?, reqCode: Int) {
        T.show(msg)
    }

    override fun success(response: BaseResponse, reqCode: Int) {
        when(reqCode) {
            RequestCode.query_all_manual_task -> {
                if (response.code == 0) {
                    var sceneListResponse = JSON.parseObject(response.data.toString(), SceneListResponse::class.java)
                    if (sceneListResponse.SceneList != null && sceneListResponse.SceneList.size > 0) {
                        for (i in 0 until sceneListResponse.SceneList.size) {
                            var automation = Automation()
                            automation.Icon = sceneListResponse.SceneList.get(i).SceneIcon
                            automation.Name = sceneListResponse.SceneList.get(i).SceneName
                            automation.actions = sceneListResponse.SceneList.get(i).Actions
                            automation.id = sceneListResponse.SceneList.get(i).SceneId
                            manualList.add(automation)
                        }
                        if (manualList.size < sceneListResponse.Total) {
                            manualListOffset = manualList.size
                            HttpRequest.instance.queryManualTask(App.data.getCurrentFamily().FamilyId, manualListOffset, this)
                        } else {
                            loadDataOver()
                        }

                    }
                } else {
                    T.show(response.msg)
                }
            }
        }
    }

    private fun loadDataOver() {
        adapter?.notifyDataSetChanged()
    }

}
