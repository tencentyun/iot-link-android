package com.tencent.iot.explorer.video

import android.util.Log
import com.alibaba.fastjson.JSON
import junit.framework.TestCase
import org.junit.Test
import java.lang.Long
import java.nio.charset.Charset
import java.security.InvalidKeyException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.text.SimpleDateFormat
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.experimental.and

class SignForV3Test {

    private var secretId = "YourSecretId"
    private var secretKey = "YourSecretKey"
    private var VIDEO_SERVICE = "iotvideo" // video

    companion object {
        val UTF8: Charset = Charset.forName("UTF-8")

        val REST_HOST_URL = ".tencentcloudapi.com"
    }

    @Test
    fun testSign() {
        try {
            var headerParams = commonHeaderParams("DescribeCloudStorageDate", "2020-12-15")
            val param = TreeMap<String, Any>()
            param["ProductId"] = "productId"
            param["DeviceName"] = "devName"
            val authorization = generateAuthorization(VIDEO_SERVICE, headerParams, param)
            println(authorization)
            TestCase.assertTrue(true)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            TestCase.fail()
        }
    }

    fun commonHeaderParams(action: String, version: String): HashMap<String, String> {
        val param = HashMap<String, String>()
        param["X-TC-Action"] = action
        param["X-TC-Version"] = version
        param["X-TC-Region"] = "ap-guangzhou"
        param["X-TC-Timestamp"] = (System.currentTimeMillis() / 1000).toString()
        return param
    }

    open fun generateAuthorization(
            service: String,
            headers: Map<String, String>,
            param: TreeMap<String, Any>
    ): String? {
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
        val canonicalHeadersBuilder = "content-type:application/json; charset=utf-8\nhost:${service}${REST_HOST_URL}\n"
        val signedHeadersBuilder = "content-type;host"
        val canonicalHeaders = canonicalHeadersBuilder
        val signedHeaders = signedHeadersBuilder.toLowerCase()

        // 将Extra参数加到待签名字符串中，否则会签名失败
        var payload: String? = toJson(param)
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
                ("TC3" + secretKey).toByteArray(UTF8),
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
        return algorithm + " " + "Credential=" + secretId + "/" + credentialScope + ", " + "SignedHeaders=" + signedHeaders + ", " + "Signature=" + signature
    }

    /**
     * 功能描述：把指定的java对象转为json数据
     */
    fun toJson(clazz: Any?): String? {
        return try {
            JSON.toJSONString(clazz)
        } catch (e: Exception) {
            Log.e("ToJsonEntity", "fastjson转换错误：${e.message} \n原因是：${e.cause}")
            null
        }
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
}