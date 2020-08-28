package com.tencent.iot.explorer.link.core.utils

import android.annotation.TargetApi
import android.content.Context
import android.net.MacAddress
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSuggestion
import android.os.Build
import com.tencent.iot.explorer.link.core.log.L
import java.io.BufferedReader
import java.io.InputStreamReader


object PingUtil {

    const val OPEN_NETWORD = 0
    const val WPA_WAP2 = 1
    const val WEP = 2

    fun ping(host: String): Boolean {
        var isSuccess: Boolean
        var process: Process? = null
        try {
            process = Runtime.getRuntime()
                .exec("/system/bin/ping -c 1 $host")
            isSuccess = (process.waitFor() == 0)
        } catch (e: Exception) {
            isSuccess = false
            e.printStackTrace()
            process?.destroy()
        } finally {
            process?.destroy()
        }
        L.d("ping isSuccess=$isSuccess")
        return isSuccess
    }

    fun pingAndRead(host: String): String {
        var process: Process? = null
        var buf: BufferedReader? = null
        try {
            process = Runtime.getRuntime()
                .exec("/system/bin/ping -c 4 $host")
            buf = BufferedReader(InputStreamReader(process.inputStream))
            var result = ""
            var str = buf.readLine()
            //读出所有信息并显示
            while (str != null) {
                result += "\r\n"
                str = buf.readLine()
            }
            return result
        } catch (e: Exception) {
            e.printStackTrace()
            process?.destroy()
            buf?.close()
        } finally {
            process?.destroy()
            buf?.close()
        }
        return ""
    }

    private fun append(stringBuffer: StringBuffer, text: String) {
        stringBuffer.append(text + "\n")
    }

    fun connect(context: Context, ssid: String, bssid: String, password: String): Boolean {
        (context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager).run {
            //判断wifi曾经是不是连接过
            val tempConfig = isExists(this, ssid)
            return if (tempConfig != null) {
                disconnect()
                L.e("连接1")
                return enableNetwork(tempConfig.networkId, true)
            } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                val wifiConfiguration = getWifiConfiguration(WPA_WAP2, password)
                wifiConfiguration.SSID = "\"$ssid\""
                wifiConfiguration.BSSID = bssid
                enableNetwork(addNetwork(wifiConfiguration), true)
            } else {
                return connetcWifiOverQ(context, ssid, password)
            }
            return true
        }
    }

    private fun getWifiConfiguration(type: Int, password: String): WifiConfiguration {
        val wifiConfig = WifiConfiguration()
        when (type) {
            OPEN_NETWORD -> {
                // No security
                wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE)
                wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN)
                wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA)
                wifiConfig.allowedAuthAlgorithms.clear()
                wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP)
                wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP)
                wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40)
                wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104)
                wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP)
                wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP)
            }
            WPA_WAP2 -> {
                //WPA/WPA2 Security
                wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN)
                wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA)
                wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK)
                wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP)
                wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP)
                wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40)
                wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104)
                wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP)
                wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP)
                wifiConfig.preSharedKey = "\"$password\""
            }
            WEP -> {
                // WEP Security
                wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE)
                wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN)
                wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA)
                wifiConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN)
                wifiConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED)
                wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP)
                wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP)
                wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40)
                wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104)
                wifiConfig.wepKeys[0] = "\"$password\""
                wifiConfig.wepTxKeyIndex = 0
            }
        }
        return wifiConfig
    }

    @TargetApi(Build.VERSION_CODES.Q)
    fun connecttion(context: Context, ssid: String, bssid: String, password: String): Boolean {
        val networkSuggestion = WifiNetworkSuggestion.Builder()
            .setBssid(MacAddress.fromString(bssid))
            .setSsid(ssid)
            .setWpa2Passphrase(password)
            .build()
        (context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager).run {
            val result = addNetworkSuggestions(listOf(networkSuggestion))
            L.e("result=$result")
        }

        return true
    }

    private fun isExists(wifiManager: WifiManager, ssid: String): WifiConfiguration? {
        for (existingConfig in wifiManager.getConfiguredNetworks()) {
            if (existingConfig.SSID == "\"$ssid\"") {
                return existingConfig
            }
        }
        return null
    }

    private fun isExist(wifiManager: WifiManager, ssid: String): WifiConfiguration? {
        for (existingConfig in wifiManager.getConfiguredNetworks()) {
            if (existingConfig.SSID == "\"$ssid\"") {
                return existingConfig
            }
        }
        return null
    }

}