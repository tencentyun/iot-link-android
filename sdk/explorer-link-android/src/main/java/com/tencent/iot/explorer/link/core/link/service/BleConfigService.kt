package com.tencent.iot.explorer.link.core.link.service

import android.bluetooth.BluetoothManager
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
import com.tencent.iot.explorer.link.core.link.listener.BleDeviceScanResult
import java.util.*
import kotlin.collections.ArrayList

class BleConfigService private constructor() {

    private val TAG = this.javaClass.simpleName
    private var listener : BleDeviceScanResult? = null

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
                    Log.e("XXX", "${result?.scanRecord?.deviceName.toString()} ---- ${value.uuid.toString().substring(4,8).toUpperCase()}")
                    if (value.uuid.toString().substring(4,8).toUpperCase().equals("FFF0")) {
                        var dev = BleDevice()
                        dev.devName = result?.scanRecord?.deviceName.toString()
                        listener?.onBleDeviceFounded(dev)
                        Log.e("XXX", "---------- ${JSON.toJSONString(dev)}")
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
}
