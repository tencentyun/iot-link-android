package com.tencent.iot.explorer.link.demo.core.activity

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.tencent.iot.explorer.link.core.auth.IoTAuth
import com.tencent.iot.explorer.link.core.auth.callback.MyCallback
import com.tencent.iot.explorer.link.core.auth.entity.DeviceEntity
import com.tencent.iot.explorer.link.core.auth.response.BaseResponse
import com.tencent.iot.explorer.link.core.demo.R
import com.tencent.iot.explorer.link.demo.core.adapter.DeviceAdapter
import com.tencent.iot.explorer.link.demo.core.adapter.OnItemListener
import com.tencent.iot.explorer.link.demo.core.holder.BaseHolder
import com.tencent.iot.explorer.link.demo.common.log.L
import com.tencent.iot.explorer.link.demo.core.response.ShareDeviceListRespone
import com.tencent.iot.explorer.link.demo.common.customView.MyDivider
import kotlinx.android.synthetic.main.activity_share_device_list.*
import kotlinx.android.synthetic.main.menu_back_layout.*

/**
 * 共享的设备列表
 */
class ShareDeviceListActivity : BaseActivity() {

    private val shareDeviceList = arrayListOf<DeviceEntity>()
    private lateinit var adapter: DeviceAdapter

    override fun getContentView(): Int {
        return R.layout.activity_share_device_list
    }

    override fun initView() {
        tv_title.text = getString(R.string.share_device)
        adapter = DeviceAdapter(this, shareDeviceList)
        rv_share_device_list.layoutManager = LinearLayoutManager(this)
        rv_share_device_list.addItemDecoration(MyDivider(dp2px(16), dp2px(16), dp2px(16)))
        rv_share_device_list.adapter = adapter
        getShareDeviceList()
    }

    override fun setListener() {
        iv_back.setOnClickListener { finish() }
        adapter.setOnItemListener(object : OnItemListener {
            override fun onItemClick(holder: BaseHolder<*>, clickView: View, position: Int) {
                put("device", shareDeviceList[position])
                put("share", true)
                jumpActivity(ControlPanelActivity::class.java)
            }
        })
    }

    /**
     * 更新列表
     */
    private fun showList() {
        tv_empty_share_device.visibility = if (shareDeviceList.size > 0) View.GONE
        else View.VISIBLE
        adapter.notifyDataSetChanged()
    }

    /**
     * 获取共享设备
     */
    private fun getShareDeviceList() {
        IoTAuth.shareImpl.shareDeviceList(0, object : MyCallback {
            override fun fail(msg: String?, reqCode: Int) {
                L.d(msg ?: "")
            }

            override fun success(response: BaseResponse, reqCode: Int) {
                if (response.isSuccess()) {
                    response.parse(ShareDeviceListRespone::class.java)?.run {
                        shareDeviceList.clear()
                        shareDeviceList.addAll(ShareDevices)
                        showList()
                    }
                }
            }
        })
    }
}
