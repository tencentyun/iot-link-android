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
import com.tencent.iot.explorer.link.core.link.entity.*
import com.tencent.iot.explorer.link.core.link.exception.TCLinkException
import com.tencent.iot.explorer.link.core.link.listener.BleDeviceConnectionListener
import com.tencent.iot.explorer.link.core.link.listener.BleDeviceScanResult
import com.tencent.iot.explorer.link.core.log.L
import java.util.*
import kotlin.collections.ArrayList

class BleConfigService private constructor() {

    private val TAG = this.javaClass.simpleName
    private var listener : BleDeviceScanResult? = null
    private var connetionListener : BleDeviceConnectionListener? = null

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
            .setReportDelay(0)
            .build()
        bluetoothadapter.bluetoothLeScanner.startScan(filters, scanSettings, scanCallback)
        return true
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    var scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)

            result?.scanRecord?.serviceUuids?.let {
                for (value in it) {
                    if (value.uuid.toString().substring(4, 8).toUpperCase().equals("FFF0")) {
                        var dev = BleDevice()
                        dev.devName = result?.scanRecord?.deviceName.toString()
                        dev.blueDev = result?.device
                        listener?.onBleDeviceFounded(dev)
                        //connectBleDevice(dev)
                        return@let
                    }
                }
            }
        }
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

        var test = dev?.blueDev?.connectGatt(context, autoConnect,
            object: BluetoothGattCallback() {
                override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
                    if (newState == BluetoothProfile.STATE_CONNECTED) {
                        L.d(TAG, "onBleDeviceConnected")
                        gatt?.discoverServices()
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

        Thread(Runnable {
            Thread.sleep(5000)
            var ret = false
//            ret = requestDevInfo(test)
//            Log.e("XXX", "requestDevInfo ${ret}")
//            Thread.sleep(5000)
//            ret = setWifiMode(test, BleDeviceWifiMode.STA)
//            Log.e("XXX", "setWifiMode ${ret}")
//            Thread.sleep(5000)
//
//            var bleDeviceWifiInfo = BleDeviceWifiInfo()
//            bleDeviceWifiInfo.ssid = "L-004"
//            bleDeviceWifiInfo.pwd = "iot2021\$"
//            ret = sendWifiInfo(test, bleDeviceWifiInfo)
//            Log.e("XXX", "sendWifiInfo ${ret}")
//
//            ret = requestConnectWifi(test)
//            Log.e("XXX", "requestConnectWifi ${ret}")
//
//            ret = configToken(test, "asdasasdqweqweqw")
//            Log.e("XXX", "configToken ${ret}")
        }).start()

        return test
    }

    fun convertData2SetWifiResult(byteArray: ByteArray): Boolean {
        if (byteArray.isEmpty()) return false
        if (byteArray[0] != 0xE0.toByte()) return false
        return byteArray[3].toInt() == 0
    }

    fun convertData2SendWifiResult(byteArray: ByteArray): Boolean {
        if (byteArray.isEmpty()) return false
        if (byteArray[0] != 0xE1.toByte()) return false
        return byteArray[3].toInt() == 0
    }

    fun convertData2PushTokenResult(byteArray: ByteArray): Boolean {
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
}
