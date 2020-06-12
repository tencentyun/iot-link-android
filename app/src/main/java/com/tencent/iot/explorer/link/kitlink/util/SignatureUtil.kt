package com.tencent.iot.explorer.link.kitlink.util

import android.util.Base64
import com.tencent.iot.explorer.link.util.L
import java.nio.charset.Charset
import java.util.*
import javax.crypto.Mac
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

object SignatureUtil {

    /**
     * 签名数据
     */
    fun format(params: Map<String, Any>): String {
        val sb = StringBuilder()
        params.toSortedMap().forEach {
            L.e("${it.key}=${it.value}")
            sb.append(it.key).append("=").append(it.value).append("&")
        }
        return sb.substring(0, sb.lastIndex)
    }

    fun signature(sign: String, secret: String): String {
        L.e("待签名=$sign")
        val secretKey = SecretKeySpec(secret.toByteArray(Charset.forName("utf-8")), "HmacSHA1")
        val mac = Mac.getInstance("HmacSHA1")
        mac.init(secretKey)
        val result = mac.doFinal(sign.toByteArray(Charset.forName("utf-8")))
        return Base64.encodeToString(result, Base64.DEFAULT).replace("\n", "")
    }

}