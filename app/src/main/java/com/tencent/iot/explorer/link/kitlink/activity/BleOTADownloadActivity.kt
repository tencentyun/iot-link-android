package com.tencent.iot.explorer.link.kitlink.activity

import android.os.Handler
import android.os.Message
import android.text.TextUtils
import android.view.View
import com.alibaba.fastjson.JSONObject
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.core.auth.IoTAuth
import com.tencent.iot.explorer.link.core.auth.callback.MyCallback
import com.tencent.iot.explorer.link.core.auth.entity.DeviceEntity
import com.tencent.iot.explorer.link.core.auth.response.BaseResponse
import com.tencent.iot.explorer.link.core.link.entity.*
import com.tencent.iot.explorer.link.core.link.exception.TCLinkException
import com.tencent.iot.explorer.link.core.link.listener.BleDeviceConnectionListener
import com.tencent.iot.explorer.link.core.link.service.BleConfigService
import com.tencent.iot.explorer.link.core.log.L
import com.tencent.iot.explorer.link.mvp.IPresenter
import com.tencent.iot.explorer.link.retrofit.DownloadRequest
import com.tencent.iot.explorer.link.retrofit.DownloadRequest.OnDownloadListener
import kotlinx.android.synthetic.main.activity_ble_otadownload.*
import kotlinx.android.synthetic.main.menu_back_layout.*
import kotlinx.coroutines.*
import java.io.*


class BleOTADownloadActivity : PActivity(), CoroutineScope by MainScope() {
    val MSG_DOWNLOAD_REFRESH = 0
    val MSG_DOWNLOAD_SUCCESS = 1
    val MSG_DOWNLOAD_FAILED = 2
    val MSG_UPLOAD_REFRESH = 3
    val MSG_UPLOAD_SUCCESS = 4
    val MSG_UPLOAD_FAILED = 5

    private var TAG = BleOTADownloadActivity::class.java.simpleName
    private var deviceEntity: DeviceEntity? = null
    private var connectBleJob: Job? = null

    private var targetVersion: String? = null // 要升级到的ota版本
    @Volatile
    private var downloadprogress: Int = 0// 下载进度值
    private var otaFilePath: String? = null // 下载固件时本地的存储路径

    private var mtusize = 0
    @Volatile
    private var uploadprogress: Int = 0// 写入进度值

    override fun getPresenter(): IPresenter? {
        return null
    }

    override fun onResume() {
        super.onResume()
        BleConfigService.get().connetionListener = bleDeviceConnectionListener
    }

    override fun getContentView(): Int {
        return R.layout.activity_ble_otadownload
    }

    override fun initView() {
        tv_title.text = getString(R.string.firmware_update)
        stopScanBleDev(true)
        deviceEntity = get("device")
        mtusize = get("mtusize") ?: 0
        downloadDeviceOTAInfo()
    }

    override fun setListener() {
        iv_back.setOnClickListener { finish() }
        tv_finish.setOnClickListener { finish() }
    }


    private fun startScanBleDev() {
        ble_connect_layout.visibility = View.VISIBLE
        BleConfigService.get().startScanBluetoothDevices()
        search_ble_dev_layout.visibility = View.VISIBLE
        search_reault_layout.visibility = View.GONE
    }

    private fun stopScanBleDev(connected: Boolean) {
        launch(Dispatchers.Main) {
            ble_connect_layout.visibility = View.VISIBLE
            BleConfigService.get().stopScanBluetoothDevices()
            search_ble_dev_layout.visibility = View.GONE
            search_reault_layout.visibility = View.VISIBLE
            if (connected) {
                search_reault_layout.setBackgroundResource(R.color.blue_006EFF)
                retry_connect.setTextColor(this@BleOTADownloadActivity.resources.getColor(R.color.white))
                retry_connect.setText(R.string.break_ble_connect)
                retry_connect.setOnClickListener {
                    BleConfigService.get().bluetoothGatt?.let {
                        it?.close()
                        stopScanBleDev(false)
                    }
                }
            } else {
                search_reault_layout.setBackgroundResource(R.color.red_E65A59)
                retry_connect.setTextColor(this@BleOTADownloadActivity.resources.getColor(R.color.white))
                retry_connect.setText(R.string.scanning_retry)
                retry_connect.setOnClickListener { startScanBleDev() }
            }
        }
    }

