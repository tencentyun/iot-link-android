package com.kitlink.util;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebView;

import com.kitlink.activity.HelpFeedbackActivity;

import org.jetbrains.annotations.NotNull;

public class JsBridge implements OnCameraEventListener{

    public static final String BRIDGE = JsBridge.class.getSimpleName();

    private static final String TAG = JsBridge.class.getSimpleName();

    // android 调用 js 方法时，js 的函数名
    private final String CAPTURE_SUCCESS_FUNCTION = "captureSuccess()";
    // android 调用 js 方法时的前缀
    private final String JS_PRE_FIX = "javascript:";

    private Activity context;

    public JsBridge(Activity context){
        this.context = context;
    }

    // 清除 JsBridge 引有窗口对象
    public void close() {
        context = null;
    }

    // @JavascriptInterface android 4.2 以上避免 js 入侵的注释
    // 与 js 交互时用到的方法，js 调用该方法打开相机
    @JavascriptInterface
    public boolean openCamera(){
        if (context instanceof HelpFeedbackActivity) {
            return ((HelpFeedbackActivity) context).asynOpenCamera();
        }
        return false;
    }

    // 根据实际情况清除临时图片文件，避免临时文件过多占用存储空间
    @JavascriptInterface
    public void deleteTempImage(String filePath){
        if (context instanceof HelpFeedbackActivity) {
            ((HelpFeedbackActivity) context).deleteTempImageFile(filePath);
        }
    }

    @Override
    public void onCameraOpened() {

    }

    @Override
    public void onCaptureSuccess(View view, @NotNull String path) {
        if (view instanceof WebView) {
            ((WebView) view).evaluateJavascript (JS_PRE_FIX + CAPTURE_SUCCESS_FUNCTION,
                    new ValueCallback<String>(){
                @Override
                public void onReceiveValue(String value){

                }
            });
        }
    }

    @Override
    public void onCameraClosed() {

    }
}
