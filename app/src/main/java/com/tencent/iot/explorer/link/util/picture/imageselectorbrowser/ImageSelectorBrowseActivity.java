package com.tencent.iot.explorer.link.util.picture.imageselectorbrowser;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.github.chrisbanes.photoview.OnPhotoTapListener;
import com.github.chrisbanes.photoview.PhotoViewAttacher;
import com.squareup.picasso.Picasso;
import com.tencent.iot.explorer.link.R;
import com.tencent.iot.explorer.link.util.picture.utils.ImageShowUtils;
import com.tencent.iot.explorer.link.util.picture.utils.ImageUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 图片浏览类
 */
public class ImageSelectorBrowseActivity extends Activity implements OnClickListener {
    public static final String LOG_TAG = ImageSelectorBrowseActivity.class.getName();

    /**
     * mImagebrowseVg : 呈现多图浏览的ViewPage
     */
    private HackyViewPager mImagebrowseVg;

    /**
     * mImagebrowseCountTv : 图片个数
     */
    private TextView mImagebrowseCountTv;

    /**
     * mImagebrowseSaveTv : 图片保存
     */
    private TextView mImagebrowseSaveTv;

    /**
     * mImagebrowseExitTv : 退出
     */
    private TextView mImagebrowseExitTv;

    /**
     * mBrowsePageAdapter : 适配器
     */
    private BrowsePageAdapter mBrowsePageAdapter;

    /**
     * mPaths : 本地图片文件的路径
     */
    private List<String> mPaths;

    /**
     * mViews : 存储要显示的View
     */
    private List<ImageView> mViews = null;

    /**
     * 点击查看图片详情传进来的position，如果没有该参数的话，就把传进来的uris全部展示
     */
    private int mPosition;

    private boolean mIsNetImage = false;

    /**
     * EXTRA_NET_IMAGE_TYPE : 网络图片
     */
    private static final String EXTRA_NET_IMAGE_TYPE = "imageUris";

    /**
     * EXTRA_LOCAL_IMAGE_TYPE : 本地图片
     */
    private static final String EXTRA_LOCAL_IMAGE_TYPE = "imagePaths";

