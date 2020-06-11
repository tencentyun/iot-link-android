package com.tencent.iot.explorer.link.kitlink.device;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

/**
 * Created by rongerwu on 2018/9/14.
 * Copyright (c) 2018 Tencent Cloud. All Rights Reserved.
 */
public class DeviceInfo {
    private String mProductId;
    private String mDeviceName;
    private String mSignature;
    private long mTimestamp;
    private JSONArray mErrorLogs;
    private String mErrorLogStr;

    public DeviceInfo(String json) {
        mTimestamp = System.currentTimeMillis() / 1000;
        mErrorLogs = null;
        mErrorLogStr = "";
        try {
            JSONObject obj = new JSONObject(json);
            if (obj.has("ProductId")) {
                mProductId = obj.getString("ProductId");
            }
            if (obj.has("DeviceName")) {
                mDeviceName = obj.getString("DeviceName");
            }
            if (obj.has("Signature")) {
                mSignature = obj.getString("Signature");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public DeviceInfo(final JSONObject jsonObject) throws JSONException {
        if (jsonObject.has("cmdType")) {
            int cmdType = jsonObject.getInt("cmdType");
        }
        if (jsonObject.has("productId")) {
            mProductId = jsonObject.getString("productId");
        }
        if (jsonObject.has("deviceName")) {
            mDeviceName = jsonObject.getString("deviceName");
        }
        if (jsonObject.has("signature")) {
            mSignature = jsonObject.getString("signature");
        }
        if (jsonObject.has("timestamp")) {
            mTimestamp = jsonObject.getLong("timestamp");
        }
        if (jsonObject.has("errorLogs")) {
            mErrorLogs = jsonObject.getJSONArray("errorLogs");
            String[] logArray = new String[mErrorLogs.length()];
            for (int i = 0; i < mErrorLogs.length(); i++) {
                logArray[i] = mErrorLogs.getString(i);
            }
            mErrorLogStr = Arrays.toString(logArray);
        }
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

    public long getTimestamp() {
        return mTimestamp;
    }

    public String getErrorLog() {
        return mErrorLogStr;
    }
}
