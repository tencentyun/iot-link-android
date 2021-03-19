## 第三方定制 APP 常见问题   

* 腾讯移动推送平台相关常见开发问题   
	请先参考腾讯移动推送平台 Android 常见问题，链接如下:   
	[https://cloud.tencent.com/document/product/548/36674](https://cloud.tencent.com/document/product/548/36674)

### 其他问题   

* 腾讯移动推送其他问题   
   问题链接：  
    [https://cloud.tencent.com/document/product/548/36675](https://cloud.tencent.com/document/product/548/36675)   
* 物联网平台相关问题   
	1、一般性问题   
		   相关链接：   
		   [https://cloud.tencent.com/document/product/1081/34735](https://cloud.tencent.com/document/product/1081/34735)   
  2、控制台相关问题   
      	相关链接：   
      	[https://cloud.tencent.com/document/product/1081/34736](https://cloud.tencent.com/document/product/1081/34736)	   
  3、设备端开发问题   
      	相关链接：   
      	[https://cloud.tencent.com/document/product/1081/34737](https://cloud.tencent.com/document/product/1081/34737)   

* 微信登录功能有关问题解答   
	相关链接 :  [https://developers.weixin.qq.com/doc/oplatform/Mobile_App/WeChat_Login/Development_Guide.html](https://developers.weixin.qq.com/doc/oplatform/Mobile_App/WeChat_Login/Development_Guide.html)
* 客户问题

  1、从 Github 上拉下来的源码导入Android Studio后，编译不通过，报 com.tencent.xnet.XP2P 找不到

     问题原因：从Maven仓库拉取 XP2P 依赖时，由于网络环境不好导致拉取失败

     解决方法：在 Android Studio工具栏 点击`同步按钮`即可

  2、使用SDKDemo跑Video直播场景时，会出现串设备的问题(观看a设备直播，但实际看到的却是b设备直播)

     问题原因：在用户来回切换设备的过程中，xp2p 底层的设备ID有缓存

     解决方法：在xp2p底层重新赋值设备ID之前，清理老的设备ID，避免取到的设备ID被污染

  3、如何修改腾讯连连App的应用名称和图标

     解决方法：修改`app/src/main/AndroidManifest.xml`中的`android:icon`和`android:label`

  4、扫描二维码一键配网一直失败，但是用腾讯连连小程序可以配网成功

     问题原因：在物联网开发平台设备面板页的[交互开发](https://cloud.tencent.com/document/product/1081/40457#.E6.8E.A7.E5.88.B6.E4.BA.A7.E5.93.81.E6.96.B9.E5.BC.8F)页面只配置了小程序的配网引导，未配置APP的配网引导

     解决方法：在物联网开发平台设备面板页的[交互开发](https://cloud.tencent.com/document/product/1081/40457#.E6.8E.A7.E5.88.B6.E4.BA.A7.E5.93.81.E6.96.B9.E5.BC.8F)页面配置APP的配网引导，详细配置说明可参考[配网引导](https://cloud.tencent.com/document/product/1081/40457#.E9.85.8D.E7.BD.91.E5.BC.95.E5.AF.BC)

