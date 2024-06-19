package com.tencent.iot.explorer.link.core.link.service

import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.Build
import android.text.TextUtils
import androidx.annotation.RequiresApi
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.tencent.iot.explorer.link.core.auth.IoTAuth
import com.tencent.iot.explorer.link.core.auth.callback.MyCallback
import com.tencent.iot.explorer.link.core.auth.response.BaseResponse
import com.tencent.iot.explorer.link.core.link.entity.*
import com.tencent.iot.explorer.link.core.link.exception.TCLinkException
import com.tencent.iot.explorer.link.core.link.listener.BleDeviceConnectionListener
import com.tencent.iot.explorer.link.core.log.L
import com.tencent.iot.explorer.link.core.utils.CRC32
import kotlinx.coroutines.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.security.InvalidKeyException
import java.security.MessageDigest
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.collections.ArrayList
import kotlin.experimental.xor


class BleConfigService private constructor() {

    private val TAG = this.javaClass.simpleName
    var connetionListener : BleDeviceConnectionListener? = null
    var context: Context? = null
    var localPsk: ByteArray = ByteArray(4)
    @Volatile
    var currentConnectBleDevice: BleDevice? = null
    @Volatile
    var unixTimestemp = 0L
    var nonceKeep = 0L
    private val WRITEINTERVAL: Long = 500
    @Volatile
    var bluetoothGatt: BluetoothGatt? = null

    val otaByteArrList = ArrayList<ByteArray>()
    var otaTotalFileSize = 0

    private val defaultMtu = 23+3  // 连接鉴权期间LLSync认为设备使用的ATT_MTU固定是23。
    private val mtu = 23+3  // 默认的mtu size为23
    @Volatile
    var tempByteArray: ByteArray? = null

