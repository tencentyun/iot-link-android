package com.tencent.iot.explorer.link.demo.core.activity

import android.os.Handler
import com.tencent.iot.explorer.link.core.demo.R
import com.tencent.iot.explorer.link.core.link.configNetwork.TIoTCoreUtil
import com.tencent.iot.explorer.link.core.link.listener.WiredConfigListener
import kotlinx.android.synthetic.main.activity_ap_config_net.btn_start_config_net
import kotlinx.android.synthetic.main.activity_ap_config_net.tv_status
import kotlinx.android.synthetic.main.activity_qrcode_config_net.ev_token
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
            tIoTCoreUtil.groupAddress = ev_group_address.text.toString()
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
