## 概述

该演示Demo主要演示了 [SDK](https://github.com/tencentyun/iot-link-android/tree/master/sdk) 目录下三个sdk ([LINKSDK](https://github.com/tencentyun/iot-link-android/tree/master/sdk/explorer-link-android) [RTC](https://github.com/tencentyun/iot-link-android/tree/master/sdk/explorer-link-rtc) [VIDEO](https://github.com/tencentyun/iot-link-android/tree/master/sdk/video-link-android)) 的基础功能，其中
1. `LINKSDK`主要演示设备与物联网开发平台之间建立连接、通信、关闭连接等功能；
2. `RTC`主要演示了实时音视频通话场景；
3. `VIDEO`主要演示以下几个场景：
    * 实时监控
    * 音频对讲
    * 本地回放
    * 云端存储

## Demo入口示意图
```
├── sdkdemo
│   ├── LINKSDK (explorer-link-android)
│   ├── RTC (explorer-link-rtc)
│   └── VIDEO (video-link-android)
```

## 演示Demo的执行路径
### 1. LINKSDK
待补充

### 2. RTC
待补充

### 3. VIDEO Demo

#### 操作路径：
`VIDEO ---> 播放 ---> 实时监控 or 本地回放 or云端存储`
#### 演示内容：
1. 实时监控
    * 对讲（开始对讲--->停止对讲）
    * 观看实时监控
2. 本地回放
    * 观看本地回放（可点击`停止观看`按钮停止播放）
3. 云端存储
    * 选择日期（选择要观看哪一天的远端视频）
    * 左右拖动日期下方的`时间刻度尺`，当`游标`落在绿色部分时即可观看云端视频（绿色部分代表当前日期可观看视频的时间段）
