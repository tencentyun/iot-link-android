package com.tencent.iot.explorer.link.demo.core.activity

import android.os.Handler
import com.tencent.iot.explorer.link.demo.R
import com.tencent.iot.explorer.link.core.link.configNetwork.TIoTCoreUtil
import com.tencent.iot.explorer.link.core.link.listener.WiredConfigListener
import com.tencent.iot.explorer.link.demo.BaseActivity
import com.tencent.iot.explorer.link.demo.databinding.ActivityWiredConfigNetBinding

class WiredConfigNetActivity : BaseActivity<ActivityWiredConfigNetBinding>() {

    var handler = Handler()
    var tIoTCoreUtil = TIoTCoreUtil()

    override fun getViewBinding(): ActivityWiredConfigNetBinding = ActivityWiredConfigNetBinding.inflate(layoutInflater)

    override fun initView() {
    }

    override fun setListener() {
        binding.btnStartConfigNet.setOnClickListener {
            tIoTCoreUtil.groupAddress = binding.evGroupAddress.text.toString()
            tIoTCoreUtil.configNetByWired(binding.evToken.text.toString(), listener)
        }
    }

    var listener = object: WiredConfigListener {
        override fun onStartConfigNet() {
            handler.post {
                binding.tvStatus.setText("start config net")
            }
        }

        override fun onSuccess(productId: String, deviceName: String) {
            handler.post {
                binding.tvStatus.setText("confignet success productId " + productId + ", deviceName " + deviceName)
            }
        }

        override fun onFail() {
            handler.post {
                binding.tvStatus.setText("config net failed")
            }
        }

        override fun onConfiging() {
            handler.post {
                binding.tvStatus.setText("configing")
            }
        }

    }

}
