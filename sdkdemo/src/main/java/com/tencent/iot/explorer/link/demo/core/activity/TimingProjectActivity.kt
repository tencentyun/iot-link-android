package com.tencent.iot.explorer.link.demo.core.activity

import android.view.View
import android.widget.RelativeLayout
import android.widget.Switch
import androidx.recyclerview.widget.LinearLayoutManager
import com.tencent.iot.explorer.link.core.auth.IoTAuth
import com.tencent.iot.explorer.link.core.auth.callback.MyCallback
import com.tencent.iot.explorer.link.core.auth.entity.DeviceEntity
import com.tencent.iot.explorer.link.core.auth.response.BaseResponse
import com.tencent.iot.explorer.link.demo.BaseActivity
import com.tencent.iot.explorer.link.demo.R
import com.tencent.iot.explorer.link.demo.core.adapter.OnItemListener
import com.tencent.iot.explorer.link.demo.core.adapter.TimingProjectAdapter
import com.tencent.iot.explorer.link.demo.core.entity.TimingProject
import com.tencent.iot.explorer.link.demo.core.holder.BaseHolder
import com.tencent.iot.explorer.link.demo.common.log.L
import com.tencent.iot.explorer.link.demo.core.response.TimingListResponse
import com.tencent.iot.explorer.link.demo.databinding.ActivityTimingProjectBinding

/**
 *  云端定时
 */
class TimingProjectActivity : BaseActivity<ActivityTimingProjectBinding>() {

    private val timingList = arrayListOf<TimingProject>()
    private lateinit var adapter: TimingProjectAdapter
    private var device: DeviceEntity? = null

    override fun getViewBinding(): ActivityTimingProjectBinding = ActivityTimingProjectBinding.inflate(layoutInflater)

    override fun initView() {
        with(binding) {
            menuTimingProject.tvTitle.text = "云端定时"
            device = get<DeviceEntity>("device")
            rvTimingProject.layoutManager = LinearLayoutManager(this@TimingProjectActivity)
            adapter = TimingProjectAdapter(this@TimingProjectActivity, timingList)
            rvTimingProject.adapter = adapter
        }
    }

    override fun onResume() {
        getTimingList()
        super.onResume()
    }

    override fun setListener() {
        binding.menuTimingProject.ivBack.setOnClickListener {
            finish()
        }
        binding.tvAddTimingProject.setOnClickListener {
            remove("timing")
            jumpActivity(AddTimingProjectActivity::class.java)
        }
        adapter.setOnItemListener(object : OnItemListener {
            override fun onItemClick(holder: BaseHolder<*, *>, clickView: View, position: Int) {
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
                    L.e { msg ?: "" }
                }

                override fun success(response: BaseResponse, reqCode: Int) {
                    if (response.isSuccess()) {
                        timingList.clear()
                        response.parse(TimingListResponse::class.java)?.run {
                            timingList.addAll(TimerList)
                            showTimingList()
                        }
                    } else {
                        L.e { response.msg }
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
                        L.e { msg ?: "" }
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
                        L.e { msg ?: "" }
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
                binding.tvEmptyData.visibility = View.VISIBLE
            }else{
                binding.tvEmptyData.visibility = View.GONE
            }
            adapter.notifyDataSetChanged()
        }
    }
}
