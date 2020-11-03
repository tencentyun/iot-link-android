package com.tencent.iot.explorer.link.kitlink.activity

import android.os.Handler
import androidx.recyclerview.widget.LinearLayoutManager
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.kitlink.adapter.DevModeAdapter
import com.tencent.iot.explorer.link.kitlink.adapter.ManualTaskAdapter
import com.tencent.iot.explorer.link.kitlink.entity.DevModeInfo
import kotlinx.android.synthetic.main.activity_add_manual_task.*
import kotlinx.android.synthetic.main.activity_device_mode_info.*
import kotlinx.android.synthetic.main.menu_back_layout.*


class DeviceModeInfoActivity : BaseActivity() {

    private var devModes = ArrayList<DevModeInfo>()
    private var adapter: DevModeAdapter = DevModeAdapter(devModes)

    override fun getContentView(): Int {
        return R.layout.activity_device_mode_info
    }

    override fun initView() {
        tv_title.setText("")

        val layoutManager = LinearLayoutManager(this)
        lv_dev_mode.setLayoutManager(layoutManager)
        adapter?.setOnItemClicked(onListItemClicked)
        lv_dev_mode.setAdapter(adapter)

        loadView()
    }

    private var onListItemClicked = object : DevModeAdapter.OnItemClicked{
        override fun onItemClicked(pos: Int, devModeInfo: DevModeInfo) {

        }

    }

    override fun setListener() {
        iv_back.setOnClickListener { finish() }
        tv_cancel.setOnClickListener { finish() }
    }

    fun loadView() {
        devModes.add(DevModeInfo())
        devModes.add(DevModeInfo())
        devModes.add(DevModeInfo())
        adapter.notifyDataSetChanged()
    }

}