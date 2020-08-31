## 产品介绍

腾讯云物联网开发平台（Tencent IoT）是集智能生活、智能制造、智能人居等功能于一体的解决方案。如家庭生活类产品，智能门锁可通过 wifi 设备接入腾讯云 IoT 平台进行管理。

项目工程中包含三大块，App 体验模块、SDK Demo、SDK 模块。 用户可通过 App 体验产品功能，通过现有 App 快速搭建起属于自己的 IoT 应用。 也可通过 SDK 接入到自己的工程来完成与腾讯云物联网开发平台对接功能。

## 下载安装

[腾讯连连下载](https://github.com/tencentyun/iot-link-android/wiki/下载安装)

## 接入的第三方组件

腾讯连连是一个完整的应用项目，集成了业内主流的推送、定位、日志系统、性能统计和微信授权登录等功能。推送集成了信鸽推送，定位使用了腾讯地图，日志系统和性能统计依赖 Firebase，微信授权登录则需要微信的支持。

## 快速开始

用户需要根据实际情况调整 **app-config.json** 中的内容，app-config.json 位于项目的根目录。

app-config.json 需要配置的内容，如下：

```
  "WXAccessAppId": "",
  "TencentIotLinkAppkey": "请输入从物联网开发平台申请的 App key，正式发布前务必填写",
  "TencentIotLinkAppSecret": "请输入从物联网开发平台申请的 App Secret，App Secret 请保存在服务端，此处仅为演示，如有泄露概不负责",
  "XgAccessId": "",
  "XgAccessKey": "",
  "TencentMapSDKValue": "",
  "TencentIotLinkSDKDemoAppkey": " "
````

**1、物联网平台**

* **TencentIotLinkAppkey** 和 **TencentIotLinkAppSecret** 请使用在[物联网开发平台](https://cloud.tencent.com/product/iotexplorer)创建应用时生成的 **APP Key** 和 **APP Secret**。<u>***App Key 和 App Secret 用于访问应用端 API 时生成签名串，参见[应用端 API 简介](https://cloud.tencent.com/document/product/1081/40773)。签名算法务必在服务端实现，腾讯连连 App 开源版的使用方式仅为演示，请勿将 App Key 和 App Secret 保存在客户端，避免泄露***</u>。

**2、信鸽（可选）**

连连开源体验版集成了**信鸽推送**，用于实现消息推送。

* 若确认使用推送功能，需要前往[信鸽推送平台](https://cloud.tencent.com/product/tpns?fromSource=gwzcw.2454256.2454256.2454256&utm_medium=cpc&utm_id=gwzcw.2454256.2454256.2454256)申请获得的 **AccessID** 和 **AccessKey**，[申请步骤](https://cloud.tencent.com/product/tpns/getting-started)。
* 若不使用推送功能，**XgAccessId** 和 **XgAccessKey**  设置为**长度为0的字符串**即可。

**3、Firebase（可选）**

连连开源体验版集成了 **Firebase** 插件，用于记录应用的异常日志和性能状况。

* 若用户确认使用 Firebase 插件，需通过 [Firebase 官网](https://firebase.google.cn/?hl=zh-cn) 创建应用并获取 **google-services.json** 文件；将 google-services.json 文件放在 app 目录下。
* 若不依赖 Firebase 插件，需要在以下文件中注释掉对应依赖
  
  在项目级 build.gradle（&lt;iot-link-android&gt;/build.gradle）中注释掉dependencies中以下三个依赖项
  
  ```
  dependencies {
  //        classpath 'com.google.gms:google-services:4.3.3'
  //        classpath 'com.google.firebase:firebase-crashlytics-gradle:2.1.1'
  //        classpath 'com.google.firebase:perf-plugin:1.3.1'
  }
  ```
  
  在应用级 build.gradle（&lt;iot-link-android&gt;/&lt;app&gt;/build.gradle）中注释掉以下三个应用插件和三个依赖项
  
  ```
  //apply plugin: 'com.google.gms.google-services'
  //apply plugin: 'com.google.firebase.crashlytics'
  //apply plugin: 'com.google.firebase.firebase-perf'
  
  dependencies {
  //    implementation 'com.google.firebase:firebase-analytics-ktx:17.4.3'
  //    implementation 'com.google.firebase:firebase-crashlytics:17.0.1'
  //    implementation 'com.google.firebase:firebase-perf:19.0.7'
  }
  ```

**4、腾讯地图（可选）**

连连开源体验版集成了**腾讯地图**，用于实现定位。

* 若确认使用自定义的定位功能，需要前往[腾讯地图开放平台](https://lbs.qq.com/)申请获得 **key**，[申请步骤](https://lbs.qq.com/mobile/androidLocationSDK/androidGeoGuide/androidGeoCreat)。
* 若确认使用默认定位功能，无需修改 **TencentMapSDKValue** 配置项内容 。
* 若不使用定位功能，**TencentMapSDKValue** 设置为**长度为0的字符串**即可。

**5、微信授权登录（可选）**

连连开源体验版集成了微信授权登录。

* 若确认使用自定义的微信授权登录，需要在[微信开放平台](https://open.weixin.qq.com/)注册开发者帐号，创建移动应用，审核通过后，即可获得相应的 AppID 和 AppSecret，[申请步骤](https://developers.weixin.qq.com/doc/oplatform/Mobile_App/WeChat_Login/Development_Guide.html)；同时需要自行搭建微信授权登录的**接入服务器**，可参考接入服务器的[接口](https://cloud.tencent.com/document/product/1081/40781)。
  
  使用微信授权登录还需：
  
  - 将 *opensource_keystore.jks* 文件替换成自己的签名文件并给应用签字
  - 前往[微信开放平台](https://developers.weixin.qq.com/doc/oplatform/Downloads/Android_Resource.html)下载签名生成工具，使用该工具生成应用的数字签名(需要将该工具和应用同时安装到手机上，打开签名生成工具输入应用包名即可生成数字签名，如下图所示)
    
    <img src="https://main.qcloudimg.com/raw/e5734b5731d77e8b1e271cbd78bb5fcf.png" alt="image-20200619162858817" width = "35%" height = "50%" />
  - 将该数字签名和应用包名登记到微信开放平台，否则微信授权登录将不可用
  
  最后将配置项 **WXAccessAppId** 设置为在微信开放平台申请并获得的 **AppID**；***<u>同时请遵从官方建议自建微信接入服务器，保证 AppSecret 不被泄露</u>***。
* 若不使用微信授权登录功能，**WXAccessAppId** 设置为**长度为0的字符串**即可。

完成上述配置后，依赖 Android studio 的构建，即可在手机上运行。


