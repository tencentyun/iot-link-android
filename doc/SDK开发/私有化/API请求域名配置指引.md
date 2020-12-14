## API 请求域名配置指引

以下配置需要在IoTAuth.init(APP_KEY)之前调用, 调用位置可参考[APP SDK 创建引导.md](https://github.com/tencentyun/iot-link-android/blob/master/doc/SDK%E5%BC%80%E5%8F%91/APP%20SDK%20%E5%88%9B%E5%BB%BA%E5%BC%95%E5%AF%BC.md)

1、登录前`请求API的host`配置
```
IoTAuth.setAppAPI(host)
```

2、登录后`请求API的host`配置
```
IoTAuth.setTokenAPI(host)
```

3、`WebSocket长连接host`配置
```
IoTAuth.setBrokerUrl(host)
```