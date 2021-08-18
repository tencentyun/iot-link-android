package com.tencent.iot.explorer.link.rtc.model;

public class TRTCPayload {
    private String json;
    private String payload;
    private String deviceId;

    public TRTCPayload(String json, String payload, String deviceId) {
        this.json = json;
        this.payload = payload;
        this.deviceId = deviceId;
    }

    public String getJson() {
        return json;
    }

    public String getPayload() {
        return payload;
    }

    public String getDeviceId() {
        return deviceId;
    }
}
