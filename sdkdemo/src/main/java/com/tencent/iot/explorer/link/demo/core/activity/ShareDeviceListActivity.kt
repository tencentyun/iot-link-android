package com.tencent.iot.explorer.link.demo.core.activity

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.tencent.iot.explorer.link.core.auth.IoTAuth
import com.tencent.iot.explorer.link.core.auth.callback.MyCallback
import com.tencent.iot.explorer.link.core.auth.entity.DeviceEntity
import com.tencent.iot.explorer.link.core.auth.response.BaseResponse
import com.tencent.iot.explorer.link.demo.BaseActivity
import com.tencent.iot.explorer.link.demo.R
import com.tencent.iot.explorer.link.demo.core.adapter.DeviceAdapter
import com.tencent.iot.explorer.link.demo.core.adapter.OnItemListener
import com.tencent.iot.explorer.link.demo.core.holder.BaseHolder
import com.tencent.iot.explorer.link.demo.common.log.L
import com.tencent.iot.explorer.link.demo.core.response.ShareDeviceListRespone
import com.tencent.iot.explorer.link.demo.common.customView.MyDivider
import com.tencent.iot.explorer.link.demo.databinding.ActivityShareDeviceListBinding

/**
 * 共享的设备列表
 */
class ShareDeviceListActivity : BaseActivity<ActivityShareDeviceListBinding>() {

    private val shareDeviceList = arrayListOf<DeviceEntity>()
    private lateinit var adapter: DeviceAdapter

    override fun getViewBinding(): ActivityShareDeviceListBinding = ActivityShareDeviceListBinding.inflate(layoutInflater)

    override fun initView() {
        with(binding) {
            menuShareDeviceList.tvTitle.text = getString(R.string.share_device)
            adapter = DeviceAdapter(this@ShareDeviceListActivity, shareDeviceList)
            rvShareDeviceList.layoutManager = LinearLayoutManager(this@ShareDeviceListActivity)
            rvShareDeviceList.addItemDecoration(MyDivider(dp2px(16), dp2px(16), dp2px(16)))
            rvShareDeviceList.adapter = adapter
            getShareDeviceList()
        }
    }

    override fun setListener() {
        binding.menuShareDeviceList.ivBack.setOnClickListener { finish() }
        adapter.setOnItemListener(object : OnItemListener {
            override fun onItemClick(holder: BaseHolder<*, *>, clickView: View, position: Int) {
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
        binding.tvEmptyShareDevice.visibility = if (shareDeviceList.size > 0) View.GONE
        else View.VISIBLE
        adapter.notifyDataSetChanged()
    }

    /**
     * 获取共享设备
     */
    private fun getShareDeviceList() {
        IoTAuth.shareImpl.shareDeviceList(0, object : MyCallback {
            override fun fail(msg: String?, reqCode: Int) {
                L.d { msg ?: "" }
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
