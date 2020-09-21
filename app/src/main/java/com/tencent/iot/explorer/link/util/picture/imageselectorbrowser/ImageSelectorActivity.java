package com.tencent.iot.explorer.link.util.picture.imageselectorbrowser;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.tencent.iot.explorer.link.R;
import com.tencent.iot.explorer.link.util.picture.clipimage.ClipPictureActivity;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;


/**
 * 多图选择的Activity
 */
public class ImageSelectorActivity extends FragmentActivity implements ImageSelectorFragment.Callback, OnClickListener {

    /**
     * 默认选择集
     */
    public static final String EXTRA_DEFAULT_SELECTED_LIST = "default_list";

    private ImageView mTitleBackIv;

    private TextView mSubmitButton;

    private ArrayList<String> resultList = new ArrayList<String>();

    private int mDefaultCount;
    private int mMode;

    private int mClipWidth;
    private int mClipHeight;
    private static final int RESULT_CAMERA_CROP_PATH_RESULT = 301;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.imageselector_base_layout);

        Intent intent = getIntent();
        Bundle data = intent.getExtras();
        mDefaultCount = data.getInt(ImageSelectorConstant.EXTRA_SELECT_COUNT, 9);
        mMode = data.getInt(ImageSelectorConstant.EXTRA_SELECT_MODE, Mode.MODE_MULTI);
        boolean isShow = data.getBoolean(ImageSelectorConstant.EXTRA_SHOW_CAMERA, true);
        if (mMode == Mode.MODE_MULTI && intent.hasExtra(ImageSelectorConstant.EXTRA_DEFAULT_SELECTED_LIST)) {
            resultList = intent.getStringArrayListExtra(ImageSelectorConstant.EXTRA_DEFAULT_SELECTED_LIST);
        }

        mClipWidth = intent.getIntExtra(ImageSelectorConstant.EXTRA_CLIP_WIDTH, 100);
        mClipHeight = intent.getIntExtra(ImageSelectorConstant.EXTRA_CLIP_HEIGHT, 100);

        Bundle bundle = new Bundle();
        bundle.putInt(ImageSelectorConstant.EXTRA_SELECT_COUNT, mDefaultCount);
        bundle.putInt(ImageSelectorConstant.EXTRA_SELECT_MODE, mMode);
        bundle.putBoolean(ImageSelectorConstant.EXTRA_SHOW_CAMERA, isShow);
        bundle.putStringArrayList(ImageSelectorConstant.EXTRA_DEFAULT_SELECTED_LIST, resultList);

        getSupportFragmentManager().beginTransaction()
                .add(R.id.fl_image_grid_container, Fragment.instantiate(this, ImageSelectorFragment.class.getName(), bundle))
                .commitAllowingStateLoss();

        mTitleBackIv = (ImageView) findViewById(R.id.iv_title_back);
        mSubmitButton = (TextView) findViewById(R.id.tv_title_commit);
        if (resultList == null || resultList.size() <= 0) {
            mSubmitButton.setText(R.string.imageselector_complete);
            mSubmitButton.setEnabled(false);
        } else {
            mSubmitButton.setText(getResources().getString(R.string.imageselector_complete) + "(" + resultList.size() + "/" + mDefaultCount + ")");
            mSubmitButton.setEnabled(true);
        }

        mSubmitButton.setOnClickListener(this);
        mTitleBackIv.setOnClickListener(this);
    }

