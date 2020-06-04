package com.tenext.demo.activity

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.tenext.auth.IoTAuth
import com.tenext.auth.callback.MyCallback
import com.tenext.auth.entity.Device
import com.tenext.auth.response.BaseResponse
import com.tenext.auth.response.DeviceListResponse
import com.tenext.demo.R
import com.tenext.demo.adapter.DeviceAdapter
import com.tenext.demo.adapter.OnItemListener
import com.tenext.demo.holder.BaseHolder
import com.tenext.demo.log.L
import com.tenext.demo.response.ShareDeviceListRespone
import com.tenext.demo.view.MyDivider
import kotlinx.android.synthetic.main.activity_share_device_list.*
import kotlinx.android.synthetic.main.menu_back_layout.*

/**
 * 共享的设备列表
 */
class ShareDeviceListActivity : BaseActivity() {

    private val shareDeviceList = arrayListOf<Device>()
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
