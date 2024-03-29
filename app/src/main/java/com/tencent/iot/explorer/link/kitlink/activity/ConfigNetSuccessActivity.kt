package com.tencent.iot.explorer.link.kitlink.activity

import android.view.View
import com.tencent.iot.explorer.link.App
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.kitlink.consts.CommonField
import com.tencent.iot.explorer.link.kitlink.entity.ConfigType
import com.tencent.iot.explorer.link.kitlink.util.Utils
import kotlinx.android.synthetic.main.activity_config_net_success.*

class ConfigNetSuccessActivity : BaseActivity() {
    var type = ConfigType.SmartConfig.id
    var deviceName = ""

    override fun getContentView(): Int {
        return R.layout.activity_config_net_success
    }

    override fun initView() {
        type = intent.getIntExtra(CommonField.CONFIG_TYPE, ConfigType.SmartConfig.id)
        deviceName = intent.getStringExtra(CommonField.DEVICE_NAME)
        var str2Show = resources.getString(R.string.device_name) +
                resources.getString(R.string.splite_from_name) + deviceName
        tv_config_net_sucess_reason_tip.setText(str2Show)

        // 发送广播，告知主界面刷新设备数量
        App.data.refresh = true
        App.data.setRefreshLevel(2)
        Utils.sendRefreshBroadcast(this@ConfigNetSuccessActivity)
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
                    finish()
                }
            }
        }
    }
}
