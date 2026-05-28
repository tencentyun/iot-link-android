# AGENTS

## What This Is

腾讯云物联网智能视频服务 Android SDK，面向智能摄像头（IPC）设备。提供 P2P 连接（XP2P）、实时视频预览、双向语音对讲、云端/本地回放等能力。

## Architecture

@doc/architecture.md

## Commands

```bash
# 构建 video-link-android SDK
./gradlew :sdk:video-link-android:assembleRelease

# 构建 sdkdemo APK（Video SDK 演示应用）
./gradlew :sdkdemo:assembleDebug

# 清理
./gradlew clean
```

环境要求：JDK 17，Android SDK（compileSdk 29，minSdk 21/26），NDK 27.3.13750724（sdkdemo 需要）。

## Code Conventions

- Java/Kotlin 混合代码库，新代码使用 Kotlin + 协程
- Activity 使用 ViewBinding（模式：`override fun getViewBinding()`）
- 网络请求使用 Kotlin 协程（`Dispatchers.IO` 发起请求，`Dispatchers.Main` 回调）
- API 签名遵循腾讯云 TC3 签名 v3 规范
- 修改完代码之后主要根据 Conventional Commits 规范生成中文版本 Commit message，但不需要 scope 和 footer 部分，不需要帮我提交，格式：

    ``` plaintext
    <type>: <subject>
    // 空行
    <body>  // 如果是修复了问题，body 部分需要包括 问题根因 和 解决方案
    ```
