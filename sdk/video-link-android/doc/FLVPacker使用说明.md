### FLVPacker使用简介

该工具类可以将aac和h264封装成flv格式，也可以单独封装aac音频数据、h264视频数据。[仓库地址](https://github.com/tencentyun/media-server)

#### 接入说明

 -  集成正式版SDK

    在应用模块的build.gradle中配置，具体版本号可参考 [Latest release](https://github.com/tencentyun/media-server/releases) 版本
    ``` gr
    dependencies {
        implementation 'com.tencent.iot.thirdparty.android:media-server:x.x.x'
    }
    ```
 -  集成snapshot版SDK

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
    在应用模块的build.gradle中配置，具体版本号可参考 [Latest release](https://github.com/tencentyun/media-server/releases) 版本，末位+1
    ``` gr
    dependencies {
        implementation 'com.tencent.iot.thirdparty.android:media-server:x.x.x-SNAPSHOT'
    }
    ```

#### 接口说明

1. FLVPacker构造方法

> public FLVPacker(FLVListener listener, boolean hasAudio, boolean hasVideo);

| 参数 | 类型 | 描述 |
|:-|:-|:-|
| listener | FLVListener | 封装后的flv数据的回调接口 |
| hasAudio | boolean | 待封装的二进制数据是否包含aac音频数据 |
| hasVideo | boolean | 待封装的二进制数据是否包含h264视频数据 |

| 返回值 | 描述 |
|:-|:-|
| String | 本地代理的url |

2. 封装flv的接口

> public native int encodeFlv(byte[] data, int type, long timestamp);

| 参数 | 类型 | 描述 |
|:-|:-|:-|
| data | byte[] | 原始二进制数据 |
| type | int | 标识参数data是音频数据还是视频数据，取值TYPE_AUDIO or TYPE_VIDEO |
| timestamp | long | 时间戳，传入当前时间戳即可 |

| 返回值 | 描述 |
|:-|:-|
| int | 返回码 |

3. 释放接口

> public native void release();

FLVPacker类使用完成后，调用该接口释放资源