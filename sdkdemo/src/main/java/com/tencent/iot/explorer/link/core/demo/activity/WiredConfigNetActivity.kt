package com.tencent.iot.explorer.link.core.demo.activity

import android.os.Handler
import android.text.TextUtils
import android.util.Log
import com.tencent.iot.explorer.link.core.demo.R
import com.tencent.iot.explorer.link.core.link.configNetwork.TIoTCoreUtil
import com.tencent.iot.explorer.link.core.link.entity.LinkTask
import com.tencent.iot.explorer.link.core.link.listener.SoftAPConfigNetListener
import com.tencent.iot.explorer.link.core.link.listener.WiredConfigListener
import kotlinx.android.synthetic.main.activity_ap_config_net.*
import kotlinx.android.synthetic.main.activity_ap_config_net.btn_start_config_net
import kotlinx.android.synthetic.main.activity_ap_config_net.tv_status
import kotlinx.android.synthetic.main.activity_qrcode_config_net.ev_bssid_name
import kotlinx.android.synthetic.main.activity_qrcode_config_net.ev_token
import kotlinx.android.synthetic.main.activity_qrcode_config_net.ev_wifi_name
import kotlinx.android.synthetic.main.activity_qrcode_config_net.ev_wifi_pwd
import kotlinx.android.synthetic.main.activity_wired_config_net.*

class WiredConfigNetActivity : BaseActivity() {

    var handler = Handler()
    var tIoTCoreUtil = TIoTCoreUtil()

    override fun getContentView(): Int {
        return R.layout.activity_wired_config_net
    }

    override fun initView() {
    }

    override fun setListener() {
        btn_start_config_net.setOnClickListener {
            tIoTCoreUtil.configNetByWired(ev_token.text.toString(), listener)
        }
    }

    var listener = object: WiredConfigListener {
        override fun onStartConfigNet() {
            handler.post {
                tv_status.setText("start config net")
            }
        }

        override fun onSuccess(productId: String, deviceName: String) {
            handler.post {
                tv_status.setText("confignet success productId " + productId + ", deviceName " + deviceName)
            }
        }

        override fun onFail() {
            handler.post {
                tv_status.setText("config net failed")
            }
        }

        override fun onConfiging() {
            handler.post {
                tv_status.setText("configing")
            }
        }

    }

}
