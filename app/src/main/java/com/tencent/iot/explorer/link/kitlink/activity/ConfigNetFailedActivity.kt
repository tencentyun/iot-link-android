package com.tencent.iot.explorer.link.kitlink.activity

import android.view.View
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.kitlink.consts.CommonField
import com.tencent.iot.explorer.link.kitlink.fragment.WifiFragment
import kotlinx.android.synthetic.main.activity_config_net_failed.*

class ConfigNetFailedActivity : BaseActivity() {
    var type = WifiFragment.smart_config

    override fun getContentView(): Int {
        return R.layout.activity_config_net_failed
    }

    override fun initView() {
        type = intent.getIntExtra(CommonField.CONFIG_NET_TYPE, WifiFragment.smart_config)

        when (type) {

            WifiFragment.smart_config -> {
                tv_config_net_failed_title.setText(R.string.smart_config_config_network)
                tv_config_net_failed_reason.setText(R.string.reson_config_net_info)
                tv_soft_first_commit.setText(R.string.switch_softap)
            }

            WifiFragment.soft_ap -> {
                tv_config_net_failed_title.setText(R.string.softap_config_network)
                tv_config_net_failed_reason.setText(R.string.softap_reson_config_net_info)
                tv_soft_first_commit.setText(R.string.switch_smart_config)
            }
        }
    }

    override fun setListener() {
        tv_soft_first_commit.setOnClickListener(listener)
        tv_retry.setOnClickListener(listener)
        tv_config_net_failed_back.setOnClickListener(listener)
    }

    var listener = object: View.OnClickListener {
        override fun onClick(v: View?) {
            when(v) {
                tv_soft_first_commit -> {
                    if (type == WifiFragment.soft_ap) {
                        jumpActivity(SmartConnectActivity::class.java)
                    } else {
                        jumpActivity(SoftApActivity::class.java)
                    }
                }

                tv_retry -> {
                    if (type == WifiFragment.soft_ap) {
                        jumpActivity(SoftApActivity::class.java)
                    } else {
                        jumpActivity(SmartConnectActivity::class.java)
                    }
                }

            }
            finish()
        }

    }
}
