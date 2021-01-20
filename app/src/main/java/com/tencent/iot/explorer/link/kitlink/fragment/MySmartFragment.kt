package com.tencent.iot.explorer.link.kitlink.fragment

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Handler
import android.util.Log
import android.view.View
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.fastjson.JSON
import com.scwang.smart.refresh.footer.ClassicsFooter
import com.scwang.smart.refresh.header.ClassicsHeader
import com.scwang.smart.refresh.layout.listener.OnLoadMoreListener
import com.scwang.smart.refresh.layout.listener.OnRefreshListener
import com.tencent.iot.explorer.link.App
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.T
import com.tencent.iot.explorer.link.core.auth.response.BaseResponse
import com.tencent.iot.explorer.link.customview.dialog.ListOptionsDialog
import com.tencent.iot.explorer.link.kitlink.activity.AddAutoicTaskActivity
import com.tencent.iot.explorer.link.kitlink.activity.AddManualTaskActivity
import com.tencent.iot.explorer.link.kitlink.activity.EditAutoicTaskActivity
import com.tencent.iot.explorer.link.kitlink.activity.EditManualTaskActivity
import com.tencent.iot.explorer.link.kitlink.adapter.IntelligenceAdapter
import com.tencent.iot.explorer.link.kitlink.consts.CommonField
import com.tencent.iot.explorer.link.kitlink.entity.Automation
import com.tencent.iot.explorer.link.kitlink.response.AutomationListResponse
import com.tencent.iot.explorer.link.kitlink.response.SceneListResponse
import com.tencent.iot.explorer.link.kitlink.util.HttpRequest
import com.tencent.iot.explorer.link.core.auth.callback.MyCallback
import com.tencent.iot.explorer.link.customview.dialog.TipDialog
import com.tencent.iot.explorer.link.kitlink.util.RequestCode
import com.tencent.iot.explorer.link.mvp.IPresenter
import kotlinx.android.synthetic.main.fragment_my_smart.*
import java.util.concurrent.CopyOnWriteArrayList

/**
 * 我的智能
 */
class MySmartFragment() : BaseFragment(), View.OnClickListener, MyCallback {

    private var addDialog: ListOptionsDialog? = null
    private var options = ArrayList<String>()
    private var adapter: IntelligenceAdapter? = null
    private var automicList: MutableList<Automation> = CopyOnWriteArrayList()
    private var manualList: MutableList<Automation> = CopyOnWriteArrayList()
    private var manualListOffset = 0
    private var automicListOffset = 0
    @Volatile
    private var manualListLoadOver = false
    @Volatile
    private var automicListLoadOver = false

    override fun getPresenter(): IPresenter? {
        return null
    }

    override fun getContentView(): Int {
        return R.layout.fragment_my_smart
    }

    override fun startHere(view: View) {
        options.add(getString(R.string.smart_option_1))
        options.add(getString(R.string.smart_option_2))
        addDialog = ListOptionsDialog(context, options)
        addDialog?.setOnDismisListener(onItemClickedListener)

        val layoutManager = LinearLayoutManager(context)
        lv_all_smart.setLayoutManager(layoutManager)
        adapter = IntelligenceAdapter()
        adapter?.setOnItemClicked(onItemClicked)
        lv_all_smart.setAdapter(adapter)
        smart_refreshLayout.setEnableRefresh(true)
        smart_refreshLayout.setRefreshHeader(ClassicsHeader(context))
        smart_refreshLayout.setEnableLoadMore(false)
        smart_refreshLayout.setRefreshFooter(ClassicsFooter(context))
        smart_refreshLayout.setOnRefreshListener(onRefreshListener)
        smart_refreshLayout.setOnLoadMoreListener(onLoadMoreListener)

        tv_add_now_btn.setOnClickListener(this)
        registBrodcast()
        loadAlldata()
    }

    private fun registBrodcast() {
        var broadcastManager = LocalBroadcastManager.getInstance(context!!)
        var intentFilter = IntentFilter()
        intentFilter.addAction("android.intent.action.CART_BROADCAST")
        var recevier = object: BroadcastReceiver(){
            override fun onReceive(context: Context?, intent: Intent?) {
                var refreshTag = intent?.getIntExtra(CommonField.EXTRA_REFRESH, 0);
                if (refreshTag != 0){
                    loadAlldata()
                }
            }
        }
        broadcastManager.registerReceiver(recevier, intentFilter);
    }

