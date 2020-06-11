package com.tencent.iot.explorer.link.retrofit;

/**
 * 请求响应回调
 */
public interface Callback {

    void success(String json, int requestCode);

    void fail(String errorInfo, int requestCode);

}
