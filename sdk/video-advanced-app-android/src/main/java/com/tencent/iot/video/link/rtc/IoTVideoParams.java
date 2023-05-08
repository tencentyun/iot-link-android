package com.tencent.iot.video.link.rtc;

public class IoTVideoParams {
    //设置p2p模式 必传参数xp2pinfo、productid、devicename
    private String xp2pInfo;
    private String productId;
    private String deviceName;

    //设置rtc模式 必传参数RTCParams
    private RTCParams rtcParams;

    public String getXp2pInfo() {
        return xp2pInfo;
    }

    public void setXp2pInfo(String xp2pInfo) {
        this.xp2pInfo = xp2pInfo;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public RTCParams getRtcParams() {
        return rtcParams;
    }

    public void setRtcParams(RTCParams rtcParams) {
        this.rtcParams = rtcParams;
    }
}