//    @Override
//    public void onConfigurationChanged(Configuration newConfig) {
//        super(newConfig);
//    }

    @Override
    public void onSingleImageSelected(String path) {
        Intent data = new Intent();
        resultList.add(path);
        selectFinish(resultList);
    }

    public static ArrayList<String> getImageList(Intent data) {
        if (null == data) {
            new ArrayList<String>();
        }
        ArrayList<String> list = data.getStringArrayListExtra(ImageSelectorConstant.EXTRA_RESULT);
        if (null == list) {
            new ArrayList<String>();
        }
        return list;
    }

    @Override
    public void onImageSelected(String path) {
        if (!resultList.contains(path)) {
            resultList.add(path);
        }
        // 有图片之后，改变按钮状态
        if (resultList.size() > 0) {
            mSubmitButton.setText(getResources().getString(R.string.imageselector_complete) + "(" + resultList.size() + "/" + mDefaultCount + ")");
            if (!mSubmitButton.isEnabled()) {
                mSubmitButton.setEnabled(true);
            }
        }
    }

    @Override
    public void onImageUnselected(String path) {
        if (resultList.contains(path)) {
            resultList.remove(path);
            mSubmitButton.setText(getResources().getString(R.string.imageselector_complete) + "(" + resultList.size() + "/" + mDefaultCount + ")");
        } else {
            mSubmitButton.setText(getResources().getString(R.string.imageselector_complete) + "(" + resultList.size() + "/" + mDefaultCount + ")");
        }
        // 当为选择图片时候的状态
        if (resultList.size() == 0) {
            mSubmitButton.setText(R.string.finish);
            mSubmitButton.setEnabled(false);
        }
    }

    @Override
    public void onCameraShot(File imageFile) {
        if (imageFile != null) {
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(imageFile)));// 刷新系统相册
            Intent data = new Intent();
            resultList.add(imageFile.getAbsolutePath());
            selectFinish(resultList);
        }
    }

    /**
     * 调用图片浏览器
     *
     * @param context      上下文对象
     * @param isShowCamera 是否显示照相机
     * @param count        选择照片最大的个数
     * @param mode         模式
     */
    public static void showImageSelector(Context context, int mode, boolean isShowCamera, int count) {

        Bundle bundle = new Bundle();
        bundle.putInt(ImageSelectorConstant.EXTRA_SELECT_COUNT, count);
        bundle.putBoolean(ImageSelectorConstant.EXTRA_SHOW_CAMERA, isShowCamera);
        bundle.putInt(ImageSelectorConstant.EXTRA_SELECT_MODE, mode);

        startActivityForResult((Activity) context, ImageSelectorActivity.class, bundle, ImageSelectorConstant.REQUEST_IMAGE);
    }

    public static void showImageSelector(Fragment context, int mode, boolean isShowCamera, int count) {

        Bundle bundle = new Bundle();
        bundle.putInt(ImageSelectorConstant.EXTRA_SELECT_COUNT, count);
        bundle.putBoolean(ImageSelectorConstant.EXTRA_SHOW_CAMERA, isShowCamera);
        bundle.putInt(ImageSelectorConstant.EXTRA_SELECT_MODE, mode);

        startActivityForResult(context, ImageSelectorActivity.class, bundle, ImageSelectorConstant.REQUEST_IMAGE);
    }

    /**
     * 添加的
     *
     * @param context
     * @param mode
     * @param isShowCamera
     * @param count
     * @param requestCode
     */
    public static void showImageSelector(Context context, int mode, boolean isShowCamera, int count, int requestCode) {

        Bundle bundle = new Bundle();
        bundle.putInt(ImageSelectorConstant.EXTRA_SELECT_COUNT, count);
        bundle.putBoolean(ImageSelectorConstant.EXTRA_SHOW_CAMERA, isShowCamera);
        bundle.putInt(ImageSelectorConstant.EXTRA_SELECT_MODE, mode);

        startActivityForResult((Activity) context, ImageSelectorActivity.class, bundle, requestCode);
    }

    public static void showImageSelector(Context context, int mode, boolean isShowCamera, int count, int clipwidth, int clipHeight) {
        try {
            Bundle bundle = new Bundle();
            bundle.putInt(ImageSelectorConstant.EXTRA_SELECT_COUNT, count);
            bundle.putBoolean(ImageSelectorConstant.EXTRA_SHOW_CAMERA, isShowCamera);
            bundle.putInt(ImageSelectorConstant.EXTRA_SELECT_MODE, mode);
            bundle.putInt(ImageSelectorConstant.EXTRA_CLIP_WIDTH, clipwidth);
            bundle.putInt(ImageSelectorConstant.EXTRA_CLIP_HEIGHT, clipHeight);
            startActivityForResult((Activity) context, ImageSelectorActivity.class, bundle, ImageSelectorConstant.REQUEST_IMAGE);
        } catch (Exception e) {

        }
    }

    public static void startActivityForResult(Activity context, Class<?> cls, Bundle bundle, int requestCode) {
        Intent intent = new Intent(context, cls);
        if (null != bundle) {
            intent.putExtras(bundle);
        }
        context.startActivityForResult(intent, requestCode);
    }

    public static void startActivityForResult(Fragment context, Class<?> cls, Bundle bundle, int requestCode) {
        Intent intent = new Intent(context.getActivity(), cls);
        if (null != bundle) {
            intent.putExtras(bundle);
        }
        context.startActivityForResult(intent, requestCode);
    }

    @Override
    public void onClick(View v) {
        int viewId = v.getId();
        if (viewId == R.id.tv_title_commit) {

            if (resultList != null && resultList.size() > 0) {
                selectFinish(resultList);
            }
            return;
        }

        if (viewId == R.id.iv_title_back) {
            setResult(RESULT_CANCELED);
            finish();
            return;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        resultList.clear();
        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
            //clip
            Intent intent = new Intent();
            intent.putExtra(ImageSelectorConstant.EXTRA_RESULT_BITMAP, ClipPictureActivity.getBitmapByte(data));
            setResult(RESULT_OK, intent);
            finish();
        } else if (requestCode == RESULT_CAMERA_CROP_PATH_RESULT && resultCode == Activity.RESULT_OK) {
            Bundle extras = data.getExtras();
            if (extras != null) {
                try {
                    Intent intent = new Intent();
                    intent.putExtra(ImageSelectorConstant.EXTRA_RESULT_BITMAP, imageCropUri.getPath());
                    setResult(RESULT_OK, intent);
                    finish();
                } catch (Exception e) {
                    Toast.makeText(ImageSelectorActivity.this, this.getString(R.string.clip_image_failed_please_reclip), Toast.LENGTH_SHORT).show();//"图片裁剪异常，请重新选择图片"
                }
            }
        }
    }

    Uri imageCropUri;

    private void selectFinish(ArrayList<String> lsit) {
        // 返回已选择的图片数据
        if (mMode == Mode.MODE_CLIP) {//启动裁剪图片
//            Intent intent = new Intent(this,ClipPictureActivity.class);
//            intent.putExtras(ClipPictureActivity.getBundle(lsit.get(0),mClipWidth,mClipHeight));
//            startActivityForResult(intent, 100);

            //启动新的裁剪程序
            Intent intent = new Intent("com.android.camera.action.CROP");
            File iFile = new File(lsit.get(0));
            Uri uri = Uri.fromFile(iFile);

            File file = new File(getSDCardPath() + "/temp_crop.jpg");
            imageCropUri = Uri.fromFile(file);
            intent.setDataAndType(uri, "image/*");
            intent.putExtra("crop", "true");
            intent.putExtra("aspectX", mClipWidth);
            intent.putExtra("aspectY", mClipHeight);
            intent.putExtra("outputX", mClipWidth);
            intent.putExtra("outputY", mClipHeight);
            intent.putExtra("return-data", false);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageCropUri);
            intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
            intent.putExtra("noFaceDetection", true);
            startActivityForResult(intent, RESULT_CAMERA_CROP_PATH_RESULT);
        } else {
            Intent data = new Intent();
            data.putStringArrayListExtra(ImageSelectorConstant.EXTRA_RESULT, lsit);
            setResult(RESULT_OK, data);
            finish();
        }
    }

    public static String getSDCardPath() {
        String cmd = "cat /proc/mounts";
        Runtime run = Runtime.getRuntime();// 返回与当前 Java 应用程序相关的运行时对象
        try {
            Process p = run.exec(cmd);// 启动另一个进程来执行命令
            BufferedInputStream in = new BufferedInputStream(p.getInputStream());
            BufferedReader inBr = new BufferedReader(new InputStreamReader(in));

            String lineStr;
            while ((lineStr = inBr.readLine()) != null) {
                // 获得命令执行后在控制台的输出信息
                if (lineStr.contains("sdcard")
                        && lineStr.contains(".android_secure")) {
                    String[] strArray = lineStr.split(" ");
                    if (strArray != null && strArray.length >= 5) {
                        String result = strArray[1].replace("/.android_secure",
                                "");
                        return result;
                    }
                }
                // 检查命令是否执行失败。
                if (p.waitFor() != 0 && p.exitValue() == 1) {
                    // p.exitValue()==0表示正常结束，1：非正常结束
                }
            }
            inBr.close();
            in.close();
        } catch (Exception e) {

            return Environment.getExternalStorageDirectory().getPath();
        }

        return Environment.getExternalStorageDirectory().getPath();
    }

    /**
     * ClassName: Mode
     * Description: 单选MODE_SINGLE 多选MODE_MULTI
     *
     * @version 1.0  2015-9-18
     */
    public interface Mode {
        /**
         * MODE_SINGLE : 单选模式
         */
        int MODE_SINGLE = 1;
        /**
         * MODE_MULTI : 多选模式
         */
        int MODE_MULTI = 2;
        /**
         * MODE_CLIP : 单选截图模式
         */
        int MODE_CLIP = 3;
    }
}
