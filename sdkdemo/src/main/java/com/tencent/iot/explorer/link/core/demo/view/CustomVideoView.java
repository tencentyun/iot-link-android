package com.tencent.iot.explorer.link.core.demo.view;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.VideoView;

import com.tencent.iot.explorer.link.core.demo.util.SSlUtiles;

import javax.net.ssl.HttpsURLConnection;

public class CustomVideoView extends VideoView {
    public CustomVideoView(Context context) {
        super(context);
    }

    public CustomVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setVideoURI(Uri uri) {
        super.setVideoURI(uri);
        try {
            HttpsURLConnection.setDefaultSSLSocketFactory(SSlUtiles.createSSLSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(new SSlUtiles.TrustAllHostnameVerifier());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
