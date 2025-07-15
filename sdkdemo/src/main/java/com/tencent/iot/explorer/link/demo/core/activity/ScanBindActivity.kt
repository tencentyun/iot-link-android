package com.tencent.iot.explorer.link.demo.core.activity

import android.text.TextUtils
import com.tencent.iot.explorer.link.core.auth.IoTAuth
import com.tencent.iot.explorer.link.core.auth.callback.MyCallback
import com.tencent.iot.explorer.link.core.auth.response.BaseResponse
import com.tencent.iot.explorer.link.demo.App
import com.tencent.iot.explorer.link.demo.BaseActivity
import com.tencent.iot.explorer.link.demo.R
import com.tencent.iot.explorer.link.demo.common.log.L
import com.tencent.iot.explorer.link.demo.databinding.ActivityScanBindBinding

/**
 * 扫码绑定设备
 */
class ScanBindActivity : BaseActivity<ActivityScanBindBinding>() {

    override fun getViewBinding(): ActivityScanBindBinding = ActivityScanBindBinding.inflate(layoutInflater)

    override fun initView() {
        binding.menuScanBind.tvTitle.text = "扫码绑定"
    }

    override fun setListener() {
        binding.menuScanBind.ivBack.setOnClickListener { finish() }
        binding.btnScanBind.setOnClickListener {
            bindDevice()
        }
    }

    private fun bindDevice() {
        val signature = binding.etScanSignature.text.trim().toString()
        if (TextUtils.isEmpty(signature)) {
            show("请输入设备签名")
            return
        }
        App.data.getCurrentFamily().run {
            IoTAuth.deviceImpl.scanBindDevice(FamilyId, signature, object : MyCallback {
                override fun fail(msg: String?, reqCode: Int) {
                    L.e(msg ?: "")
                    show(msg ?: "绑定失败")
                }

                override fun success(response: BaseResponse, reqCode: Int) {
                    if (response.isSuccess()) {
                        show("绑定成功")
                    }else{
                        show(response.msg)
                    }
                }
            })
        }
    }
}
