package com.tencent.iot.explorer.link.core.link.service

import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.Build
import android.text.TextUtils
import android.util.Log
import androidx.annotation.RequiresApi
import com.alibaba.fastjson.JSON
import com.tencent.iot.explorer.link.core.link.entity.*
import com.tencent.iot.explorer.link.core.link.exception.TCLinkException
import com.tencent.iot.explorer.link.core.link.listener.BleDeviceConnectionListener
import com.tencent.iot.explorer.link.core.log.L
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class BleConfigService private constructor() {

    private val TAG = this.javaClass.simpleName
    var connetionListener : BleDeviceConnectionListener? = null
    @Volatile
//    var dev: BleDevice? = null
    var context: Context? = null

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
                var dev: BleDevice? = null
                results?.let { ress ->
                    for (result in ress) {
                        result?.scanRecord?.serviceUuids?.let {
                            for (value in it) {
                                if (value.uuid.toString().substring(4, 8).toUpperCase().equals("FFF0")) {
                                    dev = BleDevice()
                                    dev?.devName = result?.scanRecord?.deviceName.toString()
                                    dev?.blueDev = result?.device
                                    result?.scanRecord?.manufacturerSpecificData?.let { msd ->
                                        if (msd.size() > 0) {
                                            dev?.manufacturerSpecificData = msd.valueAt(0)
                                        }
                                    }
                                    break
                                }
                            }
                        }
                    }
                }

                dev?.let { bleDev ->
                    if (TextUtils.isEmpty(bleDev.productId)) {
                        return@let
                    }

                    L.d(TAG, "productID ${dev?.productId}")
                    var founded = foundedSet.get(bleDev.productId + bleDev.devName)
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
                            L.d(TAG, "${it.value[0]}")
                            when (it.value[0]) {
                                0x08.toByte() -> {
                                    L.d(TAG, "0x08")
                                    // 测试使用
                                    BleDeviceInfo.byteArr2BleDeviceInfo(it.value)
                                    connetionListener?.onBleDeviceInfo(BleDeviceInfo.byteArr2BleDeviceInfo(it.value))
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
                            }
                        }
                    }
                }
                override fun onCharacteristicWrite(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {}
                override fun onCharacteristicRead(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {}
                override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {}
                override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {}
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
        connection?.let {
            for (service in it.services) {
                if (service.uuid.toString().substring(4, 8).toUpperCase().equals("FFF0")) {
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

    private fun setCharacteristicValue(connection: BluetoothGatt?, byteArr: ByteArray): Boolean {
        connection?.let {
            for (service in it.services) {
                if (service.uuid.toString().substring(4, 8).toUpperCase().equals("FFF0")) {

                    for (characteristic in service.characteristics) {
                        if (characteristic.uuid.toString().substring(4, 8).toUpperCase().equals("FFE1")) {
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

    fun bytes2hex(bytes: ByteArray): String? {
        val sb = StringBuilder()
        var tmp: String? = null
        for (b in bytes) {
            // 将每个字节与0xFF进行与运算，然后转化为10进制，然后借助于Integer再转化为16进制
            tmp = Integer.toHexString(0xFF and b.toInt())
            if (tmp.length == 1) {
                tmp = "0$tmp"
            }
            sb.append(tmp)
        }
        return sb.toString()
    }
}
