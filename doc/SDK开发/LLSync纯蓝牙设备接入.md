# LLSync纯蓝牙设备接入简介

LLSync纯蓝牙设备接入主要用只具有 BLE 能力的设备，利用 BLE 的无线通信能力，通过和 App 的通信。接入物联网开发平台，LLSync 协议请参考 [LLSync 协议](https://github.com/tencentyun/qcloud-iot-explorer-BLE-sdk-embedded/blob/master/docs/LLSync%E8%93%9D%E7%89%99%E8%AE%BE%E5%A4%87%E6%8E%A5%E5%85%A5%E5%8D%8F%E8%AE%AE.pdf) 。

## 登陆物联网开发平台, 创建产品及设备。

创建产品时，选择通信方式为 BLE 。
选择设备开发方式为基于标准蓝牙协议开发。
请在设备调试中，创建好设备，在设备 SDK 中烧录上创建好的设备信息，开始发送LLSync蓝牙的广播包，进行和App之间的绑定。

## 使用应用端APP，对设备进行绑定操作。

1、开始扫描 BLE设备 广播包

```
    /**
     *  设置蓝牙模块回调
     **/
    BleConfigService.get().connetionListener = object: BleDeviceConnectionListener{
        /**
         *  扫描到发送LLSync对应广播包的设备 回调
         *  @param bleDevice 扫描到发送LLSync对应广播包的设备
         **/
        override fun onBleDeviceFounded(bleDevice: BleDevice){}
    }
    /**
     *  开始扫描 BLE设备 广播包
     **/
    BleConfigService.get().startScanBluetoothDevices()
```

2、停止扫描，开始连接要绑定的 BleDevice 设备

```
    /**
     *  设置蓝牙模块回调
     **/
    BleConfigService.get().connetionListener = object: BleDeviceConnectionListener{
        /**
         *  成功连接要绑定的 BleDevice 设备回调
         **/
        override fun onBleDeviceConnected() {}
        /**
         *  连接要绑定的 BleDevice 设备失败，或已连接的 BleDevice 设备断开连接 的回调
         *  @param exception 失败异常原因
         **/
        override fun onBleDeviceDisconnected(exception: TCLinkException) {}
    }
    /**
     *  停止扫描LLSync对应广播包的设备
     **/
    BleConfigService.get().stopScanBluetoothDevices();
    /**
     *  开始连接要绑定的 BleDevice 设备，并获取绑定的密钥 
     *  @param bleDevice 要绑定的 BleDevice 设备
     *  @param presenter.getProductId() 对应的产品Id
     *  @param presenter.getDeviceName() 对应的设备名称
     **/
    BleConfigService.get().connectBleDeviceAndGetLocalPsk(bleDevice, presenter.getProductId(), presenter.getDeviceName())

```

3、发送Unix时间戳获取设备绑定信息

```
    /**
     *  设置蓝牙模块回调
     **/
    BleConfigService.get().connetionListener = object: BleDeviceConnectionListener{
        /**
         *  设备绑定签名 回调
         *  @param BleDevBindCondition 设备绑定信息
         **/
        override fun onBleBindSignInfo(bleDevBindCondition: BleDevBindCondition) {}
    }
    /**
     *  发送Unix时间戳获取设备绑定信息
     **/
    BleConfigService.get().sendUNTX(it)
```

4、请求绑定接口

```
    //请求绑定接口服务
    HttpRequest.instance.sigBindDevice(App.data.getCurrentFamily().FamilyId, App.data.getCurrentRoom().RoomId,
                        deviceInfo, "bluetooth_sign", object : MyCallback {
                            //请求服务失败回调
                            override fun fail(msg: String?, reqCode: Int) {
                                //告知蓝牙设备绑定失败
                                BleConfigService.get().sendBindfailedResult(bluetoothGatt, false)
                            }
                            //请求服务成功回调
                            override fun success(response: BaseResponse, reqCode: Int) {
                                if (!response.isSuccess()) {
                                    //服务返回绑定失败了，告知蓝牙设备绑定失败
                                    BleConfigService.get().sendBindfailedResult(bluetoothGatt, false)
                                    return
                                }
                                //告知蓝牙设备绑定成功
                                BleConfigService.get().sendBindSuccessedResult(it, bleDevBindCondition.deviceName)
                            }
```

以上绑定流程可参考 腾讯连连App中 [ConnectModel](https://github.com/tencentyun/iot-link-android/blob/8634f616fa45a0634b3bb981b12558555bd83c72/app/src/main/java/com/tencent/iot/explorer/link/mvp/model/ConnectModel.kt) 类的实现。