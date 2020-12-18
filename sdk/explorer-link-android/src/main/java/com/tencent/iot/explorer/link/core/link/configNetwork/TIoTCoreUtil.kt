package com.tencent.iot.explorer.link.core.link.configNetwork

import android.content.Context
import android.graphics.Bitmap
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import com.tencent.iot.explorer.link.core.auth.http.Reconnect
import com.tencent.iot.explorer.link.core.link.entity.DeviceInfo
import com.tencent.iot.explorer.link.core.link.entity.LinkTask
import com.tencent.iot.explorer.link.core.link.entity.SoftAPStep
import com.tencent.iot.explorer.link.core.link.listener.SoftAPConfigNetListener
import com.tencent.iot.explorer.link.core.link.listener.SoftAPListener
import com.tencent.iot.explorer.link.core.link.service.SoftAPService
import java.util.*

class TIoTCoreUtil {

    var softAPService: SoftAPService? = null
    var softAPConfigNetListener: SoftAPConfigNetListener? = null

    fun generateQrCodeWithConfig(qrcodeConfig: QrcodeConfig): Bitmap? {
        if (qrcodeConfig.height <= 0 || qrcodeConfig.width <= 0) {
            return null
        }
        var json = JSONObject()
        json.put("ssid", qrcodeConfig.ssid)
        json.put("bssid", qrcodeConfig.bssid)
        json.put("pwd", qrcodeConfig.wifiPwd)
        json.put("token", qrcodeConfig.token)
        return generateQRCodeFromString(json.toJSONString(), qrcodeConfig.width, qrcodeConfig.height)
    }

    private fun generateQRCodeFromString(content: String?, width: Int, height: Int): Bitmap? {
        val hints = HashMap<EncodeHintType, String>()
        val multiFormatWriter = MultiFormatWriter()
        hints[EncodeHintType.CHARACTER_SET] = "UTF-8"
        val bitMatrix: BitMatrix = multiFormatWriter.encode(content, BarcodeFormat.QR_CODE, width, height, hints)
        val pixels = IntArray(width * height)
        for (y in 0 until height) {
            for (x in 0 until width) {
                if (bitMatrix.get(x, y)) {
                    pixels[y * width + x] = -0x1000000
                } else {
                    pixels[y * width + x] = -0x1
                }
            }
        }
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        return bitmap
    }

    fun configNetBySoftAp(context: Context, task: LinkTask, listener: SoftAPConfigNetListener) {
        softAPConfigNetListener = listener
        if (context == null) {
            if (softAPConfigNetListener != null) {
                softAPConfigNetListener!!.onFail("error", "context is null")
            }
            return
        }
        softAPService = SoftAPService(context)
        softAPService?.startConnect(task, softAPListener)
    }

    private val softAPListener = object : SoftAPListener {
        override fun onSuccess(deviceInfo: DeviceInfo) {
            if (softAPConfigNetListener != null) {
                softAPConfigNetListener!!.onSuccess()
            }
        }

        override fun onFail(code: String, msg: String) {
            if (softAPConfigNetListener != null) {
                softAPConfigNetListener!!.onFail(code, msg)
            }
        }

        override fun reconnectedSuccess(deviceInfo: DeviceInfo) {}
        override fun reconnectedFail(deviceInfo: DeviceInfo, ssid: String) {}
        override fun onStep(step: SoftAPStep) {}
    }


}