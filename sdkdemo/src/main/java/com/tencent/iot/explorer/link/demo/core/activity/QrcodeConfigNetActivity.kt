package com.tencent.iot.explorer.link.demo.core.activity

import android.os.Handler
import com.tencent.iot.explorer.link.demo.R
import com.tencent.iot.explorer.link.core.link.configNetwork.QrcodeConfig
import com.tencent.iot.explorer.link.core.link.configNetwork.TIoTCoreUtil
import com.tencent.iot.explorer.link.demo.BaseActivity
import com.tencent.iot.explorer.link.demo.databinding.ActivityQrcodeConfigNetBinding

class QrcodeConfigNetActivity : BaseActivity<ActivityQrcodeConfigNetBinding>() {

    var handler = Handler()

    override fun getViewBinding(): ActivityQrcodeConfigNetBinding = ActivityQrcodeConfigNetBinding.inflate(layoutInflater)

    override fun initView() {
    }

    override fun setListener() {
        with(binding) {
            btnGenQrcode.setOnClickListener {
                Thread(Runnable {
                    var tIoTCoreUtil = TIoTCoreUtil()
                    var qrcodeConfig = QrcodeConfig()
                    qrcodeConfig.ssid = evWifiName.text.toString()
                    qrcodeConfig.wifiPwd = evWifiPwd.text.toString()
                    qrcodeConfig.token = evToken.text.toString()
                    qrcodeConfig.bssid = evBssidName.text.toString()
                    var bitmap = tIoTCoreUtil.generateQrCodeWithConfig(qrcodeConfig)
                    if (bitmap != null) {
                        handler.post{
                            ivQrcode.setImageBitmap(bitmap)
                        }
                    }
                }).start()

            }
        }
    }

}
