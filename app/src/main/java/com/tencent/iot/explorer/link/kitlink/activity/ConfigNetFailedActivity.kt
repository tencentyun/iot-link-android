package com.tencent.iot.explorer.link.kitlink.activity

import android.content.Intent
import android.view.View
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.core.link.service.LLSyncErrorCode
import com.tencent.iot.explorer.link.kitlink.consts.CommonField
import com.tencent.iot.explorer.link.kitlink.entity.ConfigType
import kotlinx.android.synthetic.main.activity_config_net_failed.*

class ConfigNetFailedActivity : BaseActivity() {
    var type = ConfigType.SmartConfig.id
    var productId = ""
    var bleErrorCode = ""

    override fun getContentView(): Int {
        return R.layout.activity_config_net_failed
    }

    override fun initView() {
        type = intent.getIntExtra(CommonField.CONFIG_TYPE, ConfigType.SmartConfig.id)
        productId = intent.getStringExtra(CommonField.PRODUCT_ID) ?: ""
        bleErrorCode = intent.getStringExtra(CommonField.CONFIG_NET_ERROR_CODE) ?: ""

        when (type) {

            ConfigType.SmartConfig.id -> {
                tv_config_net_failed_title.setText(R.string.smart_config_config_network)
                tv_config_net_failed_reason.setText(R.string.reson_config_net_info)
                tv_soft_first_commit.setText(R.string.switch_softap)
            }

            ConfigType.SoftAp.id -> {
                tv_config_net_failed_title.setText(R.string.softap_config_network)
                tv_config_net_failed_reason.setText(R.string.softap_reson_config_net_info)
                tv_soft_first_commit.setText(R.string.switch_smart_config)
            }

            ConfigType.BleConfig.id -> {
                tv_config_net_failed_title.setText(getString(R.string.ble_config_network))
                tv_config_net_failed_reason.setText(getErrorTipMessage(bleErrorCode))
                tv_soft_first_commit.visibility = View.GONE
                tv_soft_first_commit.setText("")
                tv_more_reason.visibility = View.GONE
            }
        }
    }

    private fun getErrorTipMessage(code: String): String {
        when (code) {
            LLSyncErrorCode.WIFI_CONFIG_TIMEOUT_ERROR_CODE -> {
                return "设备配网超时"
            }
            LLSyncErrorCode.WIFI_CONFIG_BLE_DISCONNECT_ERROR_CODE -> {
                return "设备和App断开蓝牙连接"
            }
            LLSyncErrorCode.WIFI_CONFIG_BLE_SET_MTU_ERROR_CODE -> {
                return "设置MTU失败"
            }
            LLSyncErrorCode.WIFI_CONFIG_BLE_SET_WIFI_MODE_ERROR_CODE -> {
                return "设置设备WiFi模式失败"
            }
            LLSyncErrorCode.WIFI_CONFIG_BLE_SET_WIFI_MODE_RESPONSE_ERROR_CODE -> {
                return "设备反馈设置WiFi模式失败"
            }
            LLSyncErrorCode.WIFI_CONFIG_BLE_SET_WIFI_INFO_ERROR_CODE -> {
                return "向设备发送WIFI信息失败"
            }
            LLSyncErrorCode.WIFI_CONFIG_BLE_REQUEST_WIFI_CONNECT_ERROR_CODE -> {
                return "请求设备连接wifi失败"
            }
            LLSyncErrorCode.WIFI_CONFIG_BLE_WIFI_CONNECT_RESPONSE_ERROR_CODE -> {
                return "设备反馈连接wifi失败"
            }
            LLSyncErrorCode.WIFI_CONFIG_BLE_REQUEST_BIND_TOKEN_ERROR_CODE -> {
                return "请求设备绑定token失败"
            }
            LLSyncErrorCode.WIFI_CONFIG_BLE_BIND_TOKEN_RESPONSE_ERROR_CODE -> {
                return "设备反馈绑定token失败"
            }
            LLSyncErrorCode.WIFI_CONFIG_BLE_SET_MTU_RESPONSE_ERROR_CODE -> {
                return "设备反馈设置MTU失败"
            }
            LLSyncErrorCode.WIFI_CONFIG_BLE_REQUEST_GET_DEVICE_INFO_ERROR_CODE -> {
                return "获取蓝牙设备信息失败"
            }
            LLSyncErrorCode.WIFI_CONFIG_BLE_BIND_BLE_DEVICE_NET_ERROR_CODE -> {
                return "绑定蓝牙设备接口服务失败"
            }
            LLSyncErrorCode.WIFI_CONFIG_BLE_BIND_BLE_DEVICE_NET_OTHER_ERROR_CODE -> {
                return "绑定蓝牙设备接口失败"
            }
            LLSyncErrorCode.WIFI_CONFIG_BLE_BIND_BLE_DEVICE_RESPONSE_ERROR_CODE -> {
                return "向蓝牙设备发送绑定设备结果失败"
            }
            LLSyncErrorCode.WIFI_CONFIG_BLE_PARAMS_ERROR_CODE -> {
                return "解析数据失败"
            }
            LLSyncErrorCode.PURE_BLE_SET_UNIX_TIMESTAMP_NONCE_ERROR_CODE -> {
                return "设置unix和随机串失败"
            }
        }
        return ""
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
                    if (type == ConfigType.SoftAp.id) {
                        SmartConfigStepActivity.startActivityWithExtra(this@ConfigNetFailedActivity, productId)
                    } else if (type == ConfigType.SmartConfig.id) {
                        SoftApStepActivity.startActivityWithExtra(this@ConfigNetFailedActivity, productId)
                    } else {

                    }
                    this@ConfigNetFailedActivity.finish()
                }

                tv_retry -> {
                    if (type == ConfigType.SoftAp.id) {
                        SoftApStepActivity.startActivityWithExtra(this@ConfigNetFailedActivity, productId)
                    } else if (type == ConfigType.SmartConfig.id) {
                        SmartConfigStepActivity.startActivityWithExtra(this@ConfigNetFailedActivity, productId)
                    } else {

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
