package com.tencent.iot.explorer.link.core.link.entity;

import com.alibaba.fastjson.util.Base64;
import com.tencent.iot.explorer.link.core.utils.Utils;

public class TrtcDeviceInfo {

    private String mProductId;
    private String mDeviceName;
    private String mConnId;
    private long mTimestamp;
    private String mSignMethod;
    private String mSignature;

    protected TrtcDeviceInfo() { }

    public TrtcDeviceInfo(String deviceInfo) {
        //${product_id};${device_name};${random};${timestamp};hmacsha256;sign
        String[] items = deviceInfo.split(";");
        if (items.length == 6) {
            mProductId = items[0];
            mDeviceName = items[1];
            mConnId = items[2];
            mTimestamp = Long.parseLong(items[3]);
            mSignMethod = items[4];
            mSignature = Utils.INSTANCE.bytesToHexString(Base64.decodeFast(items[5]));
        }
    }

    public void setProductId(String productId) {
        mProductId = productId;
    }

    public void setDeviceName(String deviceName) {
        mDeviceName = deviceName;
    }

    public void setTimestamp(long timestamp) {
        this.mTimestamp = timestamp;
    }

    public void setSignMethod(String signMethod) {
        this.mSignMethod = signMethod;
    }

    public void setSignature(String signature) {
        this.mSignature = signature;
    }

    public void setConnId(String connId) {
        this.mConnId = connId;
    }

    public String getProductId() { return mProductId; }
    public String getDeviceName() { return mDeviceName; }
    public String getConnId() { return mConnId; }
    public long getTimestamp() { return mTimestamp; }
    public String getSignMethod() { return mSignMethod; }
    public String getSignature() { return mSignature; }
}
