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
import com.tencent.iot.explorer.link.core.link.exception.TCLinkException
import com.tencent.iot.explorer.link.core.link.listener.BleDeviceConnectionListener
import com.tencent.iot.explorer.link.core.link.listener.BleDeviceScanResult
import java.util.*
import kotlin.collections.ArrayList

class BleConfigService private constructor() {

    private val TAG = this.javaClass.simpleName
    private var listener : BleDeviceScanResult? = null
    private var connetcedListener : BleDeviceConnectionListener? = null

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
            result?.device
            result?.scanRecord?.serviceUuids?.let {
                for (value in it) {
                    Log.e("XXX", "${result?.scanRecord?.deviceName.toString()} ---- ${value.uuid.toString().substring(4,8).toUpperCase()}")
                    if (value.uuid.toString().substring(4, 8).toUpperCase().equals("FFF0")) {
                        var dev = BleDevice()
                        dev.devName = result?.scanRecord?.deviceName.toString()
                        dev.blueDev = result?.device
                        listener?.onBleDeviceFounded(dev)
                        Log.e("XXX", "---------- ${JSON.toJSONString(dev)}")
                        var a = connectBleDevice(dev)
                        Log.e("XXX", "a - " + a)
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
        Log.e("XXX", "------ 1")
        context?:let {
            connetcedListener?.onBleDeviceDisconnected(TCLinkException("context is null"))
            return null
        }
        Log.e("XXX", "------ 2")
        dev?:let {
            connetcedListener?.onBleDeviceDisconnected(TCLinkException("dev is null"))
            return null
        }
        Log.e("XXX", "------ 3")
        dev?.blueDev?:let {
            connetcedListener?.onBleDeviceDisconnected(TCLinkException("no device to connect"))
            return null
        }

        Log.e("XXX", "------ 4")
        return dev?.blueDev?.connectGatt(context, autoConnect,
            object: BluetoothGattCallback() {
                override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
                    if (newState == BluetoothProfile.STATE_CONNECTED) {
                        Log.e("XXX", "onBleDeviceConnected")
                        connetcedListener?.onBleDeviceConnected()
                        return
                    }
                    Log.e("XXX", "onBleDeviceDisconnected")
                    connetcedListener?.onBleDeviceDisconnected(TCLinkException("diconnected"))
                }

                override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {}
                override fun onCharacteristicWrite(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {}
                override fun onCharacteristicRead(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {}
                override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {}
                override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {}
                override fun onDescriptorWrite(gatt: BluetoothGatt?, descriptor: BluetoothGattDescriptor?, status: Int) {}
            })
    }
}
