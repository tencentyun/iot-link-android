package com.tencent.iot.explorer.link.demo.core.activity

import android.text.TextUtils
import android.widget.Toast
import com.alibaba.fastjson.JSON
import com.tencent.iot.explorer.link.core.auth.IoTAuth
import com.tencent.iot.explorer.link.core.auth.callback.MyCallback
import com.tencent.iot.explorer.link.core.auth.entity.VirtualBindDeviceList
import com.tencent.iot.explorer.link.core.auth.response.BaseResponse
import com.tencent.iot.explorer.link.demo.BaseActivity
import com.tencent.iot.explorer.link.demo.R
import com.tencent.iot.explorer.link.demo.common.log.L
import com.tencent.iot.explorer.link.demo.databinding.ActivityShowAllDeviceBinding

class ShowAllDeviceActivity : BaseActivity<ActivityShowAllDeviceBinding>(), MyCallback {
    override fun getViewBinding(): ActivityShowAllDeviceBinding = ActivityShowAllDeviceBinding.inflate(layoutInflater)

    override fun initView() {
        binding.menuAllDevice.tvTitle.text = getString(R.string.show_all_device)
    }

    override fun setListener() {
        with(binding) {
            menuAllDevice.ivBack.setOnClickListener { finish() }
            btnSearch.setOnClickListener {
                tvAllDevices.text = ""
                var token = IoTAuth.user.Token
                var platformId = evPlatformId2Search.text.toString()
                if (!TextUtils.isEmpty(evToken2Search.text.toString())) {
                    token = evToken2Search.text.toString()
                }

                IoTAuth.deviceImpl.allDevices(token, platformId, 0, 99, this@ShowAllDeviceActivity)
            }
        }
    }

    override fun fail(msg: String?, reqCode: Int) {
        L.e { msg ?: "" }
    }

    override fun success(response: BaseResponse, reqCode: Int) {

        runOnUiThread {
            if (response.code == 0) {
                var virtualBindDeviceList = JSON.parseObject(response.data.toString(), VirtualBindDeviceList::class.java)
                virtualBindDeviceList?.let {
                    if (it.totalCount <= 0) {
                        Toast.makeText(this@ShowAllDeviceActivity, R.string.no_devices, Toast.LENGTH_SHORT).show()
                        return@let
                    }
                    var content2Show = ""

                    if (it.virtualBindDeviceList.size <= 0) return@let
                    for (item in it.virtualBindDeviceList) {
                        content2Show += item.deviceName + "\n"
                    }
                    binding.tvAllDevices.text = content2Show
                }
            } else {
                Toast.makeText(this@ShowAllDeviceActivity, response.msg, Toast.LENGTH_SHORT).show()
            }
        }
    }
}
