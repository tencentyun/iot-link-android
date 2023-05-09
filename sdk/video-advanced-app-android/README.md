* [IoT Video Advanced App SDK开发指南](#IoT-Video-Advanced-App-SDK开发指南)
  * [引用方式](#引用方式)
  * [API说明](#API说明)
     *  [iot-video-advanced-app-android SDK 设计说明](#iot-video-advanced-app-android-SDK-设计说明)
     *  [iot-video-advanced-app-android SDK 回调callback 设计说明](#iot-video-advanced-app-android-SDK-回调callback-设计说明)

# IoT Video Advanced App SDK开发指南

本文主要介绍腾讯云物联网智能视频服务（消费版）设备端IoT Video Advanced App Android SDK的开发指南 。

## 引用方式

1、集成 SDK 方式
 -  gradle工程集成正式版SDK
     在module目录下的build.gradle中添加如下依赖，具体版本号可参考 [Latest release](https://github.com/tencentyun/iot-link-android/releases) 版本：
     ```
     dependencies {
         ...
         implementation 'com.tencent.iot.video:video-advanced-app-android:x.x.x'
     }
     ```

 -  gradle工程集成snapshot版SDK

     > 建议使用正式版SDK，SNAPSHOT版本会静默更新，使用存在风险

     在工程的build.gradle中配置仓库url
     ``` gr
     allprojects {
         repositories {
             google()
             jcenter()
             maven {
                 url "https://oss.sonatype.org/content/repositories/snapshots"
             }
         }
     }
     ```

     在应用模块的build.gradle中配置，具体版本号可参考 [Latest release](https://github.com/tencentyun/iot-link-android/releases) 版本，末位+1
     ``` gr
     dependencies {
         implementation 'com.tencent.iot.video:video-advanced-app-android:x.x.x-SNAPSHOT'
     }
     ```


## API说明

### iot-video-advanced-app-android SDK 设计说明

#### 获取RoomKey，

需先通过 [云API]( https://github.com/tencentyun/iot-link-android/blob/video-v2.6.x/sdk/video-advanced-app-android/src/main/java/com/tencent/iot/video/link/service/VideoBaseService.kt#L197-L209) 获取到链接对端参数转换成RTCParams模型，RoomKey是IoTVideoCloud中startAppWith所需链接通话参数IoTVideoParams->RTCParams，
由于云API需要配置SecretId、SecretKey，该参数直接放在客户端，会有泄漏风险，故建议通过自建服务访问该API获取到链接通话参数，再将链接通话参数传进SDK。

#### com.tencent.iot.video.link.rtc.impl.IoTVideoCloud

1、链接对端 IoTVideoCloud#startAppWith(IoTVideoParams params)

| 参数 | 类型 | 描述 |
|:-|:-|:-|
| params | IoTVideoParams | p2p模式 必传参数xp2pinfo、productid、devicename    rtc模式 必传参数RTCParams |

2、设置事件回调 IoTVideoCloud#setListener(IoTVideoCloudListener mIoTVideoCloudListener)

| 参数 | 类型 | 描述 |
|:-|:-|:-|
| mIoTVideoCloudListener | IoTVideoCloudListener | 获得来自 SDK 的各类事件通知（比如：错误码，警告码，音视频状态参数等）。 |

3、开始推送本地音视频流 IoTVideoCloud#startLocalStream(String deviceName)

| 参数 | 类型 | 描述 |
|:-|:-|:-|
| deviceName | String | 要推给某个设备的名称。 |

4、停止推送本地音视频流 IoTVideoCloud#stopLocalStream()

5、断开链接 IoTVideoCloud#stopAppService(String deviceName)

| 参数 | 类型 | 描述 |
|:-|:-|:-|
| deviceName | String | 断开链接设备的名称。 |

6、使用通道发送自定义消息给链接中的设备 IoTVideoCloud#sendCustomCmdMsg(String deviceName, String msg, long timeout_us)

| 参数 | 类型 | 描述 |
|:-|:-|:-|
| deviceName | String | 设备的名称。 |
| msg | String | 待发送的消息，rtc单个消息的最大长度被限制为 1KB。p2p 可以为任意格式字符或二进制数据(格式必须为`action=user_define&cmd=xxx`,需要传输的数据跟在`cmd=`后面)，长度由cmd_len提供，建议在16KB以内，否则会影响实时性。 |
| timeout_us | long | rtc忽略此参数。 p2p命令超时时间，单位为微秒，值为0时采用默认超时(7500ms左右) 。 |

7、打开摄像头预览 IoTVideoCloud#openCamera(boolean isFrontCamera, TXCloudVideoView txCloudVideoView)

| 参数 | 类型 | 描述 |
|:-|:-|:-|
| isFrontCamera | boolean | true：前置摄像头；false：后置摄像头。 |
| txCloudVideoView | TXCloudVideoView | 承载视频画面的控件。 |

8、关闭摄像头预览 IoTVideoCloud#closeCamera()

9、绑定远端视频渲染控件 IoTVideoCloud#startRemoteView(String userId, TXCloudVideoView txCloudVideoView)

| 参数 | 类型 | 描述 |
|:-|:-|:-|
| userId | String | 远端用户id |
| txCloudVideoView | TXCloudVideoView | 承载视频画面的控件 |

10、切换摄像头 IoTVideoCloud#changeCameraPositon(boolean isFrontCamera)

| 参数 | 类型 | 描述 |
|:-|:-|:-|
| isFrontCamera | boolean | true：前置摄像头；false：后置摄像头。 |

11、设置关闭打开麦克风 IoTVideoCloud#muteLocalAudio(boolean mute)

| 参数 | 类型 | 描述 |
|:-|:-|:-|
| mute | boolean | true：静音；false：恢复。 |

12、设置是否免提 IoTVideoCloud#setHandsFree(boolean isHandsFree)

| 参数 | 类型 | 描述 |
|:-|:-|:-|
| isHandsFree | boolean | true：免提（扬声器）；false：听筒。 |

13、设置是否适配重力感应 IoTVideoCloud#setEnableGSensor(boolean enable)

| 参数 | 类型 | 描述 |
|:-|:-|:-|
| enable | boolean | true：适配重力感应；false：不适配重力感应。 |

### iot-video-advanced-app-android SDK 回调listener 设计说明

com.tencent.iot.video.link.rtc.IoTVideoCloudListener 回调listener说明如下：

| 回调接口 | 功能 |
| ----------------------- | ---------- |
| onError(int code, String msg) | sdk内部发生了错误， code 错误码， msg 错误消息 |
| onConnect(long result)  | 链接成功与否的事件回调， result 如果加入成功，回调 result 会是一个正数（result > 0），代表链接所消耗的时间，单位是毫秒（ms），如果链接失败，回调 result 会是一个负数（result < 0），代表失败原因的错误码。|
| onRelease(int reason) | 释放链接的事件回调， reason 释放链接的原因，0：主动调用 release 释放链接；1、2：被服务器释放链接；|
| onUserEnter(String rtc_uid) | 如果有用户同意进入通话，那么会收到此回调， rtc_uid 进入通话的用户 |
| onUserLeave(String rtc_uid) | 如果有用户同意离开通话，那么会收到此回调， rtc_uid 离开通话的用户 |
| onUserVideoAvailable(String rtc_uid, boolean isVideoAvailable) | 远端用户开启/关闭了摄像头， rtc_uid 远端用户ID，isVideoAvailable true:远端用户打开摄像头  false:远端用户关闭摄像头 |
| onUserVoiceVolume(Map<String, Integer> volumeMap) | 用户说话音量回调， volumeMap 音量表，根据每个userid可以获取对应的音量大小，音量最小值0，音量最大值100 |
| onRecvCustomCmdMsg(String rtc_uid, String message) | 收到自定义消息的事件回调， rtc_uid 用户标识，message 消息数据 |
| onFirstVideoFrame(String rtc_uid, int streamType, int width, int height) | SDK 开始渲染自己本地或远端用户的首帧画面， rtc_uid 用户标识，width 画面的宽度，height 画面的高度 |
