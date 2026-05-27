# CLAUDE.md

本文件为 Claude Code (claude.ai/code) 在此仓库中工作时提供指引。

## 项目概述

腾讯云物联网智能视频服务 Android SDK，面向智能摄像头（IPC）设备。提供 P2P 连接（XP2P）、实时视频预览、双向语音对讲、云端/本地回放等能力。

仓库地址：`tencentyun/iot-link-android`

## 分支策略

- **`video-v2.4.x`**（主力开发分支）：更新 `video-link-android` SDK 及其 Demo（`sdkdemo` 模块），客户使用量最大。
- **`master`**：更新 `explorer-link-android` SDK 及其 Demo（`app` 模块）。
- 版本 Tag 规则：Video SDK 为 `video-v{x.y.z}`，Explorer Link SDK 为 `v{x.y.z}`。

## 构建命令

```bash
# 全量构建
./gradlew build

# 构建 sdkdemo APK（Video SDK 演示应用）
./gradlew :sdkdemo:assembleRelease
# 产物路径: sdkdemo/build/outputs/apk/release/VideoLinkDemo-release-*.apk

# 单独构建 video-link-android SDK AAR
./gradlew :sdk:video-link-android:assembleRelease

# 发布 AAR 到 Maven Central（需签名凭据）
./gradlew publishToMavenCentral -info

# 清理
./gradlew clean
```

环境要求：JDK 17，Android SDK（compileSdk 29，minSdk 21/26），NDK 27.3.13750724（sdkdemo 需要）。

## 模块结构

```
settings.gradle 包含:
├── sdk:video-link-android    # Video SDK 库（AAR），video-v2.4.x 分支重点
├── sdk:explorer-link-android # Explorer Link SDK，在 master 分支开发
├── sdk:explorer-link-rtc     # RTC 模块（使用量极少，低优先级）
├── sdkdemo                   # video-link-android SDK 的演示应用
└── app                       # explorer-link-android SDK 的演示应用
```

## 架构：video-link-android SDK

包名：`com.tencent.iot.video.link`

| 包 | 职责 |
|---|------|
| `service/VideoBaseService` | 核心 API 请求层 - 使用 TC3-HMAC-SHA256 签名，调用腾讯云 IoT Video（`iotvideo.tencentcloudapi.com`）和 IoT Explorer（`iotexplorer.tencentcloudapi.com`）接口 |
| `callback/` | 响应接口（`VideoCallback` 用于异步 API 结果回调） |
| `consts/` | 常量定义（`VideoConst` 为 SharedPreferences 键名，`VideoRequestCode` 为 API 请求码） |
| `encoder/` | 音视频硬件编码器（基于 MediaCodec） |
| `entity/` | 数据模型（设备信息、回放数据、消息类型） |
| `http/VideoHttpUtil` | HTTP POST 实现，使用 `HttpURLConnection` + Kotlin 协程 |
| `param/` | 编码参数配置（音频/视频/麦克风） |
| `util/audio/` | 音频工具：`AudioRecordUtil`、`FLVPacker`、`PCMEncoder`、`G711Code`、变声 |

核心外部依赖：
- `com.tencent.iot.thirdparty.android:xp2p-sdk` — P2P 传输层（native `.so`，JNI 调用，对外类为 `XP2P`）
- `com.tencent.iot.thirdparty.android:iot-gvoice-android` — 语音处理
- `com.tencent.iot.thirdparty.android:media-server` — 媒体服务
- `com.tencent.iot.thirdparty.android:iot-soundtouch` / `iot-voice-changer` — 音效

## 架构：sdkdemo

包名：`com.tencent.iot.explorer.link.demo`

入口：`ModuleActivity` -> `VideoOptionsActivity` -> 三条路径：
1. **云 API 方式**（`VideoInputAuthorizeActivity`）：输入 SecretId/SecretKey/ProductId，从云端拉取设备列表后进入预览/回放
2. **局域网发现**（`VideoWlanDetectActivity`）：局域网内探测设备
3. **直连 P2P**（`VideoTestInputActivity` / `MultiVideoTestInputActivity`）：直接输入设备 XP2P 信息建立连接

关键页面：
- `video/preview/VideoPreviewActivity` — 实时预览 + 对讲（使用 `IjkMediaPlayer` + `XP2PCallback`）
- `video/playback/VideoPlaybackActivity` — 云端/本地回放（含时间轴）
- `video/nvr/VideoNvrActivity` — NVR 多通道查看

Demo 中 P2P 调用流程：
1. `XP2P.setCallback(this)` — 注册事件回调
2. `XP2P.startService(context, productId, deviceName, xp2pInfo, config)` — 初始化 P2P 通道
3. 等待 `xp2pEventNotify` 事件 1004（XP2PTypeDetectReady）
4. `XP2P.delegateHttpFlv(id) + "ipc.flv?action=live"` — 获取本地代理 URL 给 ijkplayer 播放
5. 对讲：`XP2P.runSendService()` -> `XP2P.dataSend()` -> `XP2P.stopSendService()`
6. `XP2P.stopService(id)` — 退出时清理

## 配置文件

- **`sdkdemo-config.json`**（项目根目录）：SDK Demo 凭据 — `TencentIotLinkSDKDemoAppkey`、`TencentIotLinkSDKDemoAppSecret`、`TencentIotLinkVideoSDKDemoSecretId/SecretKey/ProductId`
- **`app-config.json`**（项目根目录）：完整应用凭据（微信、信鸽推送、腾讯地图等）
- **`config.gradle`**：版本号定义（`sdkVersion`、`videoSdkVersion`）
- **`parse_json.gradle`**：将 JSON 配置值读入 BuildConfig 字段的工具脚本

SDK Maven 坐标：`com.tencent.iot.video:video-link-android:{version}-SNAPSHOT`

## CI/CD

GitHub Actions 工作流（`.github/workflows/opensource.yml`）：
- 推送任意分支时触发（排除 .md 文件和 tag）
- 构建全部模块，发布 AAR 到 Maven Central（SNAPSHOT），上传 APK 产物
- 版本号从最新 git tag 自动递增（如 `video-v2.3.0` -> `2.3.1-SNAPSHOT`）

## 代码风格

- Java/Kotlin 混合代码库，新代码使用 Kotlin + 协程
- Activity 使用 ViewBinding（模式：`override fun getViewBinding()`）
- 网络请求使用 Kotlin 协程（`Dispatchers.IO` 发起请求，`Dispatchers.Main` 回调）
- API 签名遵循腾讯云 TC3 签名 v3 规范
- 支持 ABI：`arm64-v8a`、`armeabi-v7a`
