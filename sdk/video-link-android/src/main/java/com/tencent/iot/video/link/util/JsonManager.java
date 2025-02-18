package com.tencent.iot.video.link.util;


import android.util.Log;

import com.google.gson.Gson;

public class JsonManager {

    /**
     * json格式转对象   245
     */
    public static <T> T parseJson(String json, Class<T> clazz) {
        try {
            return new Gson().fromJson(json, clazz);
        } catch (Exception e) {
            Log.e("ParseJsonEntity", "fastjson解析出错：" + e.getMessage() + "\n原因是：" + e.getCause());
            return null;
        }
    }

    /**
     * 功能描述：把指定的java对象转为json数据
     */
    public static String toJson(Object clazz) {
        try {
            return new Gson().toJson(clazz);
        } catch (Exception e) {
            Log.e("ToJsonEntity", "fastjson转换错误：" + e.getMessage() + "\n原因是：" + e.getCause());
            return null;
        }
    }
}
