package com.tencent.iot.explorer.link.kitlink.fragment

import android.content.Intent
import android.os.Handler
import android.util.Log
import android.view.View
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
import com.tencent.iot.explorer.link.kitlink.activity.EditManualTaskActivity
import com.tencent.iot.explorer.link.kitlink.adapter.IntelligenceAdapter
import com.tencent.iot.explorer.link.kitlink.consts.CommonField
import com.tencent.iot.explorer.link.kitlink.entity.Automation
import com.tencent.iot.explorer.link.kitlink.response.AutomationListResponse
import com.tencent.iot.explorer.link.kitlink.response.SceneListResponse
import com.tencent.iot.explorer.link.kitlink.util.HttpRequest
import com.tencent.iot.explorer.link.kitlink.util.MyCallback
import com.tencent.iot.explorer.link.kitlink.util.RequestCode
import com.tencent.iot.explorer.link.mvp.IPresenter
import kotlinx.android.synthetic.main.fragment_my_smart.*

/**
 * 我的智能
 */
class MySmartFragment() : BaseFragment(), View.OnClickListener, MyCallback {

    private var addDialog: ListOptionsDialog? = null
    private var options = ArrayList<String>()
    private var adapter: IntelligenceAdapter? = null
    private var automicList: MutableList<Automation> = ArrayList()
    private var manualList: MutableList<Automation> = ArrayList()
    private var handler: Handler = Handler()
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

        //下拉刷新及上拉加载
        smart_refreshLayout.setEnableRefresh(true)  // 禁止上拉刷新
        smart_refreshLayout.setRefreshHeader(ClassicsHeader(context)) //https://github.com/scwang90/SmartRefreshLayout
        smart_refreshLayout.setEnableLoadMore(false)
        smart_refreshLayout.setRefreshFooter(ClassicsFooter(context))
        smart_refreshLayout.setOnRefreshListener(onRefreshListener)
        smart_refreshLayout.setOnLoadMoreListener(onLoadMoreListener)

        tv_add_now_btn.setOnClickListener(this)
    }

    private var onItemClicked = object: IntelligenceAdapter.OnItemClicked {
        override fun onSwitchStatus(postion: Int, isChecked: Boolean, automation: Automation?) {

        }

        override fun onItemClicked(automation: Automation?) {
            var intent = Intent(context, EditManualTaskActivity::class.java)
            intent.putExtra(CommonField.EXTRA_INFO, JSON.toJSONString(automation))
            startActivity(intent)
        }

        override fun onRunTaskClicked(automation: Automation?) {
            if (automation!!.type == 0) {
                HttpRequest.instance.runManualTask(automation!!.id, this@MySmartFragment)
            }
        }

        override fun onItemDeleteClicked(postion: Int, automation: Automation?) {
            if (automation!!.type == 0) {
                HttpRequest.instance.delManualTask(automation!!.id, object: MyCallback{
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
            } else if (automation!!.type == 1) {
                HttpRequest.instance.delAutomicTask(automation!!.id, object: MyCallback{
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
        }
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

    override fun onResume() {
        super.onResume()
        loadAlldata()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {

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

                            automicList.add(automation)
                            automicListLoadOver = true
                            Log.e("XXX", "bbbbbbb")
                            loadDataOver()
                        }
                    }
                }
            }

            RequestCode.query_all_manual_task -> {
                if (response.code == 0) {
                    Log.e("XXX", "resp.data " + response.data)
                    var sceneListResponse = JSON.parseObject(response.data.toString(), SceneListResponse::class.java)
                    if (sceneListResponse.SceneList != null && sceneListResponse.SceneList.size > 0) {
                        for (i in 0 until sceneListResponse.SceneList.size) {
                            var automation = Automation()
                            automation.Icon = sceneListResponse.SceneList.get(i).SceneIcon
                            automation.Name = sceneListResponse.SceneList.get(i).SceneName
                            automation.actions = sceneListResponse.SceneList.get(i).Actions
                            automation.id = sceneListResponse.SceneList.get(i).SceneId
                            automation.sceneListItem = sceneListResponse.SceneList.get(i)
                            manualList.add(automation)
                        }
                        if (manualList.size < sceneListResponse.Total) {
                            manualListOffset = manualList.size
                            HttpRequest.instance.queryManualTask(App.data.getCurrentFamily().FamilyId, manualListOffset, this)
                        } else {
                            manualListLoadOver = true
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
        if (automicListLoadOver && manualListLoadOver && smart_refreshLayout.isRefreshing) {
            smart_refreshLayout.finishRefresh()
        }
        adapter?.manualList = manualList
        adapter?.automicList = automicList
        adapter?.notifyDataSetChanged()
        if (adapter?.list == null || adapter?.list?.size == 0) {
            layout_no_data.visibility = View.VISIBLE
        } else {
            layout_no_data.visibility = View.GONE
        }
    }

}