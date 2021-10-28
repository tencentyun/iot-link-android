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
import kotlinx.android.synthetic.main.activity_show_all_device.*
import kotlinx.android.synthetic.main.menu_back_layout.*

class ShowAllDeviceActivity : BaseActivity(), MyCallback {
    override fun getContentView(): Int {
        return R.layout.activity_show_all_device
    }

    override fun initView() {
        tv_title.text = getString(R.string.show_all_device)
    }

    override fun setListener() {
        iv_back.setOnClickListener { finish() }
        btn_search.setOnClickListener {
            tv_all_devices.text = ""
            var token = IoTAuth.user.Token
            var platformId = ev_platformId_2_search.text.toString()
            if (!TextUtils.isEmpty(ev_token_2_search.text.toString())) {
                token = ev_token_2_search.text.toString()
            }

            IoTAuth.deviceImpl.allDevices(token, platformId, 0, 99, this)
        }
    }

    override fun fail(msg: String?, reqCode: Int) {
        L.e(msg ?: "")
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
                    tv_all_devices.text = content2Show
                }
            } else {
                Toast.makeText(this@ShowAllDeviceActivity, response?.msg, Toast.LENGTH_SHORT).show()
            }
        }
    }
}
