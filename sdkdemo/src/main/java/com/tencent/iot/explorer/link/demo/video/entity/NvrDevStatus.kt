package com.tencent.iot.explorer.link.demo.video.entity

class NvrDevStatus {
    //0	接收请求
    //1	拒绝请求
    //404	错误请求
    //405	连接APP数量超过最大连接数
    var status = 0
    var appConnectNum = 2
}