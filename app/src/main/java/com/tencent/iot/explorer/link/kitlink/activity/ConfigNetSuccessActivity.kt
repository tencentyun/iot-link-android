package com.tencent.iot.explorer.link.kitlink.activity

import android.view.View
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.kitlink.consts.CommonField
import com.tencent.iot.explorer.link.kitlink.fragment.WifiFragment
import kotlinx.android.synthetic.main.activity_config_net_success.*

class ConfigNetSuccessActivity : BaseActivity() {
    var type = WifiFragment.smart_config
    var deviceName = ""

    override fun getContentView(): Int {
        return R.layout.activity_config_net_success
    }

    override fun initView() {
        type = intent.getIntExtra(CommonField.CONFIG_NET_TYPE, WifiFragment.smart_config)
        deviceName = intent.getStringExtra(CommonField.DEVICE_NAME)
        var str2Show = resources.getString(R.string.device_name) +
                resources.getString(R.string.splite_from_name) + deviceName
        tv_config_net_sucess_reason_tip.setText(str2Show)
    }

    override fun setListener() {
        tv_finish.setOnClickListener(listener)
        tv_add_other.setOnClickListener(listener)
        tv_config_net_sucess_back.setOnClickListener(listener)
    }

    var listener = object: View.OnClickListener {
        override fun onClick(v: View?) {
            when (v) {
                tv_finish -> {
                    backToMain()
                }
                tv_add_other -> {
//                    if (type == WifiFragment.soft_ap) {
//                        jumpActivity(SoftApActivity::class.java)
//                    } else {
//                        jumpActivity(SmartConnectActivity::class.java)
//                    }
                }
            }
            finish()
        }
    }
}
