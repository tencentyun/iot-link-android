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
import com.tencent.iot.explorer.link.core.auth.message.MessageConst
import com.tencent.iot.explorer.link.core.auth.response.BaseResponse
import com.tencent.iot.explorer.link.core.link.entity.*
import com.tencent.iot.explorer.link.core.link.exception.TCLinkException
import com.tencent.iot.explorer.link.core.link.listener.BleDeviceConnectionListener
import com.tencent.iot.explorer.link.core.log.L
import java.lang.Exception
import java.security.InvalidKeyException
import java.security.MessageDigest
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
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
    @Volatile
    var bluetoothGatt: BluetoothGatt? = null

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
            .setReportDelay(1)
            .build()

        bluetoothadapter.bluetoothLeScanner.startScan(filters, scanSettings, getScanCallback())
        return true
    }

    private var scanCallback: ScanCallback? = null

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun getScanCallback(): ScanCallback {
        var foundedSet: HashMap<String, Boolean> = HashMap() // 用于去重
        scanCallback = object : ScanCallback() {
            override fun onBatchScanResults(results: List<ScanResult?>?) {
                L.d("onBatchScanResults: " + results.toString())
                var dev: BleDevice? = null
                results?.let { ress ->
                    for (result in ress) {
                        result?.scanRecord?.serviceUuids?.let {
                            for (value in it) {
                                if (value.uuid.toString().substring(4, 8).toUpperCase().equals("FFF0")) {
                                    L.d("onBatchScanResults: " + value.uuid.toString())
                                    dev = BleDevice()
                                    dev?.devName = result?.scanRecord?.deviceName.toString()
                                    dev?.blueDev = result?.device
                                    result?.scanRecord?.manufacturerSpecificData?.let { msd ->
                                        if (msd.size() > 0) {
                                            dev?.manufacturerSpecificData = msd.valueAt(0)
                                        }
                                    }
                                    break
                                } else if (value.uuid.toString().substring(4, 8).toUpperCase().equals("FFE0")) {
                                    dev = convertManufacturerData2BleDevice(result)
                                    dev?.blueDev = result?.device
                                    dev?.type = 1
                                    break
                                }
                            }
                        }
                    }
                }

                dev?.let { bleDev ->
                    if (TextUtils.isEmpty(bleDev.productId) && TextUtils.isEmpty(bleDev.bindTag))  return@let

                    L.d(TAG, "productID ${bleDev?.productId} devName ${bleDev.devName} bindTag ${bleDev.bindTag}")
                    var founded = foundedSet.get(bleDev.productId + bleDev.devName + bleDev.bindTag)
                    founded?:let { // 不存在对应的元素，第一次发现该设备
                        connetionListener?.onBleDeviceFounded(bleDev)
                        L.d(TAG, "onScanResults productId ${bleDev.productId} devName ${bleDev.devName}")
                        foundedSet.set(bleDev.productId + bleDev.devName, true)
                    }
                }
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

    fun connectBleDevice(dev: BleDevice?): BluetoothGatt? {
        return connectBleDevice(dev, false)
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

        return dev?.blueDev?.connectGatt(context, autoConnect,
            object: BluetoothGattCallback() {
                @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
                override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
                    L.e(TAG, "onBleDeviceConnected status ${status}, newState ${newState}")
                    if (newState == BluetoothProfile.STATE_CONNECTED) {
                        L.d(TAG, "onBleDeviceConnected")
                        gatt?.discoverServices()
                        gatt?.requestConnectionPriority(BluetoothGatt.CONNECTION_PRIORITY_HIGH)
                        connetionListener?.onBleDeviceConnected()
                        return
                    }

                    L.d(TAG, "onBleDeviceDisconnected")
                    connetionListener?.onBleDeviceDisconnected(TCLinkException("diconnected"))
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
                                            connetionListener?.onBleDeviceInfo(BleDeviceInfo.byteArr2BleDeviceInfo(it.value))
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
                                    L.d(TAG, "0xE2 ${BleWifiConnectInfo.byteArr2BleWifiConnectInfo(it.value)}")
                                    connetionListener?.onBleWifiConnectedInfo(BleWifiConnectInfo.byteArr2BleWifiConnectInfo(it.value))
                                }
                                0xE3.toByte() -> {
                                    L.d(TAG, "0xE3")
                                    connetionListener?.onBlePushTokenResult(convertData2PushTokenResult(it.value))
                                }
                                0x05.toByte() -> {
                                    L.d(TAG, "0x05")
                                    connetionListener?.onBleBindSignInfo(BleDevBindCondition.data2BleDevBindCondition(it.value))
                                }
                                0x06.toByte() -> {
                                    L.d(TAG, "0x06")
                                    connetionListener?.onBleSendSignInfo(BleDevSignResult.data2BleDevSignResult(it.value))
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
                                    connetionListener?.onBlePropertyValue(BleDeviceProperty.data2BleDeviceProperty(it.value))
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
                                0x0B.toByte() -> {
                                    L.d(TAG, "0x0B")
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
                            }
                        }
                    }
                }
                override fun onCharacteristicWrite(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {}
                override fun onCharacteristicRead(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {}
                override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {}
                override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {
                    connetionListener?.onMtuChanged(mtu, status)
                }
                override fun onDescriptorWrite(gatt: BluetoothGatt?, descriptor: BluetoothGattDescriptor?, status: Int) {}
            })
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
                            return connection.setCharacteristicNotification(characteristic, true)
                        }
                    }
                }
            }
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
                            return connection.writeCharacteristic(characteristic)
                        }
                    }
                    break
                }
            }
        }
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

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun setMtuSize(connection: BluetoothGatt?, size: Int): Boolean {
        connection?.let {
            return it.requestMtu(size)
        }
        return false
    }

    fun requestConnectWifi(connection: BluetoothGatt?): Boolean {
        if (!enableCharacteristicNotification(connection)) return false
        return setCharacteristicValue(connection, ByteArray(1){0xE3.toByte()})
    }

    fun requestDevInfo(connection: BluetoothGatt?): Boolean {
        if (!enableCharacteristicNotification(connection)) return false
        currentConnectBleDevice?.let {
            if (it.type == 1) {
                L.e("纯蓝牙协议流程中，不支持调用该接口")
                return false
            }
        }
        return setCharacteristicValue(connection, ByteArray(1){0xE0.toByte()})
    }

    fun setWifiMode(connection: BluetoothGatt?, mode: BleDeviceWifiMode): Boolean {
        if (!enableCharacteristicNotification(connection)) return false
        var byteArr = ByteArray(2)
        byteArr[0] = 0xE1.toByte()
        byteArr[1] = mode.getValue()
        return setCharacteristicValue(connection, byteArr)
    }

    fun sendWifiInfo(connection: BluetoothGatt?, bleDeviceWifiInfo: BleDeviceWifiInfo): Boolean {
        if (!enableCharacteristicNotification(connection)) return false
        return setCharacteristicValue(connection, bleDeviceWifiInfo.formatByteArr())
    }

    fun configToken(connection: BluetoothGatt?, token: String): Boolean {
        if (TextUtils.isEmpty(token)) return false
        if (!enableCharacteristicNotification(connection)) return false

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

    private fun sendBindResult(connection: BluetoothGatt?, productId: String, devName: String, success: Boolean): Boolean  {
        if (!enableFFE0CharacteristicNotification(connection)) return false
        var byteArr = ByteArray(16)
        byteArr[0] = 0x02.toByte()
        var lenByts = number2Bytes(13,2)
        System.arraycopy(lenByts, 0, byteArr, 1, 2)
        if (success) byteArr[3] = 0x02.toByte()
        var intTime = System.currentTimeMillis() / 1000
        localPsk = number2Bytes(intTime, 4)
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

    fun connectSubDevice(connection: BluetoothGatt?, productId: String?, deviceName: String?): Boolean {
        if (!enableFFE0CharacteristicNotification(connection)) return false
        var byteArr = ByteArray(27)
        byteArr[0] = 0x01.toByte()
        byteArr[1] = 0x00.toByte()
        byteArr[2] = 0x18.toByte()
        var number = System.currentTimeMillis() / 1000
        var tsBytes = number2Bytes(number, 4)
        System.arraycopy(tsBytes, 0, byteArr, 3, 4)
        if (productId != null && deviceName != null) {
            var countDownLatch = CountDownLatch(1)
            val callback = object: MyCallback{
                override fun fail(msg: String?, reqCode: Int) {
                    countDownLatch.countDown()
                }

                override fun success(response: BaseResponse, reqCode: Int) {
                    val json = response.data as JSONObject
                    val configsJson = json.getJSONObject("Configs")
                    if (configsJson == null || configsJson.isEmpty()) {
                        return
                    }
                    localPsk = configsJson.getBytes("ble_psk_device_ket")
                    countDownLatch.countDown()
                }
            }
            try {
                IoTAuth.deviceImpl.getDeviceConfig(productId, deviceName, callback)
                countDownLatch.await(5, TimeUnit.SECONDS)
            } catch (e:Exception) {

            }
        }
        var signData = sign(number.toString(), localPsk)
        signData?:let { return false }
        System.arraycopy(signData, 0, byteArr, 7, 20)
        return setFFE0CharacteristicValue(connection, byteArr)
    }

    fun unbind(connection: BluetoothGatt?): Boolean  {
        if (!enableFFE0CharacteristicNotification(connection)) return false
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
        if (reason < 0 || reason > 2) return false // 没有对应的原因值，跳出当前操作
        var byteArr = ByteArray(2)
        byteArr[0] = 0x20.toByte()
        byteArr[1] = reason.toByte()
        return setFFE0CharacteristicValueWithUuid(connection, "FFE2", byteArr)
    }

    fun controlBleDevice(connection: BluetoothGatt?, bleDeviceProperty: BleDeviceProperty): Boolean {
        if (!enableFFE0CharacteristicNotification(connection)) return false
        return setFFE0CharacteristicValueWithUuid(connection, "FFE2", bleDeviceProperty.toData())
    }

    fun sendCurrentBleDeviceProperty(connection: BluetoothGatt?, bleDeviceProperty: BleDeviceProperty): Boolean {
        if (!enableFFE0CharacteristicNotification(connection)) return false
        var dataByteArr = bleDeviceProperty.toValueData()
        var byteArr = ByteArray(4 + dataByteArr.size)
        byteArr[0] = 0x22.toByte()
        byteArr[1] = 0x00.toByte()
        var lenBytes = number2Bytes(dataByteArr.size.toLong(), 2)
        System.arraycopy(lenBytes, 0, byteArr, 2, 2)
        System.arraycopy(dataByteArr, 0, byteArr, 4, dataByteArr.size)
        return setFFE0CharacteristicValueWithUuid(connection, "FFE2", byteArr)
    }

    fun pushBleDevicePropertyResult(connection: BluetoothGatt?, eventId: Int, reason: Int): Boolean {
        if (!enableFFE0CharacteristicNotification(connection)) return false
        if (reason < 0 || reason > 2) return false // 没有对应的原因值，跳出当前操作
        var byteArr = ByteArray(2)
        var value = (1 and 0x03 shl 6) or (1 and 0x01 shl 5) or (eventId and 0x1F)
        byteArr[0] = value.toByte()
        byteArr[1] = reason.toByte()
        return setFFE0CharacteristicValueWithUuid(connection, "FFE2", byteArr)
    }

    fun pushBleDeviceActionProperty(connection: BluetoothGatt?, actionId: Int, bleDeviceProperty: BleDeviceProperty): Boolean {
        if (!enableFFE0CharacteristicNotification(connection)) return false
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
}
