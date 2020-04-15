package com.yho.image.imageselectorbrowser;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.yho.image.imp.ImageManager;
import com.yho.imageselectorbrowser.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**  
 * ClassName: ImageSelectorPopupFolderAdapter   
 * Description: ListPopupWindow展示文件夹目录的Adapter    
 * @author wuqionghai  
 * @version 1.0  2015-9-17
 */
public class ImageSelectorPopupFolderAdapter extends BaseAdapter {

    private Context mContext;
    private LayoutInflater mInflater;

    private List<ImageSelectorFolderBean> mFolders = new ArrayList<ImageSelectorFolderBean>();

    private int mImageSize;

    private int mLastSelected = 0;

    public ImageSelectorPopupFolderAdapter(Context context){
        mContext = context;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mImageSize = mContext.getResources().getDimensionPixelOffset(R.dimen.imageselector_folder_cover_size);
    }

    /**  
     * Methods: setData  
     * Description: 设置数据集 
     * @exception null  
     * @param folders 
     * void
     */  
    public void setData(List<ImageSelectorFolderBean> folders) {
        if(folders != null && folders.size()>0){
            mFolders = folders;
        }else{
            mFolders.clear();
        }
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mFolders.size()+1;
    }

    @Override
    public ImageSelectorFolderBean getItem(int i) {
        if(i == 0) return null;
        return mFolders.get(i-1);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder;
        if(view == null){
            view = mInflater.inflate(R.layout.imageselector_pop_floder_list_item, viewGroup, false);
            holder = new ViewHolder(view);
        }else{
            holder = (ViewHolder) view.getTag();
        }
        if (holder != null) {
            if(i == 0){
                holder.tv_folder_name.setText(R.string.imageselector_all_folder);
                holder.tv_folder_img_count.setText(getTotalImageSize() + mContext.getResources().getString(R.string.imageselector_sheets));
                if(mFolders.size()>0){
                	ImageSelectorFolderBean f = mFolders.get(0);
                	ImageManager.show(mContext, new File(f.getImageSelectorImageBean().getImagePath()),
                			R.drawable.imageselector_default_error, mImageSize, mImageSize, holder.iv_folder_first_img);
//                    Picasso.with(mContext)
//                            .load(new File(f.getImageSelectorImageBean().getImagePath()))
//                            .error(R.drawable.imageselector_default_error)
//                            .resize(mImageSize, mImageSize)
//                            .centerCrop()
//                            .into(holder.iv_folder_first_img);
                }
            }else {
                holder.bindData(getItem(i));
            }
            if(mLastSelected == i){
                holder.iv_indicator_select.setVisibility(View.VISIBLE);
            }else{
                holder.iv_indicator_select.setVisibility(View.INVISIBLE);
            }
        }
        return view;
    }

    /**  
     * Methods: getTotalImageSize  
     * Description: 获取全部图片的个数 
     * @exception null  
     * @return 
     * int
     */  
    private int getTotalImageSize(){
        int result = 0;
        if(mFolders != null && mFolders.size()>0){
            for (ImageSelectorFolderBean f: mFolders){
                result += f.getImages().size();
            }
        }
        return result;
    }

    public void setSelectIndex(int i) {
        if(mLastSelected == i) return;

        mLastSelected = i;
        notifyDataSetChanged();
    }

    public int getSelectIndex(){
        return mLastSelected;
    }

    class ViewHolder{
        ImageView iv_folder_first_img;
        TextView tv_folder_name;
        TextView tv_folder_img_count;
        ImageView iv_indicator_select;
        
        ViewHolder(View view){
        	iv_folder_first_img = (ImageView)view.findViewById(R.id.iv_folder_first_img);
        	tv_folder_name = (TextView) view.findViewById(R.id.tv_folder_name);
        	tv_folder_img_count = (TextView) view.findViewById(R.id.tv_folder_img_count);
        	iv_indicator_select = (ImageView) view.findViewById(R.id.iv_indicator_select);
            view.setTag(this);
        }

        void bindData(ImageSelectorFolderBean data) {
        	tv_folder_name.setText(data.getFileDirectoryName());
        	tv_folder_img_count.setText(data.getImages().size() + mContext.getResources().getString(R.string.imageselector_sheets));
            // 显示图片
        	ImageManager.show(mContext, new File(data.getImageSelectorImageBean().getImagePath()),
        			R.drawable.imageselector_default_error, mImageSize, mImageSize, iv_folder_first_img);
//            Picasso.with(mContext)
//                    .load(new File(data.getImageSelectorImageBean().getImagePath()))
//                    .placeholder(R.drawable.imageselector_default_error)
//                    .resize(mImageSize, mImageSize)
//                    .centerCrop()
//                    .into(iv_folder_first_img);
        }
    }

}
