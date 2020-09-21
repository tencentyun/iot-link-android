package com.tencent.iot.explorer.link.util.picture.clipimage;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.Toast;

import com.tencent.iot.explorer.link.util.picture.clipimage.ClipView.OnDrawListenerComplete;
import com.tencent.iot.explorer.link.util.picture.utils.BitmapUtils;
import com.tencent.iot.explorer.link.util.picture.utils.ImageUtils;
import com.tencent.iot.explorer.link.util.picture.utils.WindowManage;
import com.tencent.iot.explorer.link.R;;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * 剪裁图片
 */
public class ClipPictureActivity extends Activity implements OnTouchListener {
    private ImageView mSrcPic;
    private ClipView clipview;

    private Matrix matrix = new Matrix();
    private Matrix savedMatrix = new Matrix();

    /**
     * 动作标志：无
     */
    private static final int NONE = 0;
    /**
     * 动作标志：拖动
     */
    private static final int DRAG = 1;
    /**
     * 动作标志：缩放
     */
    private static final int ZOOM = 2;
    /**
     * 初始化动作标志
     */
    private int mode = NONE;

    /**
     * 记录起始坐标
     */
    private PointF start = new PointF();
    /**
     * 记录缩放时两指中间点坐标
     */
    private PointF mid = new PointF();
    private float oldDist = 1f;

    private Bitmap bitmap;
    private String mImagePath;
    private int mWidth;
    private int mHeight;
    private byte[] mBitmapByte;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_clip_main);
        initView();
        initData();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
            Intent intent = new Intent();
            Bundle bundle = new Bundle();
            bundle.putByteArray("bitmapbyte", mBitmapByte);
            intent.putExtras(bundle);
            setResult(Activity.RESULT_OK, intent);
            finish();
        }
    }

    private void initView() {
        mSrcPic = (ImageView) this.findViewById(R.id.image_clip_main_pic_iv);
        mSrcPic.setOnTouchListener(this);
    }

    private void initData() {
        mImagePath = "";
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            mImagePath = bundle.getString("clipptah");
            mWidth = bundle.getInt("width");
            mHeight = bundle.getInt("height");
        }

        ViewTreeObserver observer = mSrcPic.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {

            @SuppressWarnings("deprecation")
            public void onGlobalLayout() {
                mSrcPic.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                initClipView(mSrcPic.getTop(), mImagePath, mWidth, mHeight);
            }
        });
        findViewById(R.id.image_clip_main_exit_iv).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        findViewById(R.id.image_clip_main_submit_iv).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //压缩bitmap
                Bitmap clipBitmap = BitmapUtils.compress(getBitmap());
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                clipBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);

                mBitmapByte = baos.toByteArray();
                Log.e("--------", mBitmapByte.length / 1024 + " kb");
                Intent intent = new Intent(ClipPictureActivity.this, ImageSimpleBrowseActivity.class);