    private var bleDeviceConnectionListener = object: BleDeviceConnectionListener {
        override fun onBleDeviceFounded(bleDevice: BleDevice) {
            if (bleDevice.productId == deviceEntity?.ProductId && !TextUtils.isEmpty(bleDevice.productId)) {
                //&& bleDevice.devName == deviceEntity?.DeviceName) {
                BleConfigService.get().bluetoothGatt = BleConfigService.get().connectBleDeviceAndGetLocalPsk(bleDevice, deviceEntity?.ProductId, deviceEntity?.DeviceName)
            } else if (!TextUtils.isEmpty(bleDevice.bindTag)) {
                deviceEntity?.let {
                    if (bleDevice.bindTag == BleConfigService.bytesToHex(BleConfigService.getBindTag(it.ProductId, it.DeviceName))) {
                        BleConfigService.get().bluetoothGatt = BleConfigService.get().connectBleDeviceAndGetLocalPsk(bleDevice, deviceEntity?.ProductId, deviceEntity?.DeviceName)
                    }
                }
            }
        }

        override fun onBleDeviceConnected() {
            launch {
                BleConfigService.get().bluetoothGatt?.let {
                    delay(3000)
//                    if (BleConfigService.get().setMtuSize(it, 512)) return@launch
                    launch (Dispatchers.Main) {
                        delay(1000)
                        BleConfigService.get().bluetoothGatt?.let {
                            BleConfigService.get().stopScanBluetoothDevices()
                            if (!BleConfigService.get().connectSubDevice(it)) {
                                stopScanBleDev(false)
                                return@launch
                            } else {

                            }
                        }
                    }
                }
            }
        }
        override fun onBleDeviceDisconnected(exception: TCLinkException) {
            stopScanBleDev(false)
            launch (Dispatchers.Main) {
                startScanBleDev()
            }
        }
        override fun onBleDeviceInfo(bleDeviceInfo: BleDeviceInfo) {}
        override fun onBleSetWifiModeResult(success: Boolean) {}
        override fun onBleSendWifiInfoResult(success: Boolean) {}
        override fun onBleWifiConnectedInfo(wifiConnectInfo: BleWifiConnectInfo) {}
        override fun onBlePushTokenResult(success: Boolean) {}
        override fun onMtuChanged(mtu: Int, status: Int) {
            L.d(TAG, "onMtuChanged mtu $mtu status $status")
        }
        override fun onBleBindSignInfo(bleDevBindCondition: BleDevBindCondition) {}
        override fun onBleSendSignInfo(bleDevSignResult: BleDevSignResult) {
            stopScanBleDev(true)
            connectBleJob?.cancel()
        }
        override fun onBleUnbindSignInfo(signInfo: String) {}
        override fun onBlePropertyValue(bleDeviceProperty: BleDeviceProperty) {}
        override fun onBleControlPropertyResult(result: Int) {}
        override fun onBleRequestCurrentProperty() {}
        override fun onBleNeedPushProperty(eventId: Int, bleDeviceProperty: BleDeviceProperty) {}
        override fun onBleReportActionResult(reason: Int, actionId: Int, bleDeviceProperty: BleDeviceProperty) {}
        override fun onBleDeviceFirmwareVersion(firmwareVersion: BleDeviceFirmwareVersion) {
            if (firmwareVersion.mtuFlag == 1) { // 是否设置 mtu 当 mtu flag为 1 时，进行 MTU 设置；当 mtu flag 为 0 时，不设置 MTU
                BleConfigService.get().setMtuSize(BleConfigService.get().bluetoothGatt, firmwareVersion.mtuSize)
            }
            if (targetVersion.equals(firmwareVersion.version)) { //ble设备重启后，firmware版本和目标版本一致，判定ota升级成功
                launch(Dispatchers.Main) {
                    L.d(TAG, "ota升级成功~")
                    rl_ota_progress.visibility = View.GONE
                    rl_ota_success.visibility = View.VISIBLE
                    tv_finish.visibility = View.VISIBLE
                    tv_upgrade_success_version.text = getString(R.string.firmware_update_success_version, targetVersion)
                }
            }
        }

        override fun onBleDevOtaUpdateResponse(otaUpdateResponse: BleDevOtaUpdateResponse) {
            L.d(TAG, "onBleDevOtaUpdateResponse")
            if (otaUpdateResponse.allowUpdate) {
                otaFilePath?.let {
                    BleConfigService.get().sendOtaFirmwareData(BleConfigService.get().bluetoothGatt,
                        it, otaUpdateResponse)
                }
            }
        }

        override fun onBleDevOtaUpdateResult(success: Boolean, errorCode: Int) {
            if (success) {
                handler.sendEmptyMessage(MSG_UPLOAD_SUCCESS)
            } else {
                handler.sendEmptyMessage(MSG_UPLOAD_FAILED)
            }
        }

        override fun onBleDevOtaReceivedProgressResponse(progress: Int) {
            L.d(TAG,"progress:${progress}")
            uploadprogress = progress
            refreshProgress(false)
            reportProgress2Cloud(progress, "updating")
        }

        override fun onBleDeviceMtuSize(size: Int) {}
        override fun onBleDeviceTimeOut(timeLong: Int) {}
    }

