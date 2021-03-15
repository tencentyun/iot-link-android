## VideoSDK 接入使用说明

---------------------------

### 快速开始
#### 使用动态库so
a.[so下载地址](https://oss.sonatype.org/#welcome)
   路径：Repositories --> Snapshots --> 在path look up 输入框中输入com/tencent/iot/thirdparty/android --> xp2p-sdk -->版本号(1.0.0-SNAPSHOT) --> 选择最新的aar右键下载
b.工程如何引用：
   b1. 解压上一步骤下载下来的aar，目录结构如下：
    ├── assets
    │   └── appWrapper.h （头文件）
    ├── jni
    │   ├── arm64-v8a
    │   │   └── libxnet-android.so
    │   └── armeabi-v7a
    │       └── libxnet-android.so
   b2. 将头文件和so动态库放在自己工程目录下，确保CMakeList.txt可以找到对应的路径即可
   b3. 使用样例：
   ├── cpp
   │   ├── CMakeLists.txt
   │   ├── include
   │   │   └── appWrapper.h
   │   ├── libs
   │   │   ├── arm64-v8a
   │   │   │   └── libxnet-android.so
   │   │   └── armeabi-v7a
   │   │       └── libxnet-android.so
   │   └── native-lib.cpp
   在CMakeLists.txt中加上以下代码即可:
   ```
   add_library(test-lib SHARED IMPORTED)
   set_target_properties(test-lib PROPERTIES IMPORTED_LOCATION ${PROJECT_SOURCE_DIR}/../jniLibs/${ANDROID_ABI}/libxnet-android.so)
   include_directories(${PROJECT_SOURCE_DIR}/include)
   target_link_libraries( native-lib test-lib ${log-lib})
   ```
#### 使用Android aar库
a、 工程如何引用：
在工程的build.gradle中配置仓库url
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
在应用模块的build.gradle中配置
```
dependencies {
    implementation 'com.tencent.iot.video:video-link-android:1.4.0-SNAPSHOT'
}
```

### 示例代码
#### 使用使用动态库so

#### 使用Android aar库


### APP接入SDK说明
第三方App在接入Video SDK时，建议将`secretId`和`secretKey`保存到自建后台，不推荐将这两个信息保存至App端; 而SDK需要的xp2p info需要App侧从自己的业务后台获取；获取到xp2p info后，可以通过上述的`startServiceWithXp2pInfo`接口将该info传给SDK，示例代码如下：
```
...
String xp2p_info = getXP2PInfo(...) // 从自建后台获取xp2p info
XP2P.setCallback(this)
XP2P.startServiceWithXp2pInfo(xp2p_info)
```