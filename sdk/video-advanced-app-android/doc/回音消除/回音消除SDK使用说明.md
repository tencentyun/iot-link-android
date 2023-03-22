### 1. 工程中集成回音消除库

#### 1.1 引用稳定版：

在应用模块的build.gradle中配置
```
dependencies {
    implementation 'com.tencent.iot.thirdparty.android:trae-voip-sdk:x.x.x'
}
```
具体版本号可参考[版本号列表](https://search.maven.org/search?q=trae-voip-sdk)

#### 1.2 引用SNAPSHOT版：

(1). 在工程的build.gradle中配置仓库url
```
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
(2). 在应用模块的build.gradle中配置
```
dependencies {
    implementation 'com.tencent.iot.thirdparty.android:trae-voip-sdk:x.x.x-SNAPSHOT'
}
```

**注：建议使用稳定版本，SNAPSHOT版仅供开发自测使用**


### 2. 接口使用说明

#### 2.1 初始化

##### 2.1.1 构造方法

public TraeVoip(Context context)

| 参数 | 类型 | 描述 |
|:-|:-|:-|
| context | Context | 上下文 |

##### 2.1.2 初始化方法

public boolean initVoip(int fps, int channel, boolean isLog)

| 参数 | 类型 | 描述 |
|:-|:-|:-|
| fps | int | 帧率 |
| channel | int | 通道数 |
| isLog | boolean | 是否打印日志 |

| 返回值 | 描述 |
|:-|:-|
| boolean | 是否初始化成功 |

##### 2.1.3 是否已经初始化

public boolean isVoipInit()

| 返回值 | 描述 |
|:-|:-|
| boolean | 是否已经初始化 |

#### 2.2 销毁

##### 2.2.1 析构方法

public void unInitVoip()

##### 2.2.2 重置AEC缓冲区

public void voipResetEcBuff()

#### 2.3 核心方法

##### 2.3.1 Capture方法

public boolean voipCapture(short[] pPcmIn0ut) //处理麦克风采集数据

| 参数 | 类型 | 描述 |
|:-|:-|:-|
| pPcmIn0ut | short[] | 从麦克风采集到的音频数据 |

| 返回值 | 描述 |
|:-|:-|
| boolean | 处理结果成功或失败 |

##### 2.3.2 Render方法

public boolean voipRender(short[] pPcmRef) //处理扬声器将要回放的数据

| 参数 | 类型 | 描述 |
|:-|:-|:-|
| pPcmRef | short[] | 扬声器即将播放的音频数据 |

| 返回值 | 描述 |
|:-|:-|
| boolean | 处理结果成功或失败 |



### 3. 示例代码

```
// 初始化
mVoip = new TraeVoip(getBaseContext());
if (!mVoip.isVoipInit()) {
    if (!mVoip.initVoip(frequency, 1, true)) {
        Log.e("", "Voip Init failed!");
    }
}

// 处理麦克风采集数据
audioRecord.read(pcmInOut, 0, mVoip.getFrameSizeInShort());
mVoip.voipCapture(pcmInOut);

// 处理扬声器将要回放的数据
mVoip.voipRender(pcmOut);
audioTrack.write(pcmOut, 0, mVoip.getFrameSizeInShort() * 2);


// 销毁
if (mVoip.isVoipInit()) {
    mVoip.voipResetEcBuff();
    mVoip.unInitVoip();
}
```