//                intent.putExtras(ImageSimpleBrowseActivity.getBundle(mBitmapByte));
                ImageSimpleBrowseActivity.bis = mBitmapByte;
                startActivityForResult(intent, 100);
            }
        });
    }

    /**
     * 初始化截图区域，并将源图按裁剪框比例缩放
     *
     * @param top
     */
    private void initClipView(int top, final String path, int width, int height) {
        try {
            bitmap = ImageUtils.getThumbnail(path, 500);//BitmapFactory.decodeFile(path);
        } catch (IOException e) {
//            e.printStackTrace();
        }
        clipview = new ClipView(ClipPictureActivity.this);
        clipview.setCustomTopBarHeight(top);
        clipview.setClipHeight(height);
        clipview.setClipWidth(width);
//		clipview.setClipHeight(300);
//		clipview.setClipWidth(300);
        if (bitmap == null) {
            Toast.makeText(this, this.getString(R.string.load_image_failed_please_reload), Toast.LENGTH_SHORT).show();//"图片加载错误,请重新选择图片"
            finish();
            return;
        }
        clipview.addOnDrawCompleteListener(new OnDrawListenerComplete() {

            public void onDrawCompelete() {
                clipview.removeOnDrawCompleteListener();
//				int clipHeight = clipview.getClipHeight();
//				int clipWidth = clipview.getClipWidth();
                //设置图片初始值的高宽为手机屏幕的高宽
                int clipHeight = WindowManage.getDispaly(ClipPictureActivity.this).height;
                int clipWidth = WindowManage.getDispaly(ClipPictureActivity.this).width;

                int midX = clipview.getClipLeftMargin() + (clipWidth / 2);
                int midY = clipview.getClipTopMargin() + (clipHeight / 2);

                int imageWidth = bitmap.getWidth();
                int imageHeight = bitmap.getHeight();
                // 按裁剪框求缩放比例
                float scale = (clipWidth * 1.0f) / imageWidth;
                if (imageWidth > imageHeight) {
                    scale = (clipHeight * 1.0f) / imageHeight;
                }

                // 起始中心点
                float imageMidX = imageWidth * scale / 2;
                float imageMidY = clipview.getCustomTopBarHeight()
                        + imageHeight * scale / 2;
                mSrcPic.setScaleType(ScaleType.MATRIX);

                // 缩放
                matrix.postScale(scale, scale);
                // 平移
                matrix.postTranslate(midX - imageMidX, midY - imageMidY);

                mSrcPic.setImageMatrix(matrix);
                mSrcPic.setImageBitmap(bitmap);
            }
        });
        this.addContentView(clipview, new LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
    }

    public boolean onTouch(View v, MotionEvent event) {
        ImageView view = (ImageView) v;
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                savedMatrix.set(matrix);
                // 设置开始点位置
                start.set(event.getX(), event.getY());
                mode = DRAG;
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                oldDist = spacing(event);
                if (oldDist > 10f) {
                    savedMatrix.set(matrix);
                    midPoint(mid, event);
                    mode = ZOOM;
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                mode = NONE;
                break;
            case MotionEvent.ACTION_MOVE:
                if (mode == DRAG) {
                    matrix.set(savedMatrix);
                    matrix.postTranslate(event.getX() - start.x, event.getY()
                            - start.y);
                } else if (mode == ZOOM) {
                    float newDist = spacing(event);
                    if (newDist > 10f) {
                        matrix.set(savedMatrix);
                        float scale = newDist / oldDist;
                        matrix.postScale(scale, scale, mid.x, mid.y);
                    }
                }
                break;
        }
        view.setImageMatrix(matrix);
        return true;
    }

    /**
     * 多点触控时，计算最先放下的两指距离
     *
     * @param event
     * @return
     */
    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    /**
     * 多点触控时，计算最先放下的两指中心坐标
     *
     * @param point
     * @param event
     */
    private void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }

    /**
     * 获取裁剪框内截图
     *
     * @return
     */
    private Bitmap getBitmap() {
        // 获取截屏
        View view = this.getWindow().getDecorView();
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();

        // 获取状态栏高度
        Rect frame = new Rect();
        this.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
        int statusBarHeight = frame.top;

        clipview.setIsClip(true);
        int x = clipview.getClipLeftMargin();
        int y = clipview.getClipTopMargin() + statusBarHeight;

        // 边界判断
        x = (x >= 0 ? x : 0);
        y = (y >= 0 ? y : 0);

        Bitmap finalBitmap = Bitmap.createBitmap(view.getDrawingCache(), x,
                y, clipview.getClipWidth(),
                clipview.getClipHeight());

        // 释放资源
        view.destroyDrawingCache();
        return finalBitmap;
    }


    public static Bundle getBundle(String path, int width, int height) {
        Bundle bundle = new Bundle();
        bundle.putString("clipptah", path);
        bundle.putInt("width", width);
        bundle.putInt("height", height);
        return bundle;
    }

    public static byte[] getBitmapByte(Intent intent) {
        return intent.getExtras().getByteArray("bitmapbyte");
    }

}