package com.tencent.iot.explorer.link.kitlink.util.picture.imageselectorbrowser;

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
import com.tencent.iot.explorer.link.kitlink.util.picture.utils.ImageShowUtils;
import com.tencent.iot.explorer.link.kitlink.util.picture.utils.ImageUtils;

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
            mIsNetImage = true;
        }

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
