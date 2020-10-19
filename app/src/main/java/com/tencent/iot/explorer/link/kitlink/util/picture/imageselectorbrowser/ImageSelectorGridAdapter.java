package com.tencent.iot.explorer.link.kitlink.util.picture.imageselectorbrowser;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.tencent.iot.explorer.link.R;
import com.tencent.iot.explorer.link.kitlink.util.picture.imp.ImageManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**  
 * 展示所有图片的Adapter
 */
public class ImageSelectorGridAdapter extends BaseAdapter {

	private static final int TYPE_CAMERA = 0;
	private static final int TYPE_NORMAL = 1;

	private Context mContext;

	private LayoutInflater mInflater;
	private boolean mShowCamera = true;
	private boolean mShowSelectIndicator = true;

	private List<ImageSelectorImageBean> mImages = new ArrayList<ImageSelectorImageBean>();
	private List<ImageSelectorImageBean> mSelectedImages = new ArrayList<ImageSelectorImageBean>();

	private int mItemSize;
	private GridView.LayoutParams mItemLayoutParams;

	public ImageSelectorGridAdapter(Context context, boolean showCamera) {
		mContext = context;
		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.mShowCamera = showCamera;
		mItemLayoutParams = new GridView.LayoutParams(
				GridView.LayoutParams.MATCH_PARENT,
				GridView.LayoutParams.MATCH_PARENT);
	}

	/**  
	 * Methods: showSelectIndicator  
	 * Description: 显示选择指示器 
	 * @exception null  
	 * @param b 
	 * void
	 */  
	public void showSelectIndicator(boolean b) {
		mShowSelectIndicator = b;
	}

	/**  
	 * Methods: setShowCamera  
	 * Description: 设置是否显示照相机 
	 * @exception null  
	 * @param b 
	 * void
	 */  
	public void setShowCamera(boolean b) {
		if (mShowCamera == b)
			return;

		mShowCamera = b;
		notifyDataSetChanged();
	}

	public boolean isShowCamera() {
		return mShowCamera;
	}

	/**  
	 * Methods: select  
	 * Description: 选择某个图片，改变选择状态 
	 * @exception null  
	 * @param image 
	 * void
	 */  
	public void select(ImageSelectorImageBean image) {
		if (mSelectedImages.contains(image)) {
			mSelectedImages.remove(image);
		} else {
			mSelectedImages.add(image);
		}
		notifyDataSetChanged();
	}

	/**  
	 * Methods: setDefaultSelected  
	 * Description: 通过图片路径设置默认选择 
	 * @exception null  
	 * @param resultList 
	 * void
	 */  
	public void setDefaultSelected(ArrayList<String> resultList) {
		for (String path : resultList) {
			ImageSelectorImageBean image = getImageByPath(path);
			if (image != null) {
				mSelectedImages.add(image);
			}
		}
		if (mSelectedImages.size() > 0) {
			notifyDataSetChanged();
		}
	}

	/**  
	 * Methods: getImageByPath  
	 * Description: 获取一个图片实体类对象 
	 * @exception null  
	 * @param path
	 * @return 
	 * ImageSelectorImageBean
	 */  
	private ImageSelectorImageBean getImageByPath(String path) {
		if (mImages != null && mImages.size() > 0) {
			for (ImageSelectorImageBean image : mImages) {
				if (image.getImagePath().equalsIgnoreCase(path)) {
					return image;
				}
			}
		}
		return null;
	}

	/**  
	 * Methods: setData  
	 * Description: 设置数据集 
	 * @exception null  
	 * @param images 
	 * void
	 */  
	public void setData(List<ImageSelectorImageBean> images) {
		mSelectedImages.clear();

		if (images != null && images.size() > 0) {
			mImages = images;
		} else {
			mImages.clear();
		}
		notifyDataSetChanged();
	}

	/**  
	 * Methods: setItemSize  
	 * Description: 重置每个Column的Size 
	 * @exception null  
	 * @param columnWidth 
	 * void
	 */  
	public void setItemSize(int columnWidth) {

		if (mItemSize == columnWidth) {
			return;
		}

		mItemSize = columnWidth;

		mItemLayoutParams = new GridView.LayoutParams(mItemSize, mItemSize);

		notifyDataSetChanged();
	}

	@Override
	public int getViewTypeCount() {
		return 2;
	}

	@Override
	public int getItemViewType(int position) {
		if (mShowCamera) {
			return position == 0 ? TYPE_CAMERA : TYPE_NORMAL;
		}
		return TYPE_NORMAL;
	}

	@Override
	public int getCount() {
		return mShowCamera ? mImages.size() + 1 : mImages.size();
	}

	@Override
	public ImageSelectorImageBean getItem(int i) {
		if (mShowCamera) {
			if (i == 0) {
				return null;
			}
			return mImages.get(i - 1);
		} else {
			return mImages.get(i);
		}
	}

	@Override
	public long getItemId(int i) {
		return i;
	}

	@Override
	public View getView(int i, View view, ViewGroup viewGroup) {

		int type = getItemViewType(i);
		if (type == TYPE_CAMERA) {
			view = mInflater.inflate(R.layout.imageselector_camera_gv_item, viewGroup, false);
			view.setTag(null);
		} else if (type == TYPE_NORMAL) {
			ViewHolde holde;
			if (view == null) {
				view = mInflater.inflate(R.layout.imageselector_image_gv_item, viewGroup, false);
				holde = new ViewHolde(view);
			} else {
				holde = (ViewHolde) view.getTag();
				if (holde == null) {
					view = mInflater.inflate(R.layout.imageselector_image_gv_item, viewGroup, false);
					holde = new ViewHolde(view);
				}
			}
			if (holde != null) {
				holde.bindData(getItem(i));
			}
		}

		/** Fixed View Size */
		GridView.LayoutParams lp = (GridView.LayoutParams) view
				.getLayoutParams();
		if (lp.height != mItemSize) {
			view.setLayoutParams(mItemLayoutParams);
		}

		return view;
	}

	class ViewHolde {
		ImageView iv_default_img;
		ImageView iv_indicator_select;
		View mask;

		ViewHolde(View view) {
			iv_default_img = (ImageView) view.findViewById(R.id.iv_default_img);
			iv_indicator_select = (ImageView) view.findViewById(R.id.iv_indicator_select);
			mask = view.findViewById(R.id.mask);
			view.setTag(this);
		}

		void bindData(final ImageSelectorImageBean data) {
			if (data == null)
				return;
			// 处理单选和多选状态
			if (mShowSelectIndicator) {
				iv_indicator_select.setVisibility(View.VISIBLE);
				if (mSelectedImages.contains(data)) {
					// 设置选中状态
					iv_indicator_select
							.setImageResource(R.drawable.imageselector_btn_selected);
					mask.setVisibility(View.VISIBLE);
				} else {
					// 未选择
					iv_indicator_select
							.setImageResource(R.drawable.imageselector_btn_unselected);
					mask.setVisibility(View.GONE);
				}
			} else {
				iv_indicator_select.setVisibility(View.GONE);
			}
			File imageFile = new File(data.getImagePath());

			if (mItemSize > 0) {
				// 显示图片
				ImageManager.show(mContext, imageFile,
	        			R.drawable.imageselector_default_error, mItemSize, mItemSize, iv_default_img);
//				Picasso.with(mContext).load(imageFile)
//						.placeholder(R.drawable.imageselector_default_error)
//						.error(R.drawable.imageselector_default_error)
//						.resize(mItemSize, mItemSize).centerCrop()
//						.into(iv_default_img);
			}
		}
	}

}
