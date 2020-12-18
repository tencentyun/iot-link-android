package com.tencent.iot.explorer.link.core.demo.activity

import android.os.Handler
import android.util.Log
import com.tencent.iot.explorer.link.core.demo.R
import com.tencent.iot.explorer.link.core.link.configNetwork.TIoTCoreUtil
import com.tencent.iot.explorer.link.core.link.entity.LinkTask
import com.tencent.iot.explorer.link.core.link.listener.SoftAPConfigNetListener
import kotlinx.android.synthetic.main.activity_ap_config_net.*
import kotlinx.android.synthetic.main.activity_qrcode_config_net.*
import kotlinx.android.synthetic.main.activity_qrcode_config_net.ev_bssid_name
import kotlinx.android.synthetic.main.activity_qrcode_config_net.ev_token
import kotlinx.android.synthetic.main.activity_qrcode_config_net.ev_wifi_name
import kotlinx.android.synthetic.main.activity_qrcode_config_net.ev_wifi_pwd

class ApConfigNetActivity : BaseActivity() {

    var handler = Handler()

    override fun getContentView(): Int {
        return R.layout.activity_ap_config_net
    }

    override fun initView() {
    }

    override fun setListener() {
        btn_start_config_net.setOnClickListener {
            Thread(Runnable {
                var tIoTCoreUtil = TIoTCoreUtil()
                var task = LinkTask()
                task.mSsid = ev_wifi_name.text.toString()
                task.mBssid = ev_bssid_name.text.toString()
                task.mPassword = ev_wifi_pwd.text.toString()
                task.mAccessToken = ev_token.text.toString()
                tIoTCoreUtil.configNetBySoftAp(this@ApConfigNetActivity, task, listener)
            }).start()

        }
    }

    var listener = object: SoftAPConfigNetListener {
        override fun onSuccess() {
            Log.e("XXX", "send success")
            handler.post {
                tv_status.setText("send success")
            }
        }

        override fun onFail(code: String, msg: String) {
            Log.e("XXX", "send onFail code " + code + " msg " + msg)
            handler.post {
                tv_status.setText("send onFail code " + code + " msg " + msg)
            }
        }

    }

}
