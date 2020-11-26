package com.tencent.iot.explorer.link.kitlink.fragment

import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.scwang.smart.refresh.footer.ClassicsFooter
import com.scwang.smart.refresh.header.ClassicsHeader
import com.scwang.smart.refresh.layout.listener.OnLoadMoreListener
import com.scwang.smart.refresh.layout.listener.OnRefreshListener
import com.tencent.iot.explorer.link.App
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.T
import com.tencent.iot.explorer.link.core.auth.response.BaseResponse
import com.tencent.iot.explorer.link.kitlink.adapter.SmartLogAdapter
import com.tencent.iot.explorer.link.kitlink.entity.LogMessage
import com.tencent.iot.explorer.link.kitlink.entity.LogResponesData
import com.tencent.iot.explorer.link.kitlink.util.HttpRequest
import com.tencent.iot.explorer.link.core.auth.callback.MyCallback
import com.tencent.iot.explorer.link.kitlink.util.RequestCode
import com.tencent.iot.explorer.link.mvp.IPresenter
import kotlinx.android.synthetic.main.fragment_smart_log.*
import java.util.*

/**
 * 智能日志页面
 */
class SmartLogFragment() : BaseFragment(), MyCallback {

    private var msgId = ""
    var logs: MutableList<LogMessage> = LinkedList()
    var adapter: SmartLogAdapter? = null

    override fun getPresenter(): IPresenter? {
        return null
    }

    override fun getContentView(): Int {
        return R.layout.fragment_smart_log
    }

    override fun startHere(view: View) {
        initView()
    }

    fun initView() {
        val layoutManager = LinearLayoutManager(context)
        lv_all_log.setLayoutManager(layoutManager)
        adapter = SmartLogAdapter(context!!, logs)
        lv_all_log.adapter = adapter
        //下拉刷新及上拉加载
        log_refreshLayout.setEnableRefresh(true)  // 禁止上拉刷新
        log_refreshLayout.setRefreshHeader(ClassicsHeader(context))
        log_refreshLayout.setEnableLoadMore(true)
        log_refreshLayout.setRefreshFooter(ClassicsFooter(context))
        log_refreshLayout.setOnRefreshListener(onRefreshListener)
        log_refreshLayout.setOnLoadMoreListener(onLoadMoreListener)
        reloadData()
    }

    private var onRefreshListener = OnRefreshListener {
        reloadData()
    }

    private var onLoadMoreListener = OnLoadMoreListener {
        loadMore()
    }

    private fun loadMore() {
        HttpRequest.instance.getTaskRunLog(msgId, App.data.getCurrentFamily().FamilyId, this)
    }

    private fun reloadData() {
        logs.clear()
        msgId = ""
        HttpRequest.instance.getTaskRunLog(msgId, App.data.getCurrentFamily().FamilyId, this)
    }

    override fun fail(msg: String?, reqCode: Int) {
        log_refreshLayout.finishRefresh()
        log_refreshLayout.finishLoadMore()
        T.show(msg)
    }

    override fun success(response: BaseResponse, reqCode: Int) {
        when (reqCode) {
            RequestCode.get_run_task_log -> {
                if (response.isSuccess()) {
                    log_refreshLayout.finishRefresh()
                    log_refreshLayout.finishLoadMore()
                    var dataJson = response.data as JSONObject
                    var logResponesData = JSON.parseObject(dataJson.getString("Data"), LogResponesData::class.java)
                    for (i in 0 until logResponesData.msgs!!.size) {
                        logs.add(logResponesData.msgs!!.get(i))
                    }

                    // 存在数据集的时候，更新最后的一条的消息 ID
                    if (logs.size > 0) {
                        msgId = logs.get(logs.lastIndex).msgId
                        adapter?.notifyDataSetChanged()
                        layout_no_data.visibility = View.GONE
                    } else {
                        layout_no_data.visibility = View.VISIBLE
                    }
                } else {
                    log_refreshLayout.finishRefresh()
                    log_refreshLayout.finishLoadMore()
                    T.show(response.msg)
                }
            }
        }
    }
}