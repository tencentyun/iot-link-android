package com.kitlink.device;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

/**
 * Created by rongerwu on 2018/9/14.
 * Copyright (c) 2018 Tencent Cloud. All Rights Reserved.
 */
public class DeviceInfo {
    private final String mProductId;
    private final String mDeviceName;
    private final String mSignature;
    private final long mTimestamp;
    private final JSONArray mErrorLogs;
    private final String mErrorLogStr;
    private final String mConnId;

    public DeviceInfo(String json) {
        mTimestamp = System.currentTimeMillis() / 1000;
        mConnId = "";
        mErrorLogs = null;
        mErrorLogStr = "";
        String productId = "";
        String deviceName = "";
        String signature = "";
        try {
            JSONObject obj = new JSONObject(json);
            productId = obj.getString("ProductId");
            deviceName = obj.getString("DeviceName");
            signature = obj.getString("Signature");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mProductId = productId;
        mDeviceName = deviceName;
        mSignature = signature;
    }

    public DeviceInfo(final JSONObject jsonObject) throws JSONException {
        int cmdType = jsonObject.getInt("cmdType");
        mProductId = jsonObject.getString("productId");
        mDeviceName = jsonObject.getString("deviceName");
        mSignature = jsonObject.getString("signature");
        mTimestamp = jsonObject.getLong("timestamp");
        mConnId = jsonObject.getString("connId");
        mErrorLogs = jsonObject.getJSONArray("errorLogs");
        String[] logArray = new String[mErrorLogs.length()];
        for (int i = 0; i < mErrorLogs.length(); i++) {
            logArray[i] = mErrorLogs.getString(i);
        }
        mErrorLogStr = Arrays.toString(logArray);
    }

    public String getProductId() {
        return mProductId;
    }

    public String getDeviceName() {
        return mDeviceName;
    }

    public String getSignature() {
        return mSignature;
    }

    public String getConnId() {
        return mConnId;
    }

    public long getTimestamp() {
        return mTimestamp;
    }

    public String getErrorLog() {
        return mErrorLogStr;
    }
}
