package com.tenext.demo.activity

import android.view.View
import android.widget.RelativeLayout
import android.widget.Switch
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.fastjson.JSON
import com.tenext.auth.IoTAuth
import com.tenext.auth.callback.MyCallback
import com.tenext.auth.consts.RequestCode
import com.tenext.auth.entity.Device
import com.tenext.auth.response.BaseResponse
import com.tenext.demo.R
import com.tenext.demo.adapter.OnItemListener
import com.tenext.demo.adapter.TimingProjectAdapter
import com.tenext.demo.entity.TimingProject
import com.tenext.demo.holder.BaseHolder
import com.tenext.demo.log.L
import com.tenext.demo.response.TimingListResponse
import kotlinx.android.synthetic.main.activity_timing_project.*
import kotlinx.android.synthetic.main.menu_back_layout.*

/**
 *  云端定时
 */
class TimingProjectActivity : BaseActivity() {

    private val timingList = arrayListOf<TimingProject>()
    private lateinit var adapter: TimingProjectAdapter
    private var device: Device? = null

    override fun getContentView(): Int {
        return R.layout.activity_timing_project
    }

    override fun initView() {
        tv_title.text = "云端定时"
        device = get<Device>("device")
        rv_timing_project.layoutManager = LinearLayoutManager(this)
        adapter = TimingProjectAdapter(this, timingList)
        rv_timing_project.adapter = adapter
    }

    override fun onResume() {
        getTimingList()
        super.onResume()
    }

    override fun setListener() {
        iv_back.setOnClickListener {
            finish()
        }
        tv_add_timing_project.setOnClickListener {
            remove("timing")
            jumpActivity(AddTimingProjectActivity::class.java)
        }
        adapter.setOnItemListener(object : OnItemListener {
            override fun onItemClick(holder: BaseHolder<*>, clickView: View, position: Int) {
                when (clickView) {
                    is Switch -> {
                        switchTimingProject(position)
                    }
                    is RelativeLayout -> deleteTimingProject(position)
                    else -> {
                        put("timing", timingList[position])
                        jumpActivity(AddTimingProjectActivity::class.java)
                    }
                }
            }
        })
    }

    /**
     * 定时列表
     */
    private fun getTimingList() {
        device?.run {
            IoTAuth.timingImpl.timeList(ProductId, DeviceName, 0, object : MyCallback {
                override fun fail(msg: String?, reqCode: Int) {
                    L.e(msg ?: "")
                }

                override fun success(response: BaseResponse, reqCode: Int) {
                    if (response.isSuccess()) {
                        timingList.clear()
                        response.parse(TimingListResponse::class.java)?.run {
                            timingList.addAll(TimerList)
                            showTimingList()
                        }
                    } else {
                        L.e(response.msg)
                    }
                }
            })
        }
    }

    /**
     * 开关定时
     */
    private fun switchTimingProject(position: Int) {
        timingList[position].run {
            val status = if (Status == 0) 1 else 0
            IoTAuth.timingImpl.modifyTimerStatus(ProductId, DeviceName, TimerId, status,
                object : MyCallback {
                    override fun fail(msg: String?, reqCode: Int) {
                        L.e(msg ?: "")
                    }

                    override fun success(response: BaseResponse, reqCode: Int) {
                        if (response.isSuccess()) {
                            timingList[position].Status = status
                            showTimingList()
                        }
                    }
                }
            )
        }
    }

    /**
     * 删除
     */
    private fun deleteTimingProject(position: Int) {
        timingList[position].run {
            IoTAuth.timingImpl.deleteTimer(ProductId, DeviceName, TimerId,
                object : MyCallback {
                    override fun fail(msg: String?, reqCode: Int) {
                        L.e(msg ?: "")
                    }

                    override fun success(response: BaseResponse, reqCode: Int) {
                        if (response.isSuccess()) {
                            timingList.removeAt(position)
                            showTimingList()
                        }
                    }
                }
            )
        }
    }

    private fun showTimingList() {
        runOnUiThread {
            if (timingList.isEmpty()){
                tv_empty_data.visibility = View.VISIBLE
            }else{
                tv_empty_data.visibility = View.GONE
            }
            adapter.notifyDataSetChanged()
        }
    }
}
