# Architecture

## 分支策略

- **`master`**（当前分支）：更新 `explorer-link-android` SDK 及其 Demo（`app` 模块）。
- **`video-v2.4.x`**：更新 `video-link-android` SDK 及其 Demo（`sdkdemo` 模块）。
- 版本 Tag 规则：Explorer Link SDK 为 `v{x.y.z}`，Video SDK 为 `video-v{x.y.z}`。

## 模块结构

```
settings.gradle 包含:
├── sdk:explorer-link-android # Explorer Link SDK 库（AAR），master 分支重点
├── sdk:video-link-android    # Video SDK 库（AAR），video-v2.4.x 分支开发
├── sdk:explorer-link-rtc     # RTC 模块（使用量极少，低优先级）
├── app                       # explorer-link-android SDK 的演示应用（腾讯连连开源版）
└── sdkdemo                   # video-link-android SDK 的演示应用
```

## 架构：explorer-link-android SDK

包名：`com.tencent.iot.explorer.link.core`

| 包 | 职责 |
|---|------|
| `auth/IoTAuth` | SDK 入口单例 — 初始化 AppKey/AppSecret，管理 WebSocket 连接，暴露各业务模块（`loginImpl`、`deviceImpl`、`familyImpl` 等） |
| `auth/service/` | 业务服务层 — `BaseService` 提供公共参数签名（HMAC-SHA1），各 Service（Login/Device/Family/Room/Member/Share/Timing/Message 等）封装具体 API 调用 |
| `auth/impl/` | 业务接口定义（`DeviceImpl`、`FamilyImpl`、`LoginImpl` 等），供 Demo 层调用 |
| `auth/http/HttpUtil` | HTTP GET/POST 实现，使用 `HttpURLConnection` + Kotlin 协程 |
| `auth/socket/` | WebSocket 长连接管理（`WSClientManager` + `JWebSocketClient`），用于设备状态实时推送、心跳保活、指数退避重连 |
| `auth/entity/` | 数据模型（User、DeviceEntity、FamilyEntity、RoomEntity、ControlPanel 等） |
| `auth/callback/` | 回调接口（`MyCallback`、`LoginCallback`、`DeviceCallback`、`ActivePushCallback`） |
| `auth/message/` | 消息协议（上行/下行消息封装、Payload 解析） |
| `link/configNetwork/` | 设备配网实现（SmartConfig、SoftAP、BLE、二维码配网） |
| `link/service/` | 配网服务（`SmartConfigService`、`SoftAPService`、`BleConfigService`） |
| `log/` | 日志工具 |
| `utils/` | 通用工具类 |

核心外部依赖：
- `org.java-websocket:Java-WebSocket` — WebSocket 客户端
- `com.tencent.iot.thirdparty.android:esptouch` — SmartConfig（ESP-Touch）配网
- `com.alibaba:fastjson` — JSON 序列化
- `com.google.zxing:core` — 二维码生成（配网二维码）

## 架构：app（腾讯连连开源版）

包名：`com.tencent.iot.explorer.link`

入口：`PrivicyDialogActivity`（隐私弹窗）-> `LoginActivity` -> `MainActivity`（底部 Tab 导航）

底部 Tab 结构：
- **首页**（`HomeFragment` / `DeviceFragment`）— 设备列表、设备控制面板
- **智能**（`SmartFragment`）— 智能场景/自动化任务
- **消息**（`MessageFragment`）— 告警和通知消息
- **我的**（`MeFragment`）— 个人信息、家庭管理、设置

关键业务路径：
- 设备配网：`DeviceCategoryActivity` -> `SmartConfigActivity` / `SoftAPConfigActivity` / `BleConfigActivity` -> `ConfigNetSuccessActivity`
- 设备控制：`DevicePanelActivity` / `ControlPanelActivity` — 根据物模型动态渲染面板
- 家庭管理：`FamilyListActivity` -> `FamilyActivity`（成员、房间、设备）
- 视频预览：集成了 `video-link-android` SDK，通过 P2P 连接进行 IPC 预览

App 中 SDK 调用模式：
1. `IoTAuth.init(APP_KEY, APP_SECRET)` — Application 初始化
2. `IoTAuth.loginImpl.loginPhone(...)` — 用户登录，获取 Token
3. `IoTAuth.registerActivePush(deviceIds, callback)` — 注册 WebSocket 设备监听
4. `IoTAuth.deviceImpl.controlDevice(productId, deviceName, data, callback)` — 设备控制
5. `IoTAuth.addActivePushCallback(callback)` — 接收设备状态实时推送

## 配置文件

- **`app-config.json`**（项目根目录）：App 凭据 — `TencentIotLinkAppkey`、`TencentIotLinkAppSecret`、微信 AppId、信鸽推送、腾讯地图 Key 等
- **`sdkdemo-config.json`**（项目根目录）：Video SDK Demo 凭据
- **`config.gradle`**：版本号定义（`sdkVersion`、`videoSdkVersion`）
- **`parse_json.gradle`**：将 JSON 配置值读入 BuildConfig 字段的工具脚本

SDK Maven 坐标：`com.tencent.iot.explorer:explorer-link-android:{version}-SNAPSHOT`

## CI/CD

GitHub Actions 工作流（`.github/workflows/opensource.yml`）：
- 推送任意分支时触发（排除 .md 文件和 tag）
- 构建全部模块，发布 AAR 到 Maven Central（SNAPSHOT），上传 APK 产物
- 版本号从最新 git tag 自动递增（如 `v1.4.0` -> `1.4.1-SNAPSHOT`）

