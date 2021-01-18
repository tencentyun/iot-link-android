## 拉取demo app的日志

### 安装adb (Mac平台)
1. 执行`brew install android-platform-tools` 安装adb工具

2. 执行`adb devices` 测试adb是否安装成功


### 使用adb拉取日志
1. 确保手机打开了[开发者模式](https://jingyan.baidu.com/article/15622f24196b79fdfdbea54f.html)
2. 通过usb数据线将手机和电脑进行连接(使用adb devices命令查看手机是否成功连接到电脑)
3. 执行`adb pull /sdcard/Android/data/com.tencent.iot.explorer.link.sdkdemo/cache/`命令拉取日志到当前目录

**注：**在运行demo app时可以先通过执行下面命令清理掉旧的日志

`adb shell rm -rf /sdcard/Android/data/com.tencent.iot.explorer.link.sdkdemo/cache/`