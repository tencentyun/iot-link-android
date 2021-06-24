package com.tencent.iot.explorer.link.kitlink.activity

import android.content.Intent
import android.view.View
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.kitlink.consts.CommonField
import com.tencent.iot.explorer.link.kitlink.fragment.DeviceFragment
import kotlinx.android.synthetic.main.activity_config_net_failed.*

class ConfigNetFailedActivity : BaseActivity() {
    var type = DeviceFragment.ConfigType.SmartConfig.id
    var productId = ""

    override fun getContentView(): Int {
        return R.layout.activity_config_net_failed
    }

    override fun initView() {
        type = intent.getIntExtra(CommonField.CONFIG_TYPE, DeviceFragment.ConfigType.SmartConfig.id)
        productId = intent.getStringExtra(CommonField.PRODUCT_ID) ?: ""

        when (type) {

            DeviceFragment.ConfigType.SmartConfig.id -> {
                tv_config_net_failed_title.setText(R.string.smart_config_config_network)
                tv_config_net_failed_reason.setText(R.string.reson_config_net_info)
                tv_soft_first_commit.setText(R.string.switch_softap)
            }

            DeviceFragment.ConfigType.SoftAp.id -> {
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
        tv_more_reason.setOnClickListener(listener)
    }

    var listener = object: View.OnClickListener {
        override fun onClick(v: View?) {
            when(v) {
                tv_soft_first_commit -> {
                    if (type == DeviceFragment.ConfigType.SoftAp.id) {
                        SmartConfigStepActivity.startActivityWithExtra(this@ConfigNetFailedActivity, productId)
                    } else {
                        SoftApStepActivity.startActivityWithExtra(this@ConfigNetFailedActivity, productId)
                    }
                    this@ConfigNetFailedActivity.finish()
                }

                tv_retry -> {
                    if (type == DeviceFragment.ConfigType.SoftAp.id) {
                        SoftApStepActivity.startActivityWithExtra(this@ConfigNetFailedActivity, productId)
                    } else {
                        SmartConfigStepActivity.startActivityWithExtra(this@ConfigNetFailedActivity, productId)
                    }
                    this@ConfigNetFailedActivity.finish()
                }

                tv_more_reason -> {
                    var intent = Intent(this@ConfigNetFailedActivity, HelpWebViewActivity::class.java)
                    intent.putExtra(CommonField.CONFIG_QUESTION_LIST, true)
                    startActivity(intent)
                }

                tv_config_net_failed_back -> {
                    this@ConfigNetFailedActivity.finish()
                }
            }
        }
    }

//    private fun startActivityWithExtra(cls: Class<*>?, productId: String) {
//        val intent = Intent(this, cls)
//        if (!TextUtils.isEmpty(productId)) {
//            intent.putExtra(CommonField.LOAD_VIEW_TXT_TYPE, LoadViewTxtType.LoadRemoteViewTxt.ordinal)
//            intent.putExtra(CommonField.PRODUCT_ID, productId)
//        }
//        startActivity(intent)
//    }

    override fun onDestroy() {
        super.onDestroy()
    }
}
