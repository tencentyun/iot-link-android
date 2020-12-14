## API 请求域名配置指引

以下配置需要在IoTAuth.init()之前调用

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