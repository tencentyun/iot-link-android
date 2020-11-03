package com.tencent.iot.explorer.link.kitlink.fragment

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
import com.tencent.iot.explorer.link.kitlink.adapter.IntelligenceAdapter
import com.tencent.iot.explorer.link.kitlink.entity.Automation
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
        override fun onItemClicked(automation: Automation?) {

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
            RequestCode.query_all_manual_task -> {
                if (response.code == 0) {

                    var sceneListResponse = JSON.parseObject(response.data.toString(), SceneListResponse::class.java)
                    if (sceneListResponse.SceneList != null && sceneListResponse.SceneList.size > 0) {
                        for (i in 0 until sceneListResponse.SceneList.size) {
                            var automation = Automation()
                            automation.Icon = sceneListResponse.SceneList.get(i).SceneIcon
                            automation.Name = sceneListResponse.SceneList.get(i).SceneName
                            automation.actions = sceneListResponse.SceneList.get(i).Actions
                            manualList.add(automation)
                        }
                        if (manualList.size < sceneListResponse.Total) {
                            manualListOffset = manualList.size
                            HttpRequest.instance.queryManualTask(App.data.getCurrentFamily().FamilyId, manualListOffset, this)
                        } else {
                            manualListLoadOver = true
                            automicListLoadOver = true
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