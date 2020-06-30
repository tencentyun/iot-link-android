package com.tencent.iot.explorer.link.core.link.entity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

/**
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
    private final int cmdType;

    public DeviceInfo(final JSONObject jsonObject) throws JSONException {
        cmdType = jsonObject.getInt("cmdType");
        mProductId = jsonObject.getString("productId");
        mDeviceName = jsonObject.getString("deviceName");
        mSignature = jsonObject.getString("signature");
        mTimestamp = jsonObject.getLong("timestamp");
        mConnId = jsonObject.getString("connId");
        mErrorLogs = jsonObject.getJSONArray("errorLogs");
        String[] logArray =  new String[mErrorLogs.length()];
        for(int i = 0; i < mErrorLogs.length(); i++){
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

    public int getCmdType() {
        return cmdType;
    }
}