    public static final String EXTRA_POSITION_TYPE = "position";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.imageselector_browse_image_layout);
        initView();
        initData();
        setData();
        setListener();
    }

    @SuppressWarnings("unchecked")
    private void initData() {
        Intent intent = getIntent();
        if (intent.hasExtra(EXTRA_LOCAL_IMAGE_TYPE)) {// 加载本地图片
            mPaths = (List<String>) intent.getSerializableExtra(EXTRA_LOCAL_IMAGE_TYPE);
            mIsNetImage = false;
        } else {// 加载网络图片
            mPaths = (List<String>) intent.getSerializableExtra(EXTRA_NET_IMAGE_TYPE);
//			mPaths.add("http://imgsrc.baidu.com/forum/w%3D580/sign=cc4e2934edf81a4c2632ecc1e72b6029/563d720e0cf3d7cabbb6a436f41fbe096a63a9ca.jpg");
//			mPaths.add("http://imgsrc.baidu.com/forum/w%3D580/sign=836aa654ad18972ba33a00c2d6cc7b9d/b301d21b0ef41bd5fe1272b457da81cb38db3dc8.jpg");
//			mPaths.add("http://imgsrc.baidu.com/forum/w%3D580/sign=f73696e75fee3d6d22c687c373166d41/3ccc572c11dfa9ec2c18d35764d0f703918fc158.jpg");
//			mPaths.add("http://imgsrc.baidu.com/forum/w%3D580/sign=5e9288ac9a82d158bb8259b9b00b19d5/722e5c6034a85edf022af0614f540923dc5475e9.jpg");
//			mPaths.add("http://imgsrc.baidu.com/forum/w%3D580/sign=c55237873c87e9504217f3642039531b/ae46252dd42a283447c90e4f5db5c9ea15cebf22.jpg");
//			mPaths.add("http://imgsrc.baidu.com/forum/w%3D580/sign=9b1e5565bca1cd1105b672288913c8b0/eb0095cad1c8a786aad5fb5b6109c93d71cf5083.jpg");
//			mPaths.add("http://imgsrc.baidu.com/forum/w%3D580/sign=df634974748b4710ce2ffdc4f3cfc3b2/1fd7a786c9177f3e02cb4d5876cf3bc79e3d5683.jpg");
//			mPaths.add("http://imgsrc.baidu.com/forum/w%3D580/sign=2fd23d77cefcc3ceb4c0c93ba244d6b7/b1216709c93d70cf53051b58fedcd100bba12b83.jpg");
            mIsNetImage = true;
        }
        //test
//		if (intent.hasExtra(EXTRA_LOCAL_IMAGE_TYPE)) {// 加载本地图片
//			mPaths = (List<String>) intent.getSerializableExtra(EXTRA_LOCAL_IMAGE_TYPE);
//			mIsNetImage = false;
//		} else {// 加载网络图片
//			mPaths = new ArrayList<String>();
//			mPaths.add("http://imgsrc.baidu.com/forum/w%3D580/sign=cc4e2934edf81a4c2632ecc1e72b6029/563d720e0cf3d7cabbb6a436f41fbe096a63a9ca.jpg");
//			mPaths.add("http://imgsrc.baidu.com/forum/w%3D580/sign=836aa654ad18972ba33a00c2d6cc7b9d/b301d21b0ef41bd5fe1272b457da81cb38db3dc8.jpg");
//			mPaths.add("http://imgsrc.baidu.com/forum/w%3D580/sign=f73696e75fee3d6d22c687c373166d41/3ccc572c11dfa9ec2c18d35764d0f703918fc158.jpg");
//			mPaths.add("http://imgsrc.baidu.com/forum/w%3D580/sign=5e9288ac9a82d158bb8259b9b00b19d5/722e5c6034a85edf022af0614f540923dc5475e9.jpg");
//			mPaths.add("http://imgsrc.baidu.com/forum/w%3D580/sign=c55237873c87e9504217f3642039531b/ae46252dd42a283447c90e4f5db5c9ea15cebf22.jpg");
//			mPaths.add("http://imgsrc.baidu.com/forum/w%3D580/sign=9b1e5565bca1cd1105b672288913c8b0/eb0095cad1c8a786aad5fb5b6109c93d71cf5083.jpg");
//			mPaths.add("http://imgsrc.baidu.com/forum/w%3D580/sign=df634974748b4710ce2ffdc4f3cfc3b2/1fd7a786c9177f3e02cb4d5876cf3bc79e3d5683.jpg");
//			mPaths.add("http://imgsrc.baidu.com/forum/w%3D580/sign=2fd23d77cefcc3ceb4c0c93ba244d6b7/b1216709c93d70cf53051b58fedcd100bba12b83.jpg");
//			mIsNetImage = true;
//		}

        mViews = new ArrayList<ImageView>();
        mPosition = intent.getIntExtra(EXTRA_POSITION_TYPE, -1);
        if (mPosition == -1) {
            mPosition = 0;
        }
        for (String string : mPaths) {
            mViews.add(null);
        }
        // loadImage();
    }

    /**
     * Methods: startActivity Description: 启动图片浏览器
     *
     * @param activity 上下文
     * @param lists    图片地址列表
     * @param isLocal  图片是否为本地 void
     */

    public static void startActivity(Activity activity, ArrayList<String> lists, int startId, boolean isLocal) {
        if (null == lists || lists.size() == 0) {
            return;
        }
        Intent intent = new Intent(activity, ImageSelectorBrowseActivity.class);
        if (isLocal) {
            intent.putExtra(EXTRA_LOCAL_IMAGE_TYPE, lists);
        } else {
            intent.putExtra(EXTRA_NET_IMAGE_TYPE, lists);
        }
        intent.putExtra(EXTRA_POSITION_TYPE, startId);
        activity.startActivity(intent);
    }

    private void setData() {
        // 如果是本地图片的话，将保存按钮隐藏
        mImagebrowseSaveTv.setVisibility(mIsNetImage == true ? View.VISIBLE : View.GONE);

        // 单张图片不显示张数，反之显示
        mImagebrowseCountTv.setVisibility(mPaths.size() > 1 ? View.VISIBLE : View.GONE);

        mBrowsePageAdapter = new BrowsePageAdapter(ImageSelectorBrowseActivity.this, mPaths, mViews, mIsNetImage);
        mImagebrowseVg.setAdapter(mBrowsePageAdapter);

        final int count = mPaths.size();

        mImagebrowseVg.setCurrentItem(mPosition);
        mImagebrowseCountTv.setText((mPosition + 1 + "/" + mPaths.size()));

        mImagebrowseVg.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                mPosition = position;
                mImagebrowseCountTv.setText(position + 1 + "/" + count);
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
            }
        });
    }

    private void setListener() {
        mImagebrowseSaveTv.setOnClickListener(this);
        mImagebrowseExitTv.setOnClickListener(this);
    }

    private void initView() {
        mImagebrowseExitTv = (TextView) findViewById(R.id.tv_image_browse_exit);
        mImagebrowseCountTv = (TextView) findViewById(R.id.tv_image_count);
        mImagebrowseSaveTv = (TextView) findViewById(R.id.tv_image_browse_save);
        mImagebrowseVg = (HackyViewPager) findViewById(R.id.vp_image_browse);
    }

    @Override
    public void onClick(View v) {
        int viewId = v.getId();
        if (viewId == R.id.tv_image_browse_exit) {// 退出
            ImageSelectorBrowseActivity.this.finish();
            return;
        }

        if (viewId == R.id.tv_image_browse_save) {// 保存图片至本地
            try {
                File myCaptureFile = ImageUtils.saveFile(ImageSelectorBrowseActivity.this, mViews.get(mPosition).getDrawingCache(), mPaths.get(mPosition));
                // 发送系统广播，更新系统相册
                sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).setData(Uri.fromFile(myCaptureFile)));

            } catch (FileNotFoundException e) {
                Log.d(LOG_TAG, "FileNotFoundException" + e.toString());
            } catch (IOException e) {
                Log.d(LOG_TAG, "IOException" + e.toString());
            }
            return;
        }

    }

    class BrowsePageAdapter extends PagerAdapter {

        private List<String> lists;
        private List<ImageView> views;
        private Picasso mPicasso;
        private Activity mContext;
        private boolean isNetImage;

        public BrowsePageAdapter(Activity context, List<String> lists, List<ImageView> views, boolean isNetImage) {
            this.mContext = context;
            this.lists = lists;
            this.views = views;
            this.isNetImage = isNetImage;
            mPicasso = Picasso.get();
        }

        @Override
        public int getCount() {
            return lists.size();
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_UNCHANGED;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            if (views.size() <= position || null == views.get(position)) {
                ImageView imageView = new ImageView(container.getContext());
                if (isNetImage) {
//					mPicasso.load(lists.get(position)).into(imageView);
//                    ImageManager.setImagePath(container.getContext(), imageView, lists.get(position));
                    Picasso.get().load(lists.get(position)).resize(600, 800).centerInside().into(imageView);
                } else {
//					mPicasso.load(new File(lists.get(position))).into(imageView);
//                    ImageManager.setImagePath(container.getContext(), imageView, new File(lists.get(position)));
                    Bitmap bitmap = ImageShowUtils.getSmallBitmap(lists.get(position));
                    imageView.setImageBitmap(bitmap);
                }

                PhotoViewAttacher photoViewAttacher = new PhotoViewAttacher(imageView);
                photoViewAttacher.setMinimumScale(1f);
                photoViewAttacher.update();

                photoViewAttacher.setOnPhotoTapListener(new OnPhotoTapListener() {
                    @Override
                    public void onPhotoTap(ImageView view, float x, float y) {
                        mContext.finish();
                    }
                });
                views.remove(position);
                views.add(position, imageView);
            }
            ((ViewPager) container).addView(views.get(position), LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            return views.get(position);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }
}
