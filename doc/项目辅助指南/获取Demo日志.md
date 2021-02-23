## 拉取demo app的日志

### 使用android的文件管理器App查看日志文件
1. 操作路径：`文件管理器` --> `sdcard根目录` --> `p2p_logs`

2. 导出日志：使用`文件管理器`自带的分享功能即可导出

## 保存video直播场景的裸流文件

1. 启动camera端，同时启动SDKDemo

2. 在SDKDemo输入secret的页面里打开`保存裸流`的开关按钮

3. 切换到[裸流传输模式](https://github.com/tencentyun/iot-link-android/blob/master/sdk/video-link-android/doc/AndroidSDK%E8%AF%B4%E6%98%8E.md)(参考startAvRecvService接口)，选择设备并观看直播，

4. 观看结束后打开`文件管理器`，进入`sdcard根目录`，其中`raw_video.data`就是保存的裸流文件（导出方法同上述导出日志的方法）