    companion object {
        private var instance: BleConfigService? = null
            get() {
                if (field == null) {
                    field = BleConfigService()
                }
                return field
            }

        fun get(): BleConfigService{
            //细心的小伙伴肯定发现了，这里不用getInstance作为为方法名，是因为在伴生对象声明时，内部已有getInstance方法，所以只能取其他名字
            return instance!!
        }

        fun getBindTag(productId: String, devName: String): ByteArray {
            var srcStr = "$productId$devName"
            var srcBuf = srcStr.toByteArray()
            var md5 = MessageDigest.getInstance("MD5")
            md5.update(srcBuf)
            var resBuf = md5.digest()
            var retArr = ByteArray(8)
            if (resBuf.size >= 16) {
                for (i in 0 until 8) {
                    retArr[i] = resBuf[i] xor resBuf[i + 8]
                }
            }
            return retArr
        }

        /**
         * 字节数组转Hex
         * @param bytes 字节数组
         * @return Hex
         */
        fun bytesToHex(bytes: ByteArray?): String? {
            val sb = StringBuffer()
            if (bytes != null && bytes.size > 0) {
                for (i in bytes.indices) {
                    val hex = byteToHex(bytes[i])
                    sb.append(hex)
                }
            }
            return sb.toString()
        }

        fun byteToHex(b: Byte): String? {
            var hexString = Integer.toHexString(b.toInt() and 0xFF)
            //由于十六进制是由0~9、A~F来表示1~16，所以如果Byte转换成Hex后如果是<16,就会是一个字符（比如A=10），通常是使用两个字符来表示16进制位的,
            //假如一个字符的话，遇到字符串11，这到底是1个字节，还是1和1两个字节，容易混淆，如果是补0，那么1和1补充后就是0101，11就表示纯粹的11
            if (hexString.length < 2) {
                hexString = java.lang.StringBuilder(0.toString()).append(hexString).toString()
            }
            return hexString.toUpperCase()
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun startScanBluetoothDevices(): Boolean {
        context?:let{ return false}

        var bluetoothmanger = context?.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        var bluetoothadapter = bluetoothmanger?.getAdapter()

        var flag = bluetoothadapter.isDiscovering()
        if (flag) return true

        flag = bluetoothadapter.startDiscovery()
        if (!flag) return false

        var filters = ArrayList<ScanFilter>()
        var scanSettings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        bluetoothadapter.bluetoothLeScanner.startScan(filters, scanSettings, getScanCallback())
        return true
    }

    private var scanCallback: ScanCallback? = null

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun getScanCallback(): ScanCallback {
        var foundedSet: ConcurrentHashMap<String, Boolean> = ConcurrentHashMap() // 用于去重
        scanCallback = object : ScanCallback() {
            override fun onBatchScanResults(results: List<ScanResult?>?) {
                L.d("onBatchScanResults: " + results.toString())
            }

            override fun onScanResult(callbackType: Int, result: ScanResult?) {
                var dev: BleDevice? = null
                result?.scanRecord?.serviceUuids?.let {
                    for (value in it) {
                        if (value.uuid.toString().substring(4, 8).toUpperCase().equals("FFF0")) {
                            L.d("onBatchScanResults: " + value.uuid.toString())
                            dev = BleDevice()
                            dev?.blueDev = result?.device
                            result?.scanRecord?.manufacturerSpecificData?.let { msd ->
                                if (msd.size() > 0) {
                                    dev?.manufacturerSpecificData = msd.valueAt(0)
                                    dev?.manufacturerSpecificData?.let { manufacturerData ->
                                        if (manufacturerData.isNotEmpty() && manufacturerData.size == 17) {
                                            val macByteArr = ByteArray(6)
                                            System.arraycopy(manufacturerData, 1, macByteArr, 0, 6)
                                            bytesToHex(macByteArr)?.let {
                                                dev?.mac = it
                                            }
                                        }
                                    }
                                }
                            }
                            dev?.devName = result?.scanRecord?.deviceName.toString()
                            dev?.showedName = result?.scanRecord?.deviceName.toString()
                            if (!dev?.showedName?.contains("-")!! && !dev?.showedName?.contains("_")!!) {
                                dev?.showedName = "${dev?.showedName}_${dev?.mac?.substring(0, 4)?.toUpperCase()}"

                            }
                            break
                        } else if (value.uuid.toString().substring(4, 8).toUpperCase().equals("FFE0")) {
                            dev = convertManufacturerData2BleDevice(result)
                            dev?.devName = result?.scanRecord?.deviceName.toString()
                            dev?.blueDev = result?.device
                            dev?.type = 1
                            break
                        }
                    }
                }

                dev?.let { bleDev ->
                    if (TextUtils.isEmpty(bleDev.productId) && TextUtils.isEmpty(bleDev.bindTag))  return@let
                    L.d(TAG, "productID ${bleDev?.productId} devName ${bleDev.devName} bindTag ${bleDev.bindTag}")
                    if (!TextUtils.isEmpty(bleDev.devName) && bleDev.devName != "null") {
                        val founded = foundedSet[bleDev.productId + bleDev.devName + bleDev.bindTag]
                        founded?:let { // 不存在对应的元素，第一次发现该设备
                            connetionListener?.onBleDeviceFounded(bleDev)
                            L.d(TAG, "onScanResults productId ${bleDev.productId} devName ${bleDev.devName}")
                            foundedSet[bleDev.productId + bleDev.devName + bleDev.bindTag] = true
                        }
                    }
                }
            }

            override fun onScanFailed(errorCode: Int) {
                L.d("onScanFailed: errorCode $errorCode")
            }
        }

        return scanCallback!!
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun convertManufacturerData2BleDevice(result: ScanResult): BleDevice {
        var dev = BleDevice()
        result?.scanRecord?.manufacturerSpecificData?.let { msd ->
            if (msd.size() > 0) {
                dev?.manufacturerSpecificData = msd.valueAt(0)
                dev?.manufacturerSpecificData?.let { manufacturerData ->
                    if (manufacturerData.isNotEmpty() && manufacturerData.size == 17) {
                        var status = manufacturerData.get(0).toInt() and 0x03
                        when (status) {
                            0, 1 -> {
                                dev?.boundState = status
                                val productByteArr = ByteArray(10)
                                System.arraycopy(manufacturerData, 7, productByteArr, 0, 10)
                                dev?.productId = String(productByteArr)

                                val macByteArr = ByteArray(6)
                                System.arraycopy(manufacturerData, 1, macByteArr, 0, 6)
                                bytesToHex(macByteArr)?.let {
                                    dev?.mac = it
                                }
                                dev?.showedName = result?.scanRecord?.deviceName.toString()
                                if (!dev.showedName.contains("-") && !dev.showedName.contains("_")) {
                                    dev.showedName = "${dev.showedName}_${dev.mac.substring(0, 4).toUpperCase()}"
                                }
                            }
                            else -> {
                                val bindTagByteArr = ByteArray(8)
                                System.arraycopy(manufacturerData, 1, bindTagByteArr, 0, 8)
                                dev?.boundState = 2
                                bytesToHex(bindTagByteArr)?.let {
                                    dev?.bindTag = it
                                }
                            }
                        }
                    }
                }
            }
        }
        return dev
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun stopScanBluetoothDevices(): Boolean {
        context?:let{ return false}
        var bluetoothmanger = context?.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        var bluetoothadapter = bluetoothmanger.getAdapter()

        if (bluetoothadapter.isDiscovering()) {
            if(!bluetoothadapter.cancelDiscovery()) return false
        }

        bluetoothadapter.bluetoothLeScanner?.stopScan(scanCallback)
        return true
    }

    fun connectBleDeviceAndGetLocalPsk(dev: BleDevice?, productId: String?, deviceName: String?): BluetoothGatt? {
        getlocalPsk(productId, deviceName)
        return connectBleDevice(dev)
    }

    fun connectBleDevice(dev: BleDevice?): BluetoothGatt? {
        return connectBleDevice(dev, false)
    }

    private fun getlocalPsk(productId: String?, deviceName: String?) {
        if (productId != null && deviceName != null) {
            IoTAuth.deviceImpl.getDeviceConfig(productId, deviceName, object: MyCallback{
                override fun fail(msg: String?, reqCode: Int) {

                }

                override fun success(response: BaseResponse, reqCode: Int) {
                    val json = response.data as JSONObject
                    val configsJson = json.getJSONObject("Configs")
                    if (configsJson == null || configsJson.isEmpty()) {
                        return
                    }
                    localPsk = configsJson.getBytes("ble_psk_device_ket")
                }
            })
        }
    }

    fun connectBleDevice(dev: BleDevice?, autoConnect: Boolean): BluetoothGatt? {
        context?:let {
            connetionListener?.onBleDeviceDisconnected(TCLinkException("context is null"))
            return null
        }
        dev?:let {
            connetionListener?.onBleDeviceDisconnected(TCLinkException("dev is null"))
            return null
        }
        dev?.blueDev?:let {
            connetionListener?.onBleDeviceDisconnected(TCLinkException("no device to connect"))
            return null
        }
        currentConnectBleDevice = dev

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            dev?.blueDev?.connectGatt(context, autoConnect, bluetoothGattCallback,
                BluetoothDevice.TRANSPORT_LE
            )
        } else {
            dev?.blueDev?.connectGatt(context, autoConnect, bluetoothGattCallback)
        }
    }

    private fun isMtuEndBlock(blockValue: ByteArray): Boolean {
        var mtuType = (blockValue[1].toInt() and 0xC0) shr 6
        if (mtuType == 0 || mtuType == 3) { // 0为不需要分片， 1位首包， 2为中间包， 3为尾包
            return true
        }
        return false
    }

    private fun appendNewBlock(newBlockValue: ByteArray, lastValue: ByteArray?): ByteArray? {
        var mtuType = (newBlockValue[1].toInt() and 0xC0) shr 6
        var tempByteArray: ByteArray? = null
        if (lastValue != null) {
            var tempLength = newBlockValue.size - 3 + lastValue.size
            tempByteArray = ByteArray(tempLength)
            System.arraycopy(lastValue, 0, tempByteArray, 0, lastValue.size)
            System.arraycopy(newBlockValue, 3, tempByteArray, lastValue.size, newBlockValue.size - 3)
        } else {
            tempByteArray = ByteArray(newBlockValue.size - 3)
            System.arraycopy(newBlockValue, 3, tempByteArray, 0, newBlockValue.size - 3)
        }
        return tempByteArray
    }

    private var bluetoothGattCallback =  object: BluetoothGattCallback() {
        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            L.e(TAG, "onBleDeviceConnected status ${status}, newState ${newState}")
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                L.d(TAG, "onBleDeviceConnected")
                gatt?.discoverServices()
                gatt?.requestConnectionPriority(BluetoothGatt.CONNECTION_PRIORITY_HIGH)
                connetionListener?.onBleDeviceConnected()
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                L.d(TAG, "onBleDeviceDisconnected")
                connetionListener?.onBleDeviceDisconnected(TCLinkException("diconnected"))
            }
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
            L.d(TAG, "onCharacteristicChanged characteristic " + JSON.toJSONString(characteristic))

            characteristic?.let {
                if (it.uuid.toString().substring(4, 8).toUpperCase().equals("FFE3")) {
                    if (it.value.isEmpty()) return
                    L.d(TAG, "characteristic ${bytesToHex(it.value)}")
                    when (it.value[0]) {
                        0x08.toByte() -> {
                            L.d(TAG, "0x08")
                            // 测试使用
                            currentConnectBleDevice?.let {  tmpBleDev ->
                                if (tmpBleDev.type == 0) {
                                    tempByteArray = appendNewBlock(it.value, tempByteArray)
                                     if (isMtuEndBlock(it.value) && tempByteArray != null) {
                                        var totalByteArr = ByteArray(tempByteArray!!.size+3)
                                        totalByteArr[0] = 0x08.toByte()
                                        var totalLengthBytes = number2Bytes(tempByteArray!!.size.toLong(), 2)
                                        System.arraycopy(totalLengthBytes, 0, totalByteArr, 1, 2)
                                        System.arraycopy(tempByteArray!!, 0, totalByteArr, 3, tempByteArray!!.size)
                                        connetionListener?.onBleDeviceInfo(BleDeviceInfo.byteArr2BleDeviceInfo(totalByteArr))
                                        tempByteArray = null
                                    } else {

                                    }
                                } else {
                                    connetionListener?.onBleDeviceFirmwareVersion(BleDeviceFirmwareVersion.byteArr2BleDeviceFirmwareVersion((it.value)))
                                }
                            }

                        }
                        0xE0.toByte() -> {
                            L.d(TAG, "0xE0 ${convertData2SetWifiResult(it.value)}")
                            connetionListener?.onBleSetWifiModeResult(convertData2SetWifiResult(it.value))
                        }
                        0xE1.toByte() -> {
                            L.d(TAG, "0xE1 ${convertData2SendWifiResult(it.value)}")
                            connetionListener?.onBleSendWifiInfoResult(convertData2SendWifiResult(it.value))
                        }
                        0xE2.toByte() -> {
                            L.d(TAG, "0xE2")
                            tempByteArray = appendNewBlock(it.value, tempByteArray)
                            if (isMtuEndBlock(it.value) && tempByteArray != null) {
                                var totalByteArr = ByteArray(tempByteArray!!.size+3)
                                totalByteArr[0] = 0xE2.toByte()
                                var totalLengthBytes = number2Bytes(tempByteArray!!.size.toLong(), 2)
                                System.arraycopy(totalLengthBytes, 0, totalByteArr, 1, 2)
                                System.arraycopy(tempByteArray!!, 0, totalByteArr, 3, tempByteArray!!.size)
                                connetionListener?.onBleWifiConnectedInfo(BleWifiConnectInfo.byteArr2BleWifiConnectInfo(totalByteArr))
                                tempByteArray = null
                            }
                        }
                        0xE3.toByte() -> {
                            L.d(TAG, "0xE3")
                            connetionListener?.onBlePushTokenResult(convertData2PushTokenResult(it.value))
                        }
                        0x05.toByte() -> {
                            L.d(TAG, "0x05")
                            tempByteArray = appendNewBlock(it.value, tempByteArray)
                            if (isMtuEndBlock(it.value) && tempByteArray != null) {
                                var totalByteArr = ByteArray(tempByteArray!!.size+3)
                                totalByteArr[0] = 0x05.toByte()
                                var totalLengthBytes = number2Bytes(tempByteArray!!.size.toLong(), 2)
                                System.arraycopy(totalLengthBytes, 0, totalByteArr, 1, 2)
                                System.arraycopy(tempByteArray!!, 0, totalByteArr, 3, tempByteArray!!.size)
                                connetionListener?.onBleBindSignInfo(BleDevBindCondition.data2BleDevBindCondition(totalByteArr))
                                tempByteArray = null
                            }
                        }
                        0x0E.toByte() -> {
                            L.d(TAG, "0x0E")
                            tempByteArray = appendNewBlock(it.value, tempByteArray)
                            if (isMtuEndBlock(it.value) && tempByteArray != null) {
                                var totalByteArr = ByteArray(tempByteArray!!.size+3)
                                totalByteArr[0] = 0x0E.toByte()
                                var totalLengthBytes = number2Bytes(tempByteArray!!.size.toLong(), 2)
                                System.arraycopy(totalLengthBytes, 0, totalByteArr, 1, 2)
                                System.arraycopy(tempByteArray!!, 0, totalByteArr, 3, tempByteArray!!.size)
                                val bleDevDynregInfo = BleDevDynregInfo.data2BleDevDynregInfo(totalByteArr)
//                                connetionListener?.onBleDynRegDeviceInfo(bleDevDynregInfo)
                                deviceDynamicRegister(gatt, bleDevDynregInfo.deviceName, bleDevDynregInfo.sign)
                                tempByteArray = null
                            }
                        }
                        0x06.toByte() -> {
                            L.d(TAG, "0x06")
                            tempByteArray = appendNewBlock(it.value, tempByteArray)
                            if (isMtuEndBlock(it.value) && tempByteArray != null) {
                                var totalByteArr = ByteArray(tempByteArray!!.size+3)
                                totalByteArr[0] = 0x06.toByte()
                                var totalLengthBytes = number2Bytes(tempByteArray!!.size.toLong(), 2)
                                System.arraycopy(totalLengthBytes, 0, totalByteArr, 1, 2)
                                System.arraycopy(tempByteArray!!, 0, totalByteArr, 3, tempByteArray!!.size)
                                connetionListener?.onBleSendSignInfo(BleDevSignResult.data2BleDevSignResult(totalByteArr))
                                sendConnectSubDeviceResult(bluetoothGatt, true)
                                tempByteArray = null
                            }
                        }
                        0x07.toByte() -> {
                            L.d(TAG, "0x07")
                            val signInfoByteArr = ByteArray(20)
                            System.arraycopy(it.value, 3, signInfoByteArr, 0, 20)
                            bytesToHex(signInfoByteArr)?.let {
                                connetionListener?.onBleUnbindSignInfo(it)
                            }
                        }
                        0x00.toByte() -> {
                            L.d(TAG, "0x00")
                            tempByteArray = appendNewBlock(it.value, tempByteArray)
                            if (isMtuEndBlock(it.value) && tempByteArray != null) {
                                var totalByteArr = ByteArray(tempByteArray!!.size+3)
                                totalByteArr[0] = 0x00.toByte()
                                var totalLengthBytes = number2Bytes(tempByteArray!!.size.toLong(), 2)
                                System.arraycopy(totalLengthBytes, 0, totalByteArr, 1, 2)
                                System.arraycopy(tempByteArray!!, 0, totalByteArr, 3, tempByteArray!!.size)
                                connetionListener?.onBlePropertyValue(BleDeviceProperty.data2BleDeviceProperty(totalByteArr))
                                tempByteArray = null
                            }
                        }
                        0x01.toByte() -> {
                            L.d(TAG, "0x01")
                            val lenByteArr = ByteArray(2)
                            System.arraycopy(it.value, 1, lenByteArr, 0, 2)
                            var len = BleDeviceProperty.bytesToInt(lenByteArr)
                            val reasonByteArr = ByteArray(len)
                            System.arraycopy(it.value, 3, reasonByteArr, 0, len)
                            connetionListener?.onBleControlPropertyResult(BleDeviceProperty.bytesToInt(reasonByteArr))
                        }
                        0x02.toByte() -> {
                            L.d(TAG, "0x02")
                            connetionListener?.onBleRequestCurrentProperty()
                        }
                        0x03.toByte() -> {
                            L.d(TAG, "0x03")
                            val lenByteArr = ByteArray(2)
                            System.arraycopy(it.value, 1, lenByteArr, 0, 2)
                            var len = BleDeviceProperty.bytesToInt(lenByteArr)
                            var valueByteArr = ByteArray(len - 1)
                            System.arraycopy(it.value, 4, valueByteArr, 0, valueByteArr.size)
                            var bleDeviceProperty = BleDeviceProperty()
                            bleDeviceProperty.valueData = valueByteArr
                            connetionListener?.onBleNeedPushProperty(it.value[3].toInt(), bleDeviceProperty)
                        }
                        0x04.toByte() -> {
                            L.d(TAG, "0x04")
                            val lenByteArr = ByteArray(2)
                            System.arraycopy(it.value, 1, lenByteArr, 0, 2)
                            var len = BleDeviceProperty.bytesToInt(lenByteArr)
                            var reason = it.value[3]
                            var actinId = it.value[4]
                            val dataBytes = ByteArray(len - 2)
                            System.arraycopy(it.value, 5, dataBytes, 0, dataBytes.size)
                            var bleDeviceProperty = BleDeviceProperty()
                            bleDeviceProperty.valueData = dataBytes
                            connetionListener?.onBleReportActionResult(reason.toInt(), actinId.toInt(), bleDeviceProperty)
                        }
                        0x0C.toByte() -> {
                            L.d(TAG, "0x0C")
                            val sizeBytes = ByteArray(2)
                            System.arraycopy(it.value, 3, sizeBytes, 0, sizeBytes.size)
                            val size = BleDeviceProperty.bytesToInt(sizeBytes)
                            connetionListener?.onBleDeviceMtuSize(size)
                        }
                        0x0D.toByte() -> {
                            L.d(TAG, "0x0D")
                            val timeLongBytes = ByteArray(2)
                            System.arraycopy(it.value, 3, timeLongBytes, 0, timeLongBytes.size)
                            val timeLong = BleDeviceProperty.bytesToInt(timeLongBytes)
                            connetionListener?.onBleDeviceTimeOut(timeLong)
                        }
                        0x09.toByte() -> {
                            L.d(TAG, "0x09")
                            connetionListener?.onBleDevOtaUpdateResponse(BleDevOtaUpdateResponse.byteArr2BleDevOtaUpdateResponse(it.value))
                        }
                        0x0A.toByte() -> {
                            L.d(TAG, "0x0A")
                            val nextSeq = it.value[3].toInt() and 0xFF
                            val fileSizeBytes = ByteArray(4)
                            System.arraycopy(it.value, 4, fileSizeBytes, 0, fileSizeBytes.size)
                            val fileSizeInt = BleDeviceProperty.bytesToTimestamp(fileSizeBytes)
                            val progress = fileSizeInt*100/otaTotalFileSize
                            connetionListener?.onBleDevOtaReceivedProgressResponse(progress)

                            startSendOtaFirmwareData(gatt)
                            if (nextSeq != 0xFF.toInt()) {
                                L.d(TAG, "ota发送完毕")
                                var endByteArr = ByteArray(1)
                                endByteArr[0] = 0x02.toByte() // 升级数据结束通知包
                                setFFE0CharacteristicValueWithUuidFFE4(gatt, endByteArr)
                            }
                        }
                        0x0B.toByte() -> {
                            L.d(TAG, "0x0B")
                            val success = it.value[3].toInt() and 0x80
                            val errorCode = it.value[3].toInt() and 0x7F
                            connetionListener?.onBleDevOtaUpdateResult((success == 1), errorCode)
                        }
                    }
                }
            }
        }
        override fun onCharacteristicWrite(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
            if (characteristic?.uuid.toString().substring(4, 8).toUpperCase().equals("FFE4") && characteristic?.value != null && characteristic.value[0] == 0x01.toByte() && otaByteArrList.size != 0) {
                val byteArr = otaByteArrList[0]
                if (byteArr.contentEquals(characteristic.value)) {
                    if (status == BluetoothGatt.GATT_SUCCESS) { //写入成功后，移除当前帧再发下一帧数据 。不成功就重发当前帧数据，看看是不是能和
                        otaByteArrList.removeAt(0)
                    }
                    if (byteArr[2] != 0xFE.toByte()) {
                        startSendOtaFirmwareData(gatt)
                    }
                }
//                L.d(TAG, "onCharacteristicFFE4Write gatt:${gatt}, characteristic: ${characteristic}, status：${status} thread:${Thread.currentThread()}")

            }
            L.d(TAG, "onCharacteristicWrite gatt:${gatt}, characteristic: ${characteristic}, characteristic value: ${bytesToHex(characteristic?.value)}, status：${status} thread:${Thread.currentThread()}")
        }
        override fun onCharacteristicRead(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
            L.d(TAG, "onCharacteristicRead gatt:${gatt}, characteristic: ${characteristic}, status：${status}")
        }
        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            enableCharacteristicNotificationWithUUid(gatt, "FFF0")
        }
        override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {
            connetionListener?.onMtuChanged(mtu, status)
        }
        override fun onDescriptorWrite(gatt: BluetoothGatt?, descriptor: BluetoothGattDescriptor?, status: Int) {
            L.d(TAG, "onDescriptorWrite gatt:${gatt}, descriptor: ${descriptor.toString()}, status：${status}")
        }
    }

    private fun convertData2SetWifiResult(byteArray: ByteArray): Boolean {
        if (byteArray.isEmpty()) return false
        if (byteArray[0] != 0xE0.toByte()) return false
        return byteArray[3].toInt() == 0
    }

    private fun convertData2SendWifiResult(byteArray: ByteArray): Boolean {
        if (byteArray.isEmpty()) return false
        if (byteArray[0] != 0xE1.toByte()) return false
        return byteArray[3].toInt() == 0
    }

    private fun convertData2PushTokenResult(byteArray: ByteArray): Boolean {
        if (byteArray.isEmpty()) return false
        if (byteArray[0] != 0xE3.toByte()) return false
        return byteArray[3].toInt() == 0
    }

    // 注册监听器
    private fun enableCharacteristicNotification(connection: BluetoothGatt?): Boolean {
        return enableCharacteristicNotificationWithUUid(connection, "FFF0")
    }

    // 注册监听器
    private fun enableCharacteristicNotificationWithUUid(connection: BluetoothGatt?, serviceUuid: String): Boolean {
        connection?.let {
            for (service in it.services) {
                if (service.uuid.toString().substring(4, 8).toUpperCase().equals(serviceUuid)) {
                    for (characteristic in service.characteristics) {
                        if (characteristic.uuid.toString().substring(4, 8).toUpperCase().equals("FFE3")) {
                            val success = connection.setCharacteristicNotification(characteristic, true)
                            if (success) {
                                val descriptorList = characteristic.descriptors
                                if (descriptorList != null && descriptorList.size > 0) {
                                    for (descriptor in descriptorList) {
                                        descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                                        connection.writeDescriptor(descriptor)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return true
        }
        return false
    }

    private fun enableFFE0CharacteristicNotification(connection: BluetoothGatt?): Boolean {
        return enableCharacteristicNotificationWithUUid(connection, "FFE0")
    }

    private fun setCharacteristicValue(connection: BluetoothGatt?, serviceUuid: String, uuid: String, byteArr: ByteArray): Boolean {
        connection?.let {
            for (service in it.services) {
                if (service.uuid.toString().substring(4, 8).toUpperCase().equals(serviceUuid)) {
                    for (characteristic in service.characteristics) {
                        if (characteristic.uuid.toString().substring(4, 8).toUpperCase().equals(uuid)) {
                            characteristic.value = byteArr
                            val success = connection.writeCharacteristic(characteristic)
                            L.d("setCharacteristicValue success = ${success} value: ${bytesToHex(byteArr)}")
                            return success
                        }
                    }
                    break
                }
            }
        }
        L.e("setCharacteristicValue error")
        return false
    }

    private fun setCharacteristicValue(connection: BluetoothGatt?, byteArr: ByteArray): Boolean {
        return setCharacteristicValue(connection, "FFF0", "FFE1", byteArr)
    }

    private fun setFFE0CharacteristicValue(connection: BluetoothGatt?, byteArr: ByteArray): Boolean {
        return setCharacteristicValue(connection, "FFE0", "FFE1", byteArr)
    }

    private fun setFFE0CharacteristicValueWithUuid(connection: BluetoothGatt?, uuid: String, byteArr: ByteArray): Boolean {
        return setCharacteristicValue(connection, "FFE0", uuid, byteArr)
    }

    private fun setFFE0CharacteristicValueWithUuidFFE4(connection: BluetoothGatt?, byteArr: ByteArray): Boolean {
        return setCharacteristicValue(connection, "FFE0", "FFE4", byteArr)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun setMtuSize(connection: BluetoothGatt?, size: Int): Boolean {
        connection?.let {
            return it.requestMtu(size)
        }
        return false
    }

    fun requestConnectWifi(connection: BluetoothGatt?): Boolean {
        return setCharacteristicValue(connection, ByteArray(1){0xE3.toByte()})
    }

    fun requestDevInfo(connection: BluetoothGatt?): Boolean {
        currentConnectBleDevice?.let {
            if (it.type == 1) {
                L.e("纯蓝牙协议流程中，不支持调用该接口")
                return false
            }
        }
        return setCharacteristicValue(connection, ByteArray(1){0xE0.toByte()})
    }

    fun setWifiMode(connection: BluetoothGatt?, mode: BleDeviceWifiMode): Boolean {
        var byteArr = ByteArray(2)
        byteArr[0] = 0xE1.toByte()
        byteArr[1] = mode.getValue()
        return setCharacteristicValue(connection, byteArr)
    }

    fun sendWifiInfo(connection: BluetoothGatt?, bleDeviceWifiInfo: BleDeviceWifiInfo): Boolean {
        return setCharacteristicValue(connection, bleDeviceWifiInfo.formatByteArr())
    }

    fun configToken(connection: BluetoothGatt?, token: String): Boolean {
        if (TextUtils.isEmpty(token)) return false
        var byteArr = ByteArray(3 + token.toByteArray().size)
        byteArr[0] = 0xE4.toByte()
        byteArr[1] = (token.toByteArray().size / Math.pow(2.0, 8.0).toInt()).toByte()
        byteArr[2] = (token.toByteArray().size % Math.pow(2.0, 8.0).toInt()).toByte()
        System.arraycopy(token.toByteArray(), 0, byteArr, 3, token.toByteArray().size)
        return setCharacteristicValue(connection, byteArr)
    }

    fun number2Bytes(number: Long, length: Int): ByteArray {
        return sumHex(number.toInt(), length)
    }

    private fun sumHex(tu5: Int, len: Int): ByteArray {
        var length = len
        val retBytes = ByteArray(length)
        while (length > 0) {
            length--
            retBytes[length] = (tu5 shr 8 * (retBytes.size - length - 1) and 0xFF).toByte()
        }
        return retBytes
    }

    fun sendUNTX(connection: BluetoothGatt?): Boolean {
        if (!enableFFE0CharacteristicNotification(connection)) return false
        Thread.sleep(WRITEINTERVAL)
        var byteArr = ByteArray(11)
        byteArr[0] = 0x00.toByte()
        byteArr[1] = 0x00.toByte()
        byteArr[2] = 0x08.toByte()
        var nonceInt = Random().nextInt(100000000).toLong()
        var nonceBytes = number2Bytes(nonceInt, 4)
        System.arraycopy(nonceBytes, 0, byteArr, 3, 4)
        unixTimestemp = System.currentTimeMillis() / 1000
        nonceKeep = nonceInt
        var tsBytes = number2Bytes(unixTimestemp, 4)
        System.arraycopy(tsBytes, 0, byteArr, 7, 4)
        return setFFE0CharacteristicValue(connection, byteArr)
    }

//    /**
//     * 启动安全绑定
//     */
//    fun enableSafeBind(connection: BluetoothGatt?, seconds: Int): Boolean {
//        if (!enableCharacteristicNotification(connection)) return false
//        var byteArr = ByteArray(5)
//        byteArr[0] = 0x0D.toByte()
//        var lenBytes = number2Bytes(2, 2)
//        System.arraycopy(lenBytes, 0, byteArr, 1, 2)
//        var secBytes = number2Bytes(seconds.toLong(), 2)
//        System.arraycopy(secBytes, 0, byteArr, 3, 2)
//        return setCharacteristicValue(connection, byteArr)
//    }

    fun sendBindfailedResult(connection: BluetoothGatt?, outTime: Boolean): Boolean {
        if (!enableFFE0CharacteristicNotification(connection)) return false
        Thread.sleep(WRITEINTERVAL)
        var byteArr = ByteArray(2)
        byteArr[0] = 0x0A.toByte()
        if (outTime) {  // 超时
            byteArr[1] = 1.toByte()
        } else { // 人为取消
            byteArr[1] = 0.toByte()
        }
        return setFFE0CharacteristicValue(connection, byteArr)
    }

    fun sendBindSuccessedResult(connection: BluetoothGatt?, devName: String): Boolean  {
        currentConnectBleDevice?.let {
            return sendBindResult(connection, it.productId, devName, true)
        }
        return false
    }

    fun sendBindSuccessedResult(connection: BluetoothGatt?, productId: String, devName: String): Boolean  {
        return sendBindResult(connection, productId, devName, true)
    }

    private fun deviceDynamicRegister(connection: BluetoothGatt?, devName: String, signature: String) {
        IoTAuth.deviceImpl.deviceDynamicRegister("${currentConnectBleDevice?.productId}/${devName}",
            unixTimestemp, nonceKeep, signature, object: MyCallback {
                override fun fail(msg: String?, reqCode: Int) {
                    L.d("deviceDynamicRegister fail")
                }

                override fun success(response: BaseResponse, reqCode: Int) {
                    L.d("deviceDynamicRegister success")
                    val json = response.data as JSONObject
                    val payload = json.getString("Payload")
                    if (payload == null || payload.isEmpty()) {
                        return
                    }
                    sendDynamicRegisterPayload(connection, payload);
                }

            })
    }

    private fun sendDynamicRegisterPayload(connection: BluetoothGatt?, payload: String) {
        var byteArr = ByteArray(10 + payload.toByteArray().size)
        // 1 表 成功
        byteArr[0] = 0x01.toByte()
        byteArr[1] = payload.toByteArray().size.toByte()
        System.arraycopy(payload.toByteArray(), 0, byteArr, 2, payload.toByteArray().size)
        val nonceBytes = number2Bytes(nonceKeep, 4)
        System.arraycopy(nonceBytes, 0, byteArr, 2+payload.toByteArray().size, nonceBytes.size)
        val tsBytes = number2Bytes(unixTimestemp, 4)
        System.arraycopy(tsBytes, 0, byteArr, 2+payload.toByteArray().size+4, tsBytes.size)

        val byteArrList = ArrayList<ByteArray>()
        var i = 0
        val interval = 17
        while (i <= byteArr.size/(interval+1)) {
            if (byteArr.size/(interval+1) == 0) { //不用分片
                val tempByteArr = ByteArray(3+byteArr.size)
                tempByteArr[0] = 0x0B.toByte()
                tempByteArr[1] = (byteArr.size / Math.pow(2.0, 8.0).toInt()).toByte()
                tempByteArr[2] = (byteArr.size % Math.pow(2.0, 8.0).toInt()).toByte()
                System.arraycopy(byteArr, 0, tempByteArr, 3, byteArr.size)
                byteArrList.add(tempByteArr)

                Thread.sleep(WRITEINTERVAL)
                setFFE0CharacteristicValue(connection, tempByteArr)
                break;//不用分片 不用再循环了
            } else { // 需要分片
                var tempByteArr = ByteArray(3+interval)
                tempByteArr[0] = 0x0B.toByte()
                if (i == 0) { //首包
                    tempByteArr[1] = (0x40 xor (interval / Math.pow(2.0, 8.0).toInt())).toByte()
                    tempByteArr[2] = (interval % Math.pow(2.0, 8.0).toInt()).toByte()
                    System.arraycopy(byteArr, i*interval, tempByteArr, 3, interval)
                } else if (byteArr.size/(interval*(i+1)+1) == 0) { //尾包
                    val lastLen = byteArr.size - interval*i
                    tempByteArr = ByteArray(3+lastLen)
                    tempByteArr[0] = 0x0B.toByte()
                    tempByteArr[1] = (0xC0 xor (lastLen / Math.pow(2.0, 8.0).toInt())).toByte()
                    tempByteArr[2] = (lastLen % Math.pow(2.0, 8.0).toInt()).toByte()
                    System.arraycopy(byteArr, i*interval, tempByteArr, 3, lastLen)
                } else { //中间包
                    tempByteArr[1] = (0x80 xor (interval / Math.pow(2.0, 8.0).toInt())).toByte()
                    tempByteArr[2] = (interval % Math.pow(2.0, 8.0).toInt()).toByte()
                    System.arraycopy(byteArr, i*interval, tempByteArr, 3, interval)
                }
                byteArrList.add(tempByteArr)
                Thread.sleep(WRITEINTERVAL)
                setFFE0CharacteristicValue(connection, tempByteArr)
                i++
            }
        }

    }

    private fun sendBindResult(connection: BluetoothGatt?, productId: String, devName: String, success: Boolean): Boolean  {
        if (!enableFFE0CharacteristicNotification(connection)) return false
        Thread.sleep(WRITEINTERVAL)
        var byteArr = ByteArray(16)
        byteArr[0] = 0x02.toByte()
        var lenByts = number2Bytes(13,2)
        System.arraycopy(lenByts, 0, byteArr, 1, 2)
        if (success) byteArr[3] = 0x02.toByte()
        var intTime = System.currentTimeMillis() / 1000
        localPsk = number2Bytes(intTime, 4)
        // 保存localPsk到云端
        IoTAuth.deviceImpl.setDeviceConfig("${productId}/${devName}",
            localPsk, object: MyCallback {
                override fun fail(msg: String?, reqCode: Int) {
                    L.d("setDeviceConfig fail")
                }

                override fun success(response: BaseResponse, reqCode: Int) {
                    L.d("setDeviceConfig success")
                }
            })
        System.arraycopy(localPsk, 0, byteArr, 4, 4)
        var bindTag = getBindTag(productId, devName)
        System.arraycopy(bindTag, 0, byteArr, 8, 8)
        return setFFE0CharacteristicValue(connection, byteArr)
    }

    fun sign(src: String, pskByts: ByteArray): ByteArray?  {
        var mac = Mac.getInstance("hmacsha1")
        val signKey = SecretKeySpec(pskByts, "hmacsha1")
        try {
            mac.init(signKey)
            return mac.doFinal(src.toByteArray())
        } catch (e: InvalidKeyException) {
            e.printStackTrace()
        }
        return null
    }

    fun connectSubDevice(connection: BluetoothGatt?): Boolean {
        if (!enableFFE0CharacteristicNotification(connection)) return false
        Thread.sleep(WRITEINTERVAL)
        var headByteArr = ByteArray(20)
        headByteArr[0] = 0x01.toByte()
        headByteArr[1] = 0x40.toByte()
        headByteArr[2] = 0x11.toByte()
        var number = System.currentTimeMillis() / 1000
        var tsBytes = number2Bytes(number, 4)
        System.arraycopy(tsBytes, 0, headByteArr, 3, 4)
        if (localPsk == ByteArray(4)) {
            //无效的localPsk，需要用户重新connect从云端获取localPsk
            return false
        }
        var signData = sign(number.toString(), localPsk)
        signData?:let { return false }
        System.arraycopy(signData, 0, headByteArr, 7, 13)
        var lastByteArr = ByteArray(10)
        lastByteArr[0] = 0x01.toByte()
        lastByteArr[1] = 0xC0.toByte()
        lastByteArr[2] = 0x07.toByte()
        System.arraycopy(signData, 13, lastByteArr, 3, 7)
        if (!setFFE0CharacteristicValue(connection, headByteArr)) {
            return false
        }
        Thread.sleep(WRITEINTERVAL)
        return setFFE0CharacteristicValue(connection, lastByteArr)
    }

    fun sendConnectSubDeviceResult(connection: BluetoothGatt?, success: Boolean): Boolean {
        Thread.sleep(WRITEINTERVAL)
        var byteArr = ByteArray(1)
        if (success) {
            byteArr[0] = 0x05.toByte()
        } else {
            byteArr[0] = 0x06.toByte()
        }
        return setFFE0CharacteristicValue(connection, byteArr)
    }

    fun unbind(connection: BluetoothGatt?): Boolean  {
        if (!enableFFE0CharacteristicNotification(connection)) return false
        Thread.sleep(WRITEINTERVAL)
        var byteArr = ByteArray(23)
        byteArr[0] = 0x04.toByte()
        byteArr[1] = 0x00.toByte()
        byteArr[2] = 0x14.toByte()
        var signData = sign("UnbindRequest", localPsk)
        signData?:let { return false }
        System.arraycopy(signData, 0, byteArr, 3, 20)
        return setFFE0CharacteristicValue(connection, byteArr)
    }

    fun sendUnbindResult(connection: BluetoothGatt?, unbindSuccessed: Boolean): Boolean  {
        if (!enableFFE0CharacteristicNotification(connection)) return false
        Thread.sleep(WRITEINTERVAL)
        var byteArr = ByteArray(1)
        byteArr[0] = if (unbindSuccessed) 0x07.toByte() else 0x08.toByte()
        return setFFE0CharacteristicValue(connection, byteArr)
    }

    /**
     * 属性上报结果
     *
     * reason 原因，0 成功，1 失败，2 数据解析错误
     */
    fun reportProperty(connection: BluetoothGatt?, reason: Int): Boolean  {
        if (!enableFFE0CharacteristicNotification(connection)) return false
        Thread.sleep(WRITEINTERVAL)
        if (reason < 0 || reason > 2) return false // 没有对应的原因值，跳出当前操作
        var byteArr = ByteArray(2)
        byteArr[0] = 0x20.toByte()
        byteArr[1] = reason.toByte()
        return setFFE0CharacteristicValueWithUuid(connection, "FFE2", byteArr)
    }

    fun controlBleDevice(connection: BluetoothGatt?, tlvByteArr: ByteArray): Boolean {
        if (!enableFFE0CharacteristicNotification(connection)) return false
        Thread.sleep(WRITEINTERVAL)
        var byteArr = ByteArray(1+2+tlvByteArr.size)
        byteArr[0] = 0x00.toByte()
        var lengthTLV = number2Bytes(tlvByteArr.size.toLong(), 2)
        System.arraycopy(lengthTLV, 0, byteArr, 1, lengthTLV.size)
        System.arraycopy(tlvByteArr, 0, byteArr, 1+lengthTLV.size, tlvByteArr.size)
        return setFFE0CharacteristicValueWithUuid(connection, "FFE2", byteArr)
    }

    fun sendCurrentBleDeviceProperty(connection: BluetoothGatt?, tlvByteArr: ByteArray): Boolean {
//        if (!enableFFE0CharacteristicNotification(connection)) return false
//        Thread.sleep(WRITEINTERVAL)
        var byteArr = ByteArray(4 + tlvByteArr.size)
        byteArr[0] = 0x22.toByte()
        byteArr[1] = 0x00.toByte()
        var lenBytes = number2Bytes(tlvByteArr.size.toLong(), 2)
        System.arraycopy(lenBytes, 0, byteArr, 2, 2)
        System.arraycopy(tlvByteArr, 0, byteArr, 4, tlvByteArr.size)
        return setFFE0CharacteristicValueWithUuid(connection, "FFE2", byteArr)
    }

    fun pushBleDevicePropertyResult(connection: BluetoothGatt?, eventId: Int, reason: Int): Boolean {
        if (!enableFFE0CharacteristicNotification(connection)) return false
        Thread.sleep(WRITEINTERVAL)
        if (reason < 0 || reason > 2) return false // 没有对应的原因值，跳出当前操作
        var byteArr = ByteArray(2)
        var value = (1 and 0x03 shl 6) or (1 and 0x01 shl 5) or (eventId and 0x1F)
        byteArr[0] = value.toByte()
        byteArr[1] = reason.toByte()
        return setFFE0CharacteristicValueWithUuid(connection, "FFE2", byteArr)
    }

    fun pushBleDeviceActionProperty(connection: BluetoothGatt?, actionId: Int, bleDeviceProperty: BleDeviceProperty): Boolean {
        if (!enableFFE0CharacteristicNotification(connection)) return false
        Thread.sleep(WRITEINTERVAL)
        var dataBytes = bleDeviceProperty.toValueData()
        var byteArr = ByteArray(3 + dataBytes.size)
        var value = (2 and 0x03 shl 6) or (0 and 0x01 shl 5) or (actionId and 0x1F)
        byteArr[0] = value.toByte()
        var lenBytes = number2Bytes(dataBytes.size.toLong(), 2)
        System.arraycopy(lenBytes, 0, byteArr, 1, 2)
        System.arraycopy(dataBytes, 0, byteArr, 3, dataBytes.size)
        return setFFE0CharacteristicValueWithUuid(connection, "FFE2", byteArr)
    }

    fun pushSetMtuResult(connection: BluetoothGatt?, successed: Boolean, result: Int): Boolean {
        if (!enableFFE0CharacteristicNotification(connection)) return false
        Thread.sleep(WRITEINTERVAL)
        var byteArr = ByteArray(3)
        byteArr[0] = 0x09.toByte()
        if (successed) {
            var sizeBytes = number2Bytes(result.toLong(), 2)
            System.arraycopy(sizeBytes, 0, byteArr, 1, 2)
        } else {
            byteArr[1] = 0xFF.toByte()
            byteArr[2] = 0xFF.toByte()
        }
        return setFFE0CharacteristicValue(connection, byteArr)
    }

    fun requestOTAInfo(connection: BluetoothGatt?, otaFilePath: String, targetVersion: String): Boolean {
//        if (!enableFFE0CharacteristicNotification(connection)) return false
        Thread.sleep(WRITEINTERVAL)
        var fileByteArr = readFile(File(otaFilePath))
        otaTotalFileSize = fileByteArr?.size!!
        var fileSizeArr = fileByteArr?.size?.toLong()?.let { number2Bytes(it, 4) }
        var fileCRC32Arr = BleConfigService.get().number2Bytes(CRC32.getCRC32(fileByteArr).toLong(), 4)
        var targetVersionByteArr = targetVersion.toByteArray()
        var targetVersionLengthArr = number2Bytes(targetVersionByteArr.size.toLong(), 1)
        if (fileSizeArr == null) return false
        val valueLength = fileSizeArr.size+fileCRC32Arr.size+targetVersionLengthArr.size+targetVersionByteArr.size
        var valueLengthArr = number2Bytes(valueLength.toLong(), 2)
        var byteArr = ByteArray(1 + valueLengthArr.size + valueLength)
        byteArr[0] = 0x00.toByte()
        System.arraycopy(valueLengthArr, 0, byteArr, 1, valueLengthArr.size)
        System.arraycopy(fileSizeArr, 0, byteArr, 1+valueLengthArr.size, fileSizeArr.size)
        System.arraycopy(fileCRC32Arr, 0, byteArr, 1+valueLengthArr.size+fileSizeArr.size, fileCRC32Arr.size)
        System.arraycopy(targetVersionLengthArr, 0, byteArr, 1+valueLengthArr.size+fileSizeArr.size+fileCRC32Arr.size, targetVersionLengthArr.size)
        System.arraycopy(targetVersionByteArr, 0, byteArr, 1+valueLengthArr.size+fileSizeArr.size+fileCRC32Arr.size+targetVersionLengthArr.size, targetVersionByteArr.size)
        L.d(TAG, "requestOTAInfo ${bytesToHex(byteArr)}")
        return setFFE0CharacteristicValueWithUuid(connection, "FFE4", byteArr)
    }

    fun sendOtaFirmwareData(connection: BluetoothGatt?, otaFilePath: String, otaUpdateResponse: BleDevOtaUpdateResponse){
//        if (!enableFFE0CharacteristicNotification(connection)) return false
        otaByteArrList.clear()
        var byteArr = ByteArray(otaUpdateResponse.packageLength)
        byteArr[0] = 0x01.toByte()
        var expectedLength = otaUpdateResponse.packageLength - 3
        var seq = 0
        val file = File(otaFilePath)
        if (file.isFile) {
            var fis: FileInputStream? = null
            try {
                fis = FileInputStream(file)
                // 设置一个，每次读取expectedLength数据长度 装载信息的容器
                val buffer = ByteArray(expectedLength)
                val outputStream = ByteArrayOutputStream()
                // 开始读取数据
                var len = 0 // 每次读取到的数据的长度
                while (fis.read(buffer).also { len = it } != -1) { // len值为-1时，表示没有数据了
                    outputStream.write(buffer, 0, len)
                    byteArr[1] = (1+len).toByte()
                    byteArr[2] = seq.toByte()
                    System.arraycopy(buffer, 0, byteArr, 3, len)
                    if (expectedLength != len+3) { //最后不满足长度expectedLength的数据包 剪裁一下
                        val lastByteArr = ByteArray(len+3)
                        System.arraycopy(byteArr, 0, lastByteArr, 0, len+3)
                        otaByteArrList.add(lastByteArr.copyOf())
                    } else {
                        otaByteArrList.add(byteArr.copyOf())
                    }
                    seq += 1
                    if (seq == otaUpdateResponse.totalPackageNumbers) {
                        seq = 0
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        startSendOtaFirmwareData(connection)
    }

    private fun startSendOtaFirmwareData(connection: BluetoothGatt?) {
        if (otaByteArrList.size != 0) {
            val byteArr = otaByteArrList[0]
            if (byteArr[2] == 0x00.toByte()) {
                Thread.sleep(2000)
            }
            setFFE0CharacteristicValueWithUuidFFE4(connection, byteArr)
        } else {
        }
    }

    private fun readFile(file: File): ByteArray? {
        // 需要读取的文件，参数是文件的路径名加文件名
        if (file.isFile) {
            // 以字节流方法读取文件
            var fis: FileInputStream? = null
            try {
                fis = FileInputStream(file)
                // 设置一个，每次 装载信息的容器
                val buffer = ByteArray(1024)
                val outputStream = ByteArrayOutputStream()
                // 开始读取数据
                var len = 0 // 每次读取到的数据的长度
                while (fis.read(buffer).also { len = it } != -1) { // len值为-1时，表示没有数据了
                    // append方法往sb对象里面添加数据
                    outputStream.write(buffer, 0, len)
                }
                // 输出字符串
                fis.close()
                outputStream.close()
                return outputStream.toByteArray()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        } else {
            println("文件不存在！")
        }
        return null
    }
}
