package com.tencent.iot.explorer.link.kitlink.fragment

import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.alibaba.fastjson.JSON
import com.scwang.smart.refresh.footer.ClassicsFooter
import com.scwang.smart.refresh.header.ClassicsHeader
import com.scwang.smart.refresh.layout.listener.OnLoadMoreListener
import com.scwang.smart.refresh.layout.listener.OnRefreshListener
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.T
import com.tencent.iot.explorer.link.customview.dialog.ListOptionsDialog
import com.tencent.iot.explorer.link.kitlink.activity.AddAutoicTaskActivity
import com.tencent.iot.explorer.link.kitlink.activity.AddManualTaskActivity
import com.tencent.iot.explorer.link.kitlink.adapter.IntelligenceAdapter
import com.tencent.iot.explorer.link.kitlink.entity.Automation
import com.tencent.iot.explorer.link.mvp.IPresenter
import kotlinx.android.synthetic.main.fragment_my_smart.*

/**
 * 我的智能
 */
class MySmartFragment() : BaseFragment(), View.OnClickListener {

    private var addDialog: ListOptionsDialog? = null
    private var options = ArrayList<String>()
    private var adapter: IntelligenceAdapter? = null
    private var handler: Handler = Handler()

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

        initViewData()

        tv_add_now_btn.setOnClickListener(this)
    }

    private fun initViewData() {
        Thread(Runnable {

            Thread.sleep(2000)

            handler.post {

                smart_refreshLayout.finishRefresh()
                if (adapter?.list == null || adapter?.list?.size == 0) {
                    layout_no_data.visibility = View.VISIBLE
                } else {
                    layout_no_data.visibility = View.GONE
                }
            }
        }).start()
    }

    private var onItemClicked = object: IntelligenceAdapter.OnItemClicked {
        override fun onItemClicked(automation: Automation?) {
            Log.e("XXX", "smart " + JSON.toJSONString(automation))
        }
    }

    private var onRefreshListener = OnRefreshListener {
        initViewData()
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
        refreshListData()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            refreshListData()
        }
    }

    private fun refreshListData() {

    }

    override fun onClick(v: View?) {
        when(v) {
            tv_add_now_btn -> {
                addDialog?.show()
            }
        }
    }


}