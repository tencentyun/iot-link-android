package com.yho.image.imp;

import android.app.Activity;

import com.yho.image.imageselectorbrowser.ImageSelectorBrowseActivity;

import java.util.ArrayList;

/**
 * ClassName: ImageBrowseUtils Description: 图片浏览器
 *
 * @author wuqionghai
 * @version 1.0 2015-10-28
 */
public class ImageBrowseUtils {
    private static final String TAG = ImageBrowseUtils.class.getName();

    /**
     * Methods: showImage
     * Description: 显示图片列表
     *
     * @param activity 上下文
     * @param path    图片
     * @param isLocal  是否本地 true本地 false 网络
     *                 void
     * @throws null
     */
    public static void showImage(Activity activity, String path, boolean isLocal) {
        if (null != activity && activity instanceof Activity && null != path) {
            ArrayList<String> lists = new ArrayList<String>();
            lists.add(path);
            showImageLists(activity, lists, isLocal);
        }
    }

    /**
     * Methods: showImageLists
     * Description: 显示图片列表
     *
     * @param activity 上下文
     * @param lists    图片列表
     * @param isLocal  是否本地 true本地 false 网络
     *                 void
     * @throws null
     */
    public static void showImageLists(Activity activity, ArrayList<String> lists, boolean isLocal) {
        showImageLists(activity,lists,0,isLocal);
    }

    public static void showImageLists(Activity activity, ArrayList<String> lists, int startId, boolean isLocal) {
        if (null != activity && activity instanceof Activity && null != lists) {
            ImageSelectorBrowseActivity.startActivity(activity, lists, startId, isLocal);
        }
    }

    public static void showImageLists(Activity activity, String []  arg, int startId, boolean isLocal) {
        if (null != activity && activity instanceof Activity && null != arg) {
            ArrayList<String> list = new ArrayList<>();
            for (String url:arg) {
                list.add(url);
            }
            ImageSelectorBrowseActivity.startActivity(activity, list, startId, isLocal);
        }
    }
}
