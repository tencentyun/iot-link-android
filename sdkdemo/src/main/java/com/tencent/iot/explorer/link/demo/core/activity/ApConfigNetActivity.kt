package com.tencent.iot.explorer.link.demo.core.activity

import android.os.Handler
import android.text.TextUtils
import com.tencent.iot.explorer.link.demo.R
import com.tencent.iot.explorer.link.core.link.configNetwork.TIoTCoreUtil
import com.tencent.iot.explorer.link.core.link.entity.LinkTask
import com.tencent.iot.explorer.link.core.link.listener.SoftAPConfigNetListener
import com.tencent.iot.explorer.link.demo.BaseActivity
import com.tencent.iot.explorer.link.demo.databinding.ActivityApConfigNetBinding

class ApConfigNetActivity : BaseActivity<ActivityApConfigNetBinding>() {

    var handler = Handler()
    var tIoTCoreUtil = TIoTCoreUtil()

    override fun getViewBinding(): ActivityApConfigNetBinding = ActivityApConfigNetBinding.inflate(layoutInflater)

    override fun initView() {
    }

    override fun setListener() {

        with(binding) {
            btnStartConfigNet.setOnClickListener {
                Thread(Runnable {
                    if (!TextUtils.isEmpty(tvPort.text.toString()) && TextUtils.isDigitsOnly(tvPort.text.toString())) {
                        tIoTCoreUtil.port = tvPort.text.toString().toInt()
                    }

                    var task = LinkTask()
                    task.mSsid = evWifiName.text.toString()
                    task.mBssid = evBssidName.text.toString()
                    task.mPassword = evWifiPwd.text.toString()
                    task.mAccessToken = evToken.text.toString()
                    tIoTCoreUtil.configNetBySoftAp(this@ApConfigNetActivity, task, listener)
                }).start()
            }
        }
    }

    var listener = object: SoftAPConfigNetListener {
        private fun updateStatus(text: String) {
            handler.post {
                binding.tvStatus.text = text
            }
        }

        override fun onSuccess() {
            updateStatus("send success")
        }

        override fun onFail(code: String, msg: String) {
            updateStatus("send onFail code $code msg $msg")
        }

        // 自动切换网络成功
        override fun reconnectedSuccess() {
            updateStatus("${binding.tvStatus.text}\nreconnectedSuccess")
        }

        // 自动切换网络失败
        override fun reconnectedFail() {
            updateStatus("${binding.tvStatus.text}\nreconnectedFail")
        }
    }

}
