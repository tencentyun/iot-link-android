package com.tencent.iot.explorer.link.core.test.service

import android.util.Log
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.tencent.iot.explorer.link.core.auth.IoTAuth
import com.tencent.iot.explorer.link.core.auth.util.JsonManager
import com.tencent.iot.explorer.link.core.test.callback.VideoCallback
import com.tencent.iot.explorer.link.core.test.consts.VideoRequestCode
import com.tencent.iot.explorer.link.core.test.http.HttpCallBack
import com.tencent.iot.explorer.link.core.test.http.VideoHttpUtil
import java.lang.Long
import java.nio.charset.Charset
import java.security.InvalidKeyException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.text.SimpleDateFormat
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.collections.HashMap
import kotlin.experimental.and

/**
 * 接口请求文件
 */
open class VideoBaseService {

    companion object {
        const val video_describe_devices = "describe_devices"
        val UTF8: Charset = Charset.forName("UTF-8")
    }
    /**
     * 获取设备信息列表   DescribeDevices
     */
    fun describeDevices(
        productId: String, callback: VideoCallback
    ) {
        var headerParams = commonHeaderParams("GetDeviceList")
        val param = TreeMap<String, Any>()
        param["ProductId"] = productId
        val authorization = sign(headerParams, param)
        if (authorization != null) {
            headerParams["Authorization"] = authorization
        }
        basePost(param, headerParams, callback, VideoRequestCode.video_describe_devices)
    }

    /**
     * 未登录接口公共参数
     */
    fun commonHeaderParams(action: String): HashMap<String, String> {
        val param = HashMap<String, String>()
        param["X-TC-Action"] = action
        param["X-TC-Version"] = "2019-04-23"
        param["X-TC-Region"] = "ap-guangzhou"
        param["X-TC-Timestamp"] = (System.currentTimeMillis() / 1000).toString()
        return param
    }

    open fun sign(
        headers: Map<String, String>,
        param: TreeMap<String, Any>
    ): String? {
        val service: String = "iotexplorer"
        val host: String = VideoHttpUtil.VIDEO_API
        val action: String = headers?.get("X-TC-Action") as String
        val version: String = headers?.get("X-TC-Version") as String
        val algorithm = "TC3-HMAC-SHA256"
        val timestamp: String = headers?.get("X-TC-Timestamp") as String
        val sdf = SimpleDateFormat("yyyy-MM-dd")
        // 注意时区，否则容易出错
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        val date =
            sdf.format(Date(Long.valueOf(timestamp + "000")))

        // ************* 步骤 1：拼接规范请求串 *************
        val httpRequestMethod = "POST"
        val canonicalUri = "/"
        val canonicalQueryString = ""
        val canonicalHeadersBuilder = "content-type:application/json; charset=utf-8\nhost:${VideoHttpUtil.VIDEO_API}\n"
        val signedHeadersBuilder = "content-type;host"
        val canonicalHeaders = canonicalHeadersBuilder
        val signedHeaders = signedHeadersBuilder.toLowerCase()

        // 将Extra参数加到待签名字符串中，否则会签名失败
        var payload: String? = JsonManager.toJson(param)
        val hashedRequestPayload: String? = payload?.let { sha256Hex(it) }
        val canonicalRequest = "${httpRequestMethod}\n${canonicalUri}\n${canonicalQueryString}\n${canonicalHeaders}\n${signedHeaders}\n${hashedRequestPayload}"
        println(canonicalRequest)

        // ************* 步骤 2：拼接待签名字符串 *************
        val credentialScope = "$date/$service/tc3_request"
        val hashedCanonicalRequest: String? = sha256Hex(canonicalRequest)
        val stringToSign =
            """
            $algorithm
            $timestamp
            $credentialScope
            $hashedCanonicalRequest
            """.trimIndent()
        println(stringToSign)

        // ************* 步骤 3：计算签名 *************  SecretKey
        val secretDate: ByteArray = hmac256(
                ("TC3" + "").toByteArray(UTF8),
                date
            )
        val secretService: ByteArray = hmac256(secretDate, service)
        val secretSigning: ByteArray = hmac256(
                secretService,
                "tc3_request"
            )
        val byteArray: ByteArray = hmac256(secretSigning, stringToSign)
        val signature: String? = encodeHexString(byteArray)

        // ************* 步骤 4：拼接 Authorization ************* SecretId
        return algorithm + " " + "Credential=" + "" + "/" + credentialScope + ", " + "SignedHeaders=" + signedHeaders + ", " + "Signature=" + signature
    }

    open fun sha256Hex(s: String): String? {
        var md: MessageDigest? = null
        var d: ByteArray? = null
        try {
            md = MessageDigest.getInstance("SHA-256")
            d = md.digest(s.toByteArray(UTF8))
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }
        return d?.let { encodeHexString(it)?.toLowerCase() }
    }

    open fun encodeHexString(byteArray: ByteArray): String? {
        val hexStringBuffer = StringBuffer()
        for (i in byteArray.indices) {
            hexStringBuffer.append(
                byteToHex(
                    byteArray[i]
                )
            )
        }
        return hexStringBuffer.toString()
    }

    open fun byteToHex(num: Byte): String? {
        val hexDigits = CharArray(2)
        hexDigits[0] = Character.forDigit((num.toInt() shr 4) and 0xF, 16)
        hexDigits[1] = Character.forDigit((num and 0xF).toInt(), 16)
        return String(hexDigits)
    }

    open fun hmac256(key: ByteArray, msg: String): ByteArray {
        var mac: Mac? = null
        try {
            mac = Mac.getInstance("HmacSHA256")
            val secretKeySpec =
                SecretKeySpec(key, mac.algorithm)
            mac.init(secretKeySpec)
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        } catch (e: InvalidKeyException) {
            e.printStackTrace()
        }
        return mac!!.doFinal(msg.toByteArray(UTF8))
    }

    /**
     * base请求
     */
    fun basePost(param: TreeMap<String, Any>, headerParams: Map<String, Any>, callback: VideoCallback, reqCode: Int) {
        VideoHttpUtil.post(param, headerParams, object :
            HttpCallBack {
            override fun onSuccess(response: String) {
                Log.d("响应${headerParams["X-TC-Action"]}", response)
                val jsonObject = JSON.parse(response) as JSONObject
                val jsonResponset = jsonObject.getJSONObject("Response") as JSONObject
                if (!jsonResponset.containsKey("ERROR")) {
                    callback.success(response, reqCode)
                } else {
                    callback.fail("error", reqCode)
                }
            }

            override fun onError(error: String) {
                callback.fail(error, reqCode)
            }
        })
    }


}