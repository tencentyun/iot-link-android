package com.tencent.iot.explorer.link.core.link.service

import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.alibaba.fastjson.JSON
import com.tencent.iot.explorer.link.core.link.entity.BleDevice
import com.tencent.iot.explorer.link.core.link.entity.BleDeviceInfo
import com.tencent.iot.explorer.link.core.link.entity.BleDeviceWifiMode
import com.tencent.iot.explorer.link.core.link.exception.TCLinkException
import com.tencent.iot.explorer.link.core.link.listener.BleDeviceConnectionListener
import com.tencent.iot.explorer.link.core.link.listener.BleDeviceScanResult
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
                        Log.e("XXX", "---------- ${JSON.toJSONString(dev)}")
                        connectBleDevice(dev)
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
                        Log.e("XXX", "onBleDeviceConnected")
                        gatt?.discoverServices()
                        connetionListener?.onBleDeviceConnected()
                        return
                    }
                    Log.e("XXX", "onBleDeviceDisconnected")
                    connetionListener?.onBleDeviceDisconnected(TCLinkException("diconnected"))
                }

                override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
                    Log.e("XXX", "onCharacteristicChanged characteristic " + JSON.toJSONString(characteristic))

                    characteristic?.let {
                        if (it.uuid.toString().substring(4, 8).toUpperCase().equals("FFE3")) {
                            if (it.value.isEmpty()) return
                            Log.e("XXX", "${it.value[0]}")
                            when (it.value[0]) {
                                0x08.toByte() -> {
                                    Log.e("XXX", "0x08")
                                    // 测试使用
                                    convertData2BleDeviceInfo(it.value)
                                    connetionListener?.onBleDeviceInfo(convertData2BleDeviceInfo(it.value))
                                }
                                0xE0.toByte() -> {
                                    Log.e("XXX", "0xE0 ${convertData2SetWifiResult(it.value)}")
                                    connetionListener?.onBleSetWifiModeResult(convertData2SetWifiResult(it.value))
                                }
                            }
                        }
                    }
                }
                override fun onCharacteristicWrite(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
                    Log.e("XXX", "onCharacteristicWrite characteristic " + JSON.toJSONString(characteristic))
                }
                override fun onCharacteristicRead(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
                    Log.e("XXX", "onCharacteristicRead characteristic " + JSON.toJSONString(characteristic))
                }
                override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {}
                override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {}
                override fun onDescriptorWrite(gatt: BluetoothGatt?, descriptor: BluetoothGattDescriptor?, status: Int) {}
            })

        Thread(Runnable {
            Thread.sleep(5000)
            var ret = false
            ret = requestDevInfo(test)
            Log.e("XXX", "requestDevInfo ${ret}")
            Thread.sleep(5000)
            ret = setWifiMode(test, BleDeviceWifiMode.STA)
            Log.e("XXX", "setWifiMode ${ret}")
        }).start()

        return test
    }

    fun convertData2BleDeviceInfo(byteArray: ByteArray): BleDeviceInfo {
        var ret = BleDeviceInfo()
        if (byteArray.isEmpty()) return ret
        if (byteArray.get(0) != 0x08.toByte()) return ret

        ret.llsyncVersion = byteArray[3].toInt().toString()
        ret.mtuFlag = if ((byteArray[4].toInt() and 0x80) == 8) 1 else 0
        ret.mtuSize = (byteArray[4].toInt() and 0x02 shl 8) or (byteArray[5].toInt() and 0xFF)
        ret.devNameLen = byteArray[6].toInt()
        val nameByteArr = ByteArray(ret.devNameLen)
        System.arraycopy(byteArray, 7, nameByteArr, 0, ret.devNameLen)
        ret.devName = String(nameByteArr)

        Log.e("XXX", "ret ${JSON.toJSONString(ret)}")
        return ret
    }

    fun convertData2SetWifiResult(byteArray: ByteArray): Boolean {
        if (byteArray.isEmpty()) return false
        if (byteArray.get(0) != 0xE0.toByte()) return false
        return byteArray[3].toInt() == 0
    }

    fun setWifiMode(connection: BluetoothGatt?, mode: BleDeviceWifiMode): Boolean {
        connection?.let {
            for (service in it.services) {
                if (service.uuid.toString().substring(4, 8).toUpperCase().equals("FFF0")) {

                    // 请求数据前，先注册监听器
                    for (characteristic in service.characteristics) {
                        if (characteristic.uuid.toString().substring(4, 8).toUpperCase().equals("FFE3")) {
                            var ret = connection.setCharacteristicNotification(characteristic, true)
                            if (!ret) return false
                        }
                    }

                    // 请求设备信息
                    for (characteristic in service.characteristics) {
                        if (characteristic.uuid.toString().substring(4, 8).toUpperCase().equals("FFE1")) {
                            var byteArr = ByteArray(2)
                            byteArr[0] = 0xE1.toByte()
                            byteArr[1] = mode.getValue()
                            characteristic.setValue(byteArr)
                            return connection.writeCharacteristic(characteristic)
                        }
                    }
                    break
                }
            }
        }
        return false
    }

    fun requestDevInfo(connection: BluetoothGatt?): Boolean {

        connection?.let {
            for (service in it.services) {
                if (service.uuid.toString().substring(4, 8).toUpperCase().equals("FFF0")) {

                    // 请求数据前，先注册监听器
                    for (characteristic in service.characteristics) {
                        if (characteristic.uuid.toString().substring(4, 8).toUpperCase().equals("FFE3")) {
                            var ret = connection.setCharacteristicNotification(characteristic, true)
                            if (!ret) return false
                        }
                    }

                    // 请求设备信息
                    for (characteristic in service.characteristics) {
                        if (characteristic.uuid.toString().substring(4, 8).toUpperCase().equals("FFE1")) {
                            var byteArr = ByteArray(1){0xE0.toByte()}
                            characteristic.setValue(byteArr)
                            return connection.writeCharacteristic(characteristic)
                        }
                    }
                    break
                }
            }
        }
        return false
    }
}
