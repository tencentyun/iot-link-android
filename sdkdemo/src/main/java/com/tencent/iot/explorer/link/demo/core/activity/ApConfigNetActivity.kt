package com.tencent.iot.explorer.link.demo.core.activity

import android.os.Handler
import android.text.TextUtils
import com.tencent.iot.explorer.link.core.demo.R
import com.tencent.iot.explorer.link.core.link.configNetwork.TIoTCoreUtil
import com.tencent.iot.explorer.link.core.link.entity.LinkTask
import com.tencent.iot.explorer.link.core.link.listener.SoftAPConfigNetListener
import kotlinx.android.synthetic.main.activity_ap_config_net.*
import kotlinx.android.synthetic.main.activity_qrcode_config_net.ev_bssid_name
import kotlinx.android.synthetic.main.activity_qrcode_config_net.ev_token
import kotlinx.android.synthetic.main.activity_qrcode_config_net.ev_wifi_name
import kotlinx.android.synthetic.main.activity_qrcode_config_net.ev_wifi_pwd

class ApConfigNetActivity : BaseActivity() {

    var handler = Handler()
    var tIoTCoreUtil = TIoTCoreUtil()

    override fun getContentView(): Int {
        return R.layout.activity_ap_config_net
    }

    override fun initView() {
    }

    override fun setListener() {
        btn_start_config_net.setOnClickListener {
            Thread(Runnable {
                if (!TextUtils.isEmpty(tv_port.text.toString()) && TextUtils.isDigitsOnly(tv_port.text.toString())) {
                    tIoTCoreUtil.port = tv_port.text.toString().toInt()
                }

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
            handler.post {
                tv_status.setText("send success")
            }
        }

        override fun onFail(code: String, msg: String) {
            handler.post {
                tv_status.setText("send onFail code " + code + " msg " + msg)
            }
        }

        // 自动切换网络成功
        override fun reconnectedSuccess() {
            handler.post {
                tv_status.setText(tv_status.text.toString() + "\nreconnectedSuccess")
            }
        }

        // 自动切换网络失败
        override fun reconnectedFail() {
            handler.post {
                tv_status.setText(tv_status.text.toString() + "\nreconnectedFail")
            }
        }

    }

}
