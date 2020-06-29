## 第三方定制 APP 部署流程
### 创建平台中的项目和产品
* 注册并登录[腾讯云](https://cloud.tencent.com)。
*  创建项目，选择**产品**-**物联网开发平台**，如图：   
<img src="IMG/Picture1_Iot_Platform.png" alt="Picture1_Iot_Platform.png" style="zoom: 67%;" />      
选择**新建项目**：   
<img src="IMG/Picture2_Create_Project.png" alt="Picture2_Create_Project.png" style="zoom: 67%;" />   
填写项目信息：   
<img src="IMG/Picture3_Project_Infomation.png" alt="Picture3_Project_Infomation.png" style="zoom: 80%;" />

* 创建项目后，选择**新建产品**：   
<img src="IMG/Picture4_Create_Product.png" alt="Picture4_Create_Product.png" style="zoom: 80%;" />   
填写产品信息并保存：   
<img src="IMG/Picture5_Product_Infomation.png" alt="Picture5_Product_Infomation.png" style="zoom: 80%;" />      
* 产品开发流程：   
在平台上创建虚拟产品后，可从数据模板到批量生产流程，实现配置产、测试、再实现量产的流水线，如图：
<img src="IMG/Picture6_Iot_Product_Flow.png" alt="Picture6_Iot_Product_Flow.png" style="zoom: 33%;" />

### 创建应用   
* XXXXXXX    
  
### APP 配置文件   

* 打开 **iot-link-android** 工程，该工程根目录下的 **config.json** 为配置文件，请根据实际情况调整配置信息，配置文件内容如下

<img src="IMG/image-20200619141716806.png" alt="image-20200619141716806" style="zoom:67%;" />

​	1、TencentIotLinkAppkey 和 TencentIotLinkAppSecrecy 请使用物联网平台申请的 APP Key 和 APP Secret。<font color=red>AppKey 和 AppSecret 用于访问应用端 API 时生成签名串，参见[应用端 API 简介](https://cloud.tencent.com/document/product/1081/40773)。签名算法**务必在服务端实现**，腾讯连连 App 开源版的使用方式**仅为演示**，请勿将 AppKey 和 AppSecret 保存在客户端，**避免泄露**</font>。

​	2、XgAccessId 和 XgAccessKey 请使用信鸽推送平台申请的 AccessID 和 AccessKey。

​	3、WXAccessAppId 请使用微信开放平台申请的 AppID。

​	4、TencentMapSDKValue 请使用腾讯地图开放平台申请的 key。

### APP 绑定真实设备      

* 通过 SoftAP 配网方式（自助配网）绑定真实设备，操作步骤如下：   
	1. 点击首页添加设备按钮，跳转到添加设备页面后，点击需要添加的硬件设备。   
	2. APP 自动识别设备配网为 SoftAP 配网方式，如图：   
	<img src="IMG/SoftAp/Picture8_softAp1.PNG" alt="Picture8_softAp1.PNG" style="zoom: 50%;" />   
	3. 点击底部按钮后，连接 WiFi，输入 WiFi 名称和密码，如图：   
	<img src="IMG/SoftAp/Picture9_softAp2.PNG" alt="Picture9_softAp2.PNG" style="zoom:50%;" />   
	4. 输入完后点击确认，再连接设备热点，如图：   
	<img src="IMG/SoftAp/Picture10_softAp3.PNG" alt="Picture10_softAp3.PNG" style="zoom:50%;" />   
	5. 此时跳转到“设置”中选择设备提供的热点，进行连接。
	6. 连接成功后，返回 APP，如图：   
	<img src="IMG/SoftAp/Picture11_softAp4.PNG" alt="Picture11_softAp4.PNG" style="zoom:50%;" />     
	7. 点击下一步开始连接，连接成功后，如图：   
	<img src="IMG/SoftAp/Picture12_softAp5.PNG" alt="Picture12_softAp5.PNG" style="zoom:50%;" />   
  
* 通过 SmartConfig 配网方式（智能配网）绑定真实设备，操作步骤如下：   
  
    <font color=red>注意：在此配网方式下，目前只支持 WiFi 2.4GHZ。</font>   
    
    1. 点击首页添加设备按钮，跳转到添加设备页面后，点击需要添加的硬件设备。   
    2. APP 自动识别设备配网为 SmartConfig 配网方式，如图：   
    <img src="IMG/SmartConfig/Picture13_SmartCon1.PNG" alt="Picture13_SmartCon1.PNG" style="zoom:50%;" />
    3. 点击底部按钮后，连接 WiFi，输入 WiFi 名称和密码，如图：   
    <img src="IMG/SmartConfig/Picture14_SmartCon2.PNG" alt="Picture14_SmartCon2.PNG" style="zoom:50%;" />   
    4. 点击连接成功后，如图：   
    <img src="IMG/SmartConfig/Picture15_SmartCon3.PNG" alt="Picture15_SmartCon3.PNG" style="zoom:50%;" />   
    

 APP 通过上述方式绑定真实设备后，设备状态会自动上报至物联网平台；手机端通过与物联网平台维持的长连接，可以实现实时显示设备状态。   


​	