    private var onItemClicked = object: IntelligenceAdapter.OnItemClicked {
        override fun onSwitchStatus(postion: Int, isChecked: Boolean, automation: Automation?) {
            if (!isChecked) {
                switchStatus(automation!!.id, 0, postion)
            } else {
                switchStatus(automation!!.id, 1, postion)
            }
        }

        override fun onItemClicked(automation: Automation?) {
            if (automation?.type == 0) {
                if (automation.flag == 0) {
                    var intent = Intent(context, EditManualTaskActivity::class.java)
                    intent.putExtra(CommonField.EXTRA_INFO, JSON.toJSONString(automation))
                    startActivity(intent)
                } else {
                    var dialog = TipDialog(context)
                    dialog.show()
                    dialog.setOnDismisListener {
                        automation.actions?.clear()
                        var intent = Intent(context, EditManualTaskActivity::class.java)
                        intent.putExtra(CommonField.EXTRA_INFO, JSON.toJSONString(automation))
                        startActivity(intent)
                    }
                }

            } else if (automation?.type == 1) {
                var intent = Intent(context, EditAutoicTaskActivity::class.java)
                intent.putExtra(CommonField.EXTRA_INFO, JSON.toJSONString(automation))
                startActivity(intent)
            }
        }

        override fun onRunTaskClicked(automation: Automation?) {
            if (automation!!.type == 0) {
                if (automation.flag == 0) {
                    HttpRequest.instance.runManualTask(automation!!.id, this@MySmartFragment)
                } else {
                    var dialog = TipDialog(context)
                    dialog.show()
                    dialog.setOnDismisListener {
                        automation.actions?.clear()
                        var intent = Intent(context, EditManualTaskActivity::class.java)
                        intent.putExtra(CommonField.EXTRA_INFO, JSON.toJSONString(automation))
                        startActivity(intent)
                    }
                }
            }
        }

        override fun onItemDeleteClicked(postion: Int, automation: Automation?) {
            if (automation!!.type == 0) {
                deleteManualTask(automation!!.id, postion)
            } else if (automation!!.type == 1) {
                deleteAutomicTask(automation!!.id, postion)
            }
        }
    }

    private fun switchStatus(automationId: String, status: Int, postion: Int) {
        HttpRequest.instance.updateAutomicTaskStatus(automationId, status, object: MyCallback{
            override fun fail(msg: String?, reqCode: Int) {
                T.show(msg)
                if (status == 0) {
                    adapter?.setItemStatus(postion, 1)
                } else {
                    adapter?.setItemStatus(postion, 0)
                }
                adapter?.notifyDataSetChanged()
            }

            override fun success(response: BaseResponse, reqCode: Int) {
                when(reqCode) {
                    RequestCode.update_automic_task_status -> {
                        if (response.code == 0) {
                            if (status != 1) {
                                T.show(getString(R.string.success_update_close))
                            } else {
                                T.show(getString(R.string.success_update_open))
                            }
                            adapter?.setItemStatus(postion, status)
                            adapter?.notifyDataSetChanged();
                        } else {
                            if (status == 0) {
                                adapter?.setItemStatus(postion, 1)
                            } else {
                                adapter?.setItemStatus(postion, 0)
                            }
                            adapter?.notifyDataSetChanged()
                            T.show(response.msg)
                        }
                    }
                }
            }
        })
    }

    private fun deleteManualTask(manualTaskId: String, postion: Int) {
        HttpRequest.instance.delManualTask(manualTaskId, object: MyCallback{
            override fun fail(msg: String?, reqCode: Int) {
                T.show(msg)
            }

            override fun success(response: BaseResponse, reqCode: Int) {
                when(reqCode) {
                    RequestCode.del_manual_task -> {
                        if (response.code == 0) {
                            T.show(getString(R.string.delete_success))
                            adapter?.removeItem(postion);
                            adapter?.notifyDataSetChanged();
                        } else {
                            T.show(response.msg)
                        }
                    }
                }
            }
        })
    }

    private fun deleteAutomicTask(automationId: String, postion: Int) {
        HttpRequest.instance.delAutomicTask(automationId, object: MyCallback{
            override fun fail(msg: String?, reqCode: Int) {
                T.show(msg)
            }

            override fun success(response: BaseResponse, reqCode: Int) {
                when(reqCode) {
                    RequestCode.del_automic_task -> {
                        if (response.code == 0) {
                            T.show(getString(R.string.delete_success))
                            adapter?.removeItem(postion);
                            adapter?.notifyDataSetChanged();
                        } else {
                            T.show(response.msg)
                        }
                    }
                }
            }
        })
    }

    private var onRefreshListener = OnRefreshListener {
        loadAlldata()
    }

    private var onLoadMoreListener = OnLoadMoreListener {}

    private var onItemClickedListener = ListOptionsDialog.OnDismisListener {
        if (it == 0) {
            jumpActivity(AddManualTaskActivity::class.java)
        } else if (it == 1) {
            jumpActivity(AddAutoicTaskActivity::class.java)
        }
    }

    // 刷新列表数据，从而触发页面的更新
    private fun refreshListData() {
        HttpRequest.instance.queryManualTask(App.data.getCurrentFamily().FamilyId, manualListOffset, this)
        HttpRequest.instance.queryAutomicTask(App.data.getCurrentFamily().FamilyId, this)
    }

    private fun loadAlldata() {
        manualListOffset = 0
        automicListOffset = 0
        manualListLoadOver = false
        automicListLoadOver = false
        manualList.clear()
        automicList.clear()
        refreshListData()
    }

    override fun onClick(v: View?) {
        when(v) {
            tv_add_now_btn -> {
                addDialog?.show()
            }
        }
    }

    override fun fail(msg: String?, reqCode: Int) {
        T.show(msg)
        when(reqCode) {
            RequestCode.query_all_automic_task -> {
                automicListLoadOver = true
                loadDataOver()
            }
            RequestCode.query_all_manual_task -> {
                manualList.clear()
                manualListLoadOver = true
                loadDataOver()
            }
        }
    }

    override fun success(response: BaseResponse, reqCode: Int) {
        when(reqCode) {
            RequestCode.run_manual_task -> {
                if (response.code == 0) {
                    T.show(getString(R.string.run_manual_task_success))
                } else {
                    T.show(response.msg)
                }
            }

            RequestCode.query_all_automic_task -> {
                if (response.code == 0) {
                    var automationListResponse = JSON.parseObject(response.data.toString(), AutomationListResponse::class.java)
                    if (automationListResponse.List != null && automationListResponse.List.size > 0) {
                        for (i in 0 until automationListResponse.List.size) {
                            var automation = Automation()
                            automation.type = 1
                            automation.Icon = automationListResponse.List.get(i).Icon
                            automation.Name = automationListResponse.List.get(i).Name
                            automation.id = automationListResponse.List.get(i).AutomationId
                            automation.Status = automationListResponse.List.get(i).Status
                            automicList.add(automation)
                        }
                    }
                } else {
                    T.show(response.msg)
                }

                // 自动智能列表没有分页，只要查询有结果就结束刷新
                automicListLoadOver = true
                loadDataOver()
            }

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
                            automation.flag = sceneListResponse.SceneList.get(i).Flag
                            automation.sceneListItem = sceneListResponse.SceneList.get(i)
                            manualList.add(automation)
                        }

                        //手动智能是分页查询，还有数据继续调用查询接口
                        if (manualList.size < sceneListResponse.Total) {
                            manualListOffset = manualList.size
                            HttpRequest.instance.queryManualTask(App.data.getCurrentFamily().FamilyId, manualListOffset, this)
                        }
                    }
                } else {
                    T.show(response.msg)
                }
                manualListLoadOver = true
                loadDataOver()
            }
        }
    }

    private fun loadDataOver() {
        if (automicListLoadOver && manualListLoadOver &&
            smart_refreshLayout != null && smart_refreshLayout.isRefreshing) {
            smart_refreshLayout.finishRefresh()
        }
        adapter?.manualList = manualList
        adapter?.automicList = automicList
        adapter?.notifyDataSetChanged()
        if (adapter?.list == null || adapter?.list?.size == 0) {
            layout_no_data?.visibility = View.VISIBLE
        } else {
            layout_no_data?.visibility = View.GONE
        }
    }

}