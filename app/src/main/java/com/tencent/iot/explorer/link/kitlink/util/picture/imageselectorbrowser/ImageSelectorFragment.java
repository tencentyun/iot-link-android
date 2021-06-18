package com.tencent.iot.explorer.link.kitlink.util.picture.imageselectorbrowser;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ListPopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import com.tencent.iot.explorer.link.R;
import com.tencent.iot.explorer.link.core.log.L;
import com.tencent.iot.explorer.link.kitlink.util.picture.imageselectorbrowser.ImageSelectorActivity.Mode;
import com.tencent.iot.explorer.link.kitlink.util.picture.imp.ImageManager;
import com.tencent.iot.explorer.link.kitlink.util.picture.utils.FileUtils;
import com.tencent.iot.explorer.link.kitlink.util.picture.utils.TimeUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**  
 * 图片选择Fragment
 */
public class ImageSelectorFragment extends Fragment implements OnClickListener{

    private static final String TAG = "MultiImageSelector";

    /**LOADER_ALL : 不同loader定义 */ 
    private static final int LOADER_ALL = 0;
    private static final int LOADER_CATEGORY = 1;
    
    /**REQUEST_CAMERA : 请求加载系统照相机 */ 
    private static final int REQUEST_CAMERA = 100;


    /**resultList : 结果数据 */ 
    private ArrayList<String> resultList = new ArrayList<String>();
    
    /**mResultFolder : 文件夹数据 */ 
    private ArrayList<ImageSelectorFolderBean> mResultFolder = new ArrayList<ImageSelectorFolderBean>();

    /**mGridView : 图片Grid */ 
    private GridView mGridView;
    
    /**mCallback : 接口 */ 
    private Callback mCallback;

    /**mImageAdapter : 适配器 */ 
    private ImageSelectorGridAdapter mImageAdapter;
    private ImageSelectorPopupFolderAdapter mFolderAdapter;

    /**mFolderPopupWindow : 文件夹选项 */ 
    private ListPopupWindow mFolderPopupWindow;

    /**mTimeLineText : 时间线 */ 
    private TextView mTimeLineText;
    
    /**mCategoryText : 类别 */ 
    private TextView mCategoryText;
    
    /**mPreviewBtn : 预览按钮 */ 
    private Button mPreviewBtn;
    
    /**mPopupAnchorView : 底部View */ 
    private View mPopupAnchorView;

    private int mDesireImageCount;

    private boolean hasFolderGened = false;
    
    /**mIsShowCamera : 是否显示照相机 */ 
    private boolean mIsShowCamera = false;

    private int mGridWidth, mGridHeight;

    private File mTmpFile;
    private String mTmpPath;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        //将回调对象跟activity绑定在一起
        try {
            mCallback = (Callback) activity;
        }catch (ClassCastException e){
//            throw new ClassCastException("The Activity must implement MultiImageSelectorFragment.Callback interface...");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater,  ViewGroup container,  Bundle savedInstanceState) {
//        Toast.makeText(getActivity(),"onCreateView",Toast.LENGTH_SHORT).show();
        return inflater.inflate(R.layout.imageselector_multi_image_layout, container, false);
    }

    @Override
    public void onViewCreated(View view,  Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 选择图片数量
        mDesireImageCount = getArguments().getInt(ImageSelectorConstant.EXTRA_SELECT_COUNT);

        // 图片选择模式
        final int mode = getArguments().getInt(ImageSelectorConstant.EXTRA_SELECT_MODE);

        // 默认选择
        if(mode == Mode.MODE_MULTI) {
            ArrayList<String> tmp = getArguments().getStringArrayList(ImageSelectorConstant.EXTRA_DEFAULT_SELECTED_LIST);
            if(tmp != null && tmp.size()>0) {
                resultList = tmp;
            }
        }

        // 是否显示照相机
        mIsShowCamera = getArguments().getBoolean(ImageSelectorConstant.EXTRA_SHOW_CAMERA, true);
        mImageAdapter = new ImageSelectorGridAdapter(getActivity(), mIsShowCamera);
        // 是否显示选择指示器
        mImageAdapter.showSelectIndicator(mode == Mode.MODE_MULTI);

        mPopupAnchorView = view.findViewById(R.id.rl_footer_navigation);

        mTimeLineText = (TextView) view.findViewById(R.id.tv_found_time);
        // 初始化，先隐藏当前timeline
        mTimeLineText.setVisibility(View.GONE);

        mCategoryText = (TextView) view.findViewById(R.id.btn_category);
        // 初始化，加载所有图片
        mCategoryText.setText(R.string.imageselector_all_folder);

        mPreviewBtn = (Button) view.findViewById(R.id.btn_preview);
        // 初始化，按钮状态初始化
        if(resultList == null || resultList.size()<=0){
            mPreviewBtn.setText(R.string.imageselector_preview);
            mPreviewBtn.setEnabled(false);
        }
        
        mGridView = (GridView) view.findViewById(R.id.gv_imageselector_show_image);
        mGridView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int state) {
            	
//                final Picasso picasso = Picasso.with(getActivity());
//                if(state == SCROLL_STATE_IDLE || state == SCROLL_STATE_TOUCH_SCROLL){
//                    picasso.resumeTag(getActivity());
//                }else{
//                    picasso.pauseTag(getActivity());
//                }
                if(state == SCROLL_STATE_IDLE || state == SCROLL_STATE_TOUCH_SCROLL){
                	ImageManager.resumeTag(getActivity());
                }else{
                	ImageManager.pauseTag(getActivity());
                }

                if(state == SCROLL_STATE_IDLE){
                    // 停止滑动，日期指示器消失
                    mTimeLineText.setVisibility(View.GONE);
                }else if(state == SCROLL_STATE_FLING){
                    mTimeLineText.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if(mTimeLineText.getVisibility() == View.VISIBLE) {
                    int index = firstVisibleItem + 1 == view.getAdapter().getCount() ? view.getAdapter().getCount() - 1 : firstVisibleItem + 1;
                    ImageSelectorImageBean image = (ImageSelectorImageBean) view.getAdapter().getItem(index);
                    if (image != null) {
                        mTimeLineText.setText(TimeUtils.formatPhotoDate(image.getImagePath()));
                    }
                }
            }
        });
        mGridView.setAdapter(mImageAdapter);
        mGridView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            public void onGlobalLayout() {
                setGridViewAutoWidthAndHeight(mGridView);
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN){
                    mGridView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }else{
                    mGridView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
//                final int width = mGridView.getWidth();
//                final int height = mGridView.getHeight();
//
//                mGridWidth = width;
//                mGridHeight = height;
//
//                final int desireSize = getResources().getDimensionPixelOffset(R.dimen.imageselector_default_image_size);
//                final int numCount = width / desireSize;
//                int columnSpace = getResources().getDimensionPixelOffset(R.dimen.space_size1);
//                double temWidth = (width - columnSpace*(numCount-1))*1.0 / numCount;
//                int columnWidth = (int)(temWidth*desireSize);
//
//                columnSpace = (width - columnWidth*numCount)/(numCount-1);
//                mGridView.setHorizontalSpacing(columnSpace);
//                mGridView.setVerticalSpacing(columnSpace);
//
////                int columnWidth = (width - columnSpace*(numCount-1)) / numCount;
//                mImageAdapter.setItemSize(columnWidth);
//
//                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN){
//                    mGridView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
//                }else{
//                    mGridView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
//                }
            }
        });
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(mImageAdapter.isShowCamera()){
                    // 如果显示照相机，则第一个Grid显示为照相机，处理特殊逻辑
                    if(i == 0){
                        // 判断选择数量问题
                        if(mDesireImageCount == resultList.size()){
                            Toast.makeText(getActivity(), R.string.imageselector_msg_amount_limit, Toast.LENGTH_SHORT).show();
                            return;
                        }
                        showCameraAction();
                    }else{
                        // 正常操作
                    	ImageSelectorImageBean image = (ImageSelectorImageBean) adapterView.getAdapter().getItem(i);
                        selectImageFromGrid(image, mode);
                    }
                }else{
                    // 正常操作
                	ImageSelectorImageBean image = (ImageSelectorImageBean) adapterView.getAdapter().getItem(i);
                    selectImageFromGrid(image, mode);
                }
            }
        });

        mFolderAdapter = new ImageSelectorPopupFolderAdapter(getActivity());
        
        setListener();

        if (savedInstanceState != null){
            mTmpPath = savedInstanceState.getString("file");
        }
    }

    private void setGridViewAutoWidthAndHeight(GridView gridView){
        final int width = gridView.getWidth();
        final int height = gridView.getHeight();

        mGridWidth = width;
        mGridHeight = height;

        final int desireSize = getResources().getDimensionPixelOffset(R.dimen.imageselector_default_image_size);
        final int numCount = width / desireSize;
        int columnSpace = getResources().getDimensionPixelOffset(R.dimen.space_size);
        double temWidth = (width - columnSpace*(numCount-1))*1.0 / numCount;
        int columnWidth = (int)(temWidth);

        columnSpace = (width - columnWidth*numCount)/(numCount-1);
        gridView.setHorizontalSpacing(columnSpace);
        gridView.setVerticalSpacing(columnSpace);
        gridView.setNumColumns(numCount);

//                int columnWidth = (width - columnSpace*(numCount-1)) / numCount;
        ((ImageSelectorGridAdapter)gridView.getAdapter()).setItemSize(columnWidth);
    }

	private void setListener() {
		mCategoryText.setOnClickListener(this);
        mPreviewBtn.setOnClickListener(this);
	}

    /**
     * 创建弹出的ListView
     */
    @SuppressLint("NewApi") private void createPopupFolderList(int width, int height) {
        mFolderPopupWindow = new ListPopupWindow(getActivity());
        mFolderPopupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mFolderPopupWindow.setAdapter(mFolderAdapter);
        mFolderPopupWindow.setContentWidth(width);
        mFolderPopupWindow.setWidth(width);
        mFolderPopupWindow.setHeight(height * 5 / 8);
//        mFolderPopupWindow.setAnimationStyle(R.style.imageselector_popup_animation_style);
        mFolderPopupWindow.setAnchorView(mPopupAnchorView);
        mFolderPopupWindow.setModal(true);
        mFolderPopupWindow.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> adapterView, View view, int i, long l) {

                mFolderAdapter.setSelectIndex(i);

                final int index = i;

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mFolderPopupWindow.dismiss();

                        if (index == 0) {
                            getActivity().getSupportLoaderManager().restartLoader(LOADER_ALL, null, mLoaderCallback);
                            mCategoryText.setText(R.string.imageselector_all_folder);
                            if (mIsShowCamera) {
                                mImageAdapter.setShowCamera(true);
                            } else {
                                mImageAdapter.setShowCamera(false);
                            }
                        } else {
                            ImageSelectorFolderBean folder = (ImageSelectorFolderBean) adapterView.getAdapter().getItem(index);
                            if (null != folder) {
                                mImageAdapter.setData(folder.getImages());
                                mCategoryText.setText(folder.getFileDirectoryName());
                                // 设定默认选择
                                if (resultList != null && resultList.size() > 0) {
                                    mImageAdapter.setDefaultSelected(resultList);
                                }
                            }
                            mImageAdapter.setShowCamera(false);
                        }

                        // 滑动到最初始位置
                        mGridView.smoothScrollToPosition(0);
                    }
                }, 100);

            }
        });
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // 首次加载所有图片
        getActivity().getSupportLoaderManager().initLoader(LOADER_ALL, null, mLoaderCallback);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putSerializable("file", mTmpPath);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 相机拍照完成后，返回图片路径
        L.INSTANCE.d(mTmpPath);
        if(requestCode == REQUEST_CAMERA){
            if (mTmpFile == null){
                mTmpFile = new File(mTmpPath);
            }
            if(resultCode == Activity.RESULT_OK) {
                if (mTmpFile != null) {
                    if (mCallback != null) {
                        mCallback.onCameraShot(mTmpFile);
                    }
                }
            }else{
                if(mTmpFile != null && mTmpFile.exists()){
                    mTmpFile.delete();
                }
            }
        }
    }

    @SuppressLint("NewApi") @Override
    public void onConfigurationChanged(Configuration newConfig) {
        L.INSTANCE.d(TAG, "on change");

        if(mFolderPopupWindow != null){
            if(mFolderPopupWindow.isShowing()){
                mFolderPopupWindow.dismiss();
            }
        }

        mGridView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            public void onGlobalLayout() {

//                final int height = mGridView.getHeight();
//
//                final int desireSize = getResources().getDimensionPixelOffset(R.dimen.imageselector_default_image_size);
//                Log.d(TAG, "Desire Size = " + desireSize);
//                final int numCount = mGridView.getWidth() / desireSize;
//                Log.d(TAG, "Grid Size = " + mGridView.getWidth());
//                Log.d(TAG, "num count = " + numCount);
//                final int columnSpace = getResources().getDimensionPixelOffset(R.dimen.space_size);
//                int columnWidth = (mGridView.getWidth() - columnSpace * (numCount - 1)) / numCount;
//                mImageAdapter.setItemSize(columnWidth);
                setGridViewAutoWidthAndHeight(mGridView);
                if (mFolderPopupWindow != null) {
                    mFolderPopupWindow.setHeight(mGridView.getHeight() * 5 / 8);
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    mGridView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    mGridView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
            }
        });

        super.onConfigurationChanged(newConfig);

    }

    /**
     * 选择相机
     */
    private void showCameraAction() {
        // 跳转到系统照相机
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(cameraIntent.resolveActivity(getActivity().getPackageManager()) != null){
            // 设置系统相机拍照后的输出路径
            // 创建临时文件
            mTmpFile = FileUtils.createTmpFile(getActivity());
            mTmpPath = mTmpFile.getAbsolutePath();
            Uri mImageCaptureUri;
           try {
               if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                   ContentValues contentValues = new ContentValues(1);
                   contentValues.put(MediaStore.Images.Media.DATA, mTmpPath);
                   mImageCaptureUri = getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,contentValues);
               } else {
                   mImageCaptureUri = Uri.fromFile(mTmpFile);
               }
               cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, mImageCaptureUri);
               startActivityForResult(cameraIntent, REQUEST_CAMERA);
           }catch (Exception e) {
           }

        }else{
            Toast.makeText(getActivity(), R.string.imageselector_preview_msg_no_camera, Toast.LENGTH_SHORT).show();
        }

//        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        if(cameraIntent.resolveActivity(getActivity().getPackageManager()) != null){
//            // 设置系统相机拍照后的输出路径
//            // 创建临时文件
//            mTmpFile = FileUtils.createTmpFile(getActivity());
//            Intent camera = new Intent(getActivity(), ImageCameraActivity.class);
//            camera.putExtras(ImageCameraActivity.getBundle(mTmpFile));
//            startActivityForResult(camera,REQUEST_CAMERA);
//        }else{
//            Toast.makeText(getActivity(), R.string.imageselector_preview_msg_no_camera, Toast.LENGTH_SHORT).show();
//        }
    }
    /**
     * 选择图片操作
     * @param image
     */
    private void selectImageFromGrid(ImageSelectorImageBean image, int mode) {
        if(image != null) {
            // 多选模式
            if(mode == Mode.MODE_MULTI) {
                if (resultList.contains(image.getImagePath())) {
                    resultList.remove(image.getImagePath());
                    if(resultList.size() != 0) {
                        mPreviewBtn.setEnabled(true);
                        mPreviewBtn.setText(getResources().getString(R.string.imageselector_preview) + "(" + resultList.size() + ")");
                    }else{
                        mPreviewBtn.setEnabled(false);
                        mPreviewBtn.setText(R.string.imageselector_preview);
                    }
                    if (mCallback != null) {
                        mCallback.onImageUnselected(image.getImagePath());
                    }
                } else {
                    // 判断选择数量问题
                    if(mDesireImageCount == resultList.size()){
                        Toast.makeText(getActivity(), R.string.imageselector_msg_amount_limit, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    resultList.add(image.getImagePath());
                    mPreviewBtn.setEnabled(true);
                    mPreviewBtn.setText(getResources().getString(R.string.imageselector_preview) + "(" + resultList.size() + ")");
                    if (mCallback != null) {
                        mCallback.onImageSelected(image.getImagePath());
                    }
                }
                mImageAdapter.select(image);
            }else if(mode == Mode.MODE_SINGLE||mode == Mode.MODE_CLIP){
                // 单选模式
                if(mCallback != null){
                    mCallback.onSingleImageSelected(image.getImagePath());
                }
            }
        }
    }

    private LoaderManager.LoaderCallbacks<Cursor> mLoaderCallback = new LoaderManager.LoaderCallbacks<Cursor>() {

        private final String[] IMAGE_PROJECTION = {
                MediaStore.Images.Media.DATA,//日期
                MediaStore.Images.Media.DISPLAY_NAME,//图片名字
                MediaStore.Images.Media.DATE_ADDED,//创建日期
                MediaStore.Images.Media._ID };//图片Id

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            if(id == LOADER_ALL) {
                CursorLoader cursorLoader = new CursorLoader(getActivity(),
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, IMAGE_PROJECTION,
                        null, null, IMAGE_PROJECTION[2] + " DESC");
                return cursorLoader;
            }else if(id == LOADER_CATEGORY){
                CursorLoader cursorLoader = new CursorLoader(getActivity(),
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, IMAGE_PROJECTION,
                        IMAGE_PROJECTION[0]+" like '%"+args.getString("path")+"%'", null, IMAGE_PROJECTION[2] + " DESC");
                return cursorLoader;
            }

            return null;
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            if (data != null) {
                List<ImageSelectorImageBean> images = new ArrayList<ImageSelectorImageBean>();
                int count = data.getCount();
                if (count > 0) {
                    data.moveToFirst();
                    do{
                        String path = data.getString(data.getColumnIndexOrThrow(IMAGE_PROJECTION[0]));
                        String name = data.getString(data.getColumnIndexOrThrow(IMAGE_PROJECTION[1]));
                        long dateTime = data.getLong(data.getColumnIndexOrThrow(IMAGE_PROJECTION[2]));
                        ImageSelectorImageBean image = new ImageSelectorImageBean(path, name, dateTime);
                        images.add(image);
                        if( !hasFolderGened ) {
                            // 获取文件夹名称
                            File imageFile = new File(path);
                            File folderFile = imageFile.getParentFile();
                            if (folderFile == null) continue;
                            ImageSelectorFolderBean folder = new ImageSelectorFolderBean();
                            folder.setFileDirectoryName(folderFile.getName());
                            folder.setFristImagePath(folderFile.getAbsolutePath());
                            folder.setImageSelectorImageBean(image);
                            
                            if (!mResultFolder.contains(folder)) {
                                List<ImageSelectorImageBean> imageList = new ArrayList<ImageSelectorImageBean>();
                                imageList.add(image);
                                folder.setImages(imageList);
                                mResultFolder.add(folder);
                            } else {
                                // 更新
                            	ImageSelectorFolderBean f = mResultFolder.get(mResultFolder.indexOf(folder));
                                f.getImages().add(image);
                            }
                        }

                    }while(data.moveToNext());

                    mImageAdapter.setData(images);

                    // 设定默认选择
                    if(resultList != null && resultList.size()>0){
                        mImageAdapter.setDefaultSelected(resultList);
                    }

                    mFolderAdapter.setData(mResultFolder);
                    hasFolderGened = true;

                }
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
        	
        }
    };

    /**
     * 回调接口
     */
    public interface Callback{
        void onSingleImageSelected(String path);
        void onImageSelected(String path);
        void onImageUnselected(String path);
        void onCameraShot(File imageFile);
    }

    @SuppressLint("NewApi") @Override
	public void onClick(View v) {
		int viewId = v.getId();
		
		if(viewId == R.id.btn_category){
            if(mFolderPopupWindow == null){
                createPopupFolderList(mGridWidth, mGridHeight);
            }

            if (mFolderPopupWindow.isShowing()) {
                mFolderPopupWindow.dismiss();
            } else {
                mFolderPopupWindow.show();
                int index = mFolderAdapter.getSelectIndex();
                index = index == 0 ? index : index - 1;
                mFolderPopupWindow.getListView().setSelection(index);
            }
			return;
		}
		
		if(viewId == R.id.btn_preview){
			ImageSelectorBrowseActivity.startActivity(getActivity(), resultList, 0,true);
			return;
		}
	}
}
