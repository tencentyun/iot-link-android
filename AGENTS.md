# AGENTS

## What This Is

腾讯连连 App SDK（explorer-link-android），为腾讯云物联网开发平台提供 Android 端设备接入能力。涵盖用户认证、设备配网（SmartConfig/SoftAP/BLE）、设备控制、家庭管理、消息推送等功能模块。

## Architecture

@doc/architecture.md

## Commands

```bash
# 构建 explorer-link-android SDK
./gradlew :sdk:explorer-link-android:assembleRelease

# 构建 app APK（Explorer SDK 演示应用）
./gradlew :app:assembleDebug

# 清理
./gradlew clean
```

环境要求：JDK 17，Android SDK（compileSdk 31，minSdk 19/21），NDK 22.1.7171670（app 需要）。

## Code Conventions

- Java/Kotlin 混合代码库，新代码使用 Kotlin + 协程
- 网络请求使用 Kotlin 协程（`Dispatchers.IO` 发起请求，`Dispatchers.Main` 回调）
- API 签名使用 HMAC-SHA1（`SignatureUtil`），通过 AppKey/AppSecret 签名后 POST 到腾讯云 IoT Explorer 平台
- 修改完代码之后主要根据 Conventional Commits 规范生成中文版本 Commit message，但不需要 scope 和 footer 部分，不需要帮我提交，格式：

    ``` plaintext
    <type>: <subject>
    // 空行
    <body>  // 如果是修复了问题，body 部分需要包括 问题根因 和 解决方案
    ```