    // 上报设备OTA状态进度 （下载、更新升级、烧录）  State: downloading updating burning
    private fun reportProgress2Cloud(progress: Int, state: String) {
        targetVersion?.let {
            IoTAuth.deviceImpl.reportOTAStatus("${deviceEntity?.ProductId}/${deviceEntity?.DeviceName}", state,
                it, progress, object: MyCallback {
                    override fun fail(msg: String?, reqCode: Int) {}

                    override fun success(response: BaseResponse, reqCode: Int) {}
                })
        }
    }

    private fun downloadDeviceOTAInfo() {
        IoTAuth.deviceImpl.getDeviceOTAInfo("${deviceEntity?.ProductId}/${deviceEntity?.DeviceName}", object: MyCallback {
            override fun fail(msg: String?, reqCode: Int) {

            }

            override fun success(response: BaseResponse, reqCode: Int) {
                val json = response.data as JSONObject
                val firmwareURL = json.getString("FirmwareURL")
                if (firmwareURL != null) {
                    targetVersion = json.getString("TargetVersion")
//                val uploadVersion = json.getString("UploadVersion")
                    if (TextUtils.isEmpty(otaFilePath)) {
                        otaFilePath = cacheDir.absolutePath + "/${deviceEntity?.ProductId}_${deviceEntity?.DeviceName}_${targetVersion}"
                    }
                    downloadOtaFirmware(firmwareURL)
                }
            }
        })
    }

    private fun downloadOtaFirmware(url: String) {
        DownloadRequest.get().download(url, otaFilePath, downloadlistener)
    }


    var downloadlistener: OnDownloadListener = object : OnDownloadListener {
        override fun onDownloadSuccess(requestId: String) {
            handler.sendEmptyMessage(MSG_DOWNLOAD_SUCCESS)
        }

        override fun onDownloading(requestId: String, progress: Int) {
            downloadprogress = progress
            refreshProgress(true)
            reportProgress2Cloud(progress, "downloading")
        }

        override fun onDownloadFailed(requestId: String) {
            handler.sendEmptyMessage(MSG_DOWNLOAD_FAILED)
        }
    }


    var handler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when(msg.what) {
                MSG_DOWNLOAD_REFRESH -> {
                    if (progress_download != null) {
                        progress_download.progress = downloadprogress.toFloat()
                        tv_upgradeing.text = getString(R.string.firmware_upgrading_tip, downloadprogress) + "%"
                    }
                    if (downloadprogress < 100) {
                        L.d(TAG, "downloadprogress:${downloadprogress}, progressBar.max:${progress_download?.max}")
//                    onDismisListener.onDownloadProgress(count, progressBar.max as Int)
                    }
                }
                MSG_DOWNLOAD_SUCCESS -> {
                    progress_download?.progress = 100f
                    L.d(TAG, "onDownloadSuccess:${otaFilePath}")
                    reportProgress2Cloud(100, "downloading")
                    otaFilePath?.let {
                        targetVersion?.let { it1 ->
                            BleConfigService.get().requestOTAInfo(BleConfigService.get().bluetoothGatt,
                                it, it1
                            )
                        }
                    }
                }
                MSG_DOWNLOAD_FAILED -> {
                    L.d(TAG, "onDownloadFailed:${otaFilePath}")
                }
                MSG_UPLOAD_REFRESH -> {
                    if (progress_download != null) {
                        progress_download.progress = uploadprogress.toFloat()
                        tv_upgradeing.text = getString(R.string.firmware_writing, uploadprogress) + "%"
                    }
                    if (downloadprogress < 100) {
                        L.d(TAG, "uploadprogress:${uploadprogress}, progressBar.max:${progress_download?.max}")
                    }
                }
                MSG_UPLOAD_SUCCESS -> {
                    progress_download?.progress = 100f
                    L.d(TAG, "onUploadSuccess:${otaFilePath}")
                    reportProgress2Cloud(100, "updating")
                }
                MSG_UPLOAD_FAILED -> {
                    L.d(TAG, "onUpFailed:${otaFilePath}")
                }
            }
        }
    }

    private fun refreshProgress(download: Boolean) { //通过url下载固件文件，还是写入固件给ble设备
        // 进度在 100 以内允许刷新
        if (downloadprogress <= 100) {
            if (download) {
                handler.sendEmptyMessage(MSG_DOWNLOAD_REFRESH)
            } else {
                handler.sendEmptyMessage(MSG_UPLOAD_REFRESH)
            }
        }
    }

}