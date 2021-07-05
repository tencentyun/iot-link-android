package com.tencent.iot.explorer.link.demo.common.util;

import android.content.Context;
import android.util.Log;

import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Interceptor;

public class PicassoTrustAll {
    private static String TAG = PicassoTrustAll.class.getSimpleName();
    private static Picasso mInstance = null;

    private PicassoTrustAll(Context context) {
        List<Interceptor> interceptors = new ArrayList<>();

        mInstance = new Picasso.Builder(context)
                .downloader(new OkHttp3Downloader(SelfSignCertHttpClient.getSelfSignOkHttpClient(interceptors)))
                .listener((picasso, uri, exception) -> Log.e(TAG, exception.getMessage())).build();

    }

    public static Picasso getInstance(Context context) {
        if (mInstance == null) {
            new PicassoTrustAll(context);
        }
        return mInstance;
    }
}