package com.tencent.iot.explorer.link.demo.core.activity

import android.os.Handler
import com.tencent.iot.explorer.link.core.demo.R
import com.tencent.iot.explorer.link.core.link.configNetwork.QrcodeConfig
import com.tencent.iot.explorer.link.core.link.configNetwork.TIoTCoreUtil
import kotlinx.android.synthetic.main.activity_qrcode_config_net.*

class QrcodeConfigNetActivity : BaseActivity() {

    var handler = Handler()

    override fun getContentView(): Int {
        return R.layout.activity_qrcode_config_net
    }

    override fun initView() {
    }

    override fun setListener() {
        btn_gen_qrcode.setOnClickListener {
            Thread(Runnable {
                var tIoTCoreUtil = TIoTCoreUtil()
                var qrcodeConfig = QrcodeConfig()
                qrcodeConfig.ssid = ev_wifi_name.text.toString()
                qrcodeConfig.wifiPwd = ev_wifi_pwd.text.toString()
                qrcodeConfig.token = ev_token.text.toString()
                qrcodeConfig.bssid = ev_bssid_name.text.toString()
                var bitmap = tIoTCoreUtil.generateQrCodeWithConfig(qrcodeConfig)
                if (bitmap != null) {
                    handler.post{
                        iv_qrcode.setImageBitmap(bitmap)
                    }
                }
            }).start()

        }
    }

}
