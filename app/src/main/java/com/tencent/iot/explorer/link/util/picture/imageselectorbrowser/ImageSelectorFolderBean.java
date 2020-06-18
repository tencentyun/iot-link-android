package com.tencent.iot.explorer.link.util.picture.imageselectorbrowser;

import java.util.List;

/**  
 * 单个目录的实体类
 */
public class ImageSelectorFolderBean {

	private String mFileDirectoryName;
    private String mFristImagePath;
    private ImageSelectorImageBean mImageSelectorImageBean;
    private List<ImageSelectorImageBean> mImages;

    public String getFileDirectoryName() {
		return mFileDirectoryName;
	}

	public void setFileDirectoryName(String mFileDirectoryName) {
		this.mFileDirectoryName = mFileDirectoryName;
	}

	public String getFristImagePath() {
		return mFristImagePath;
	}

	public void setFristImagePath(String mFristImagePath) {
		this.mFristImagePath = mFristImagePath;
	}

	public ImageSelectorImageBean getImageSelectorImageBean() {
		return mImageSelectorImageBean;
	}

	public void setImageSelectorImageBean(
			ImageSelectorImageBean mImageSelectorImageBean) {
		this.mImageSelectorImageBean = mImageSelectorImageBean;
	}

	public List<ImageSelectorImageBean> getImages() {
		return mImages;
	}
	
	public void setImages(List<ImageSelectorImageBean> mImages) {
		this.mImages = mImages;
	}
	
    @Override
    public boolean equals(Object o) {
        try {
            ImageSelectorFolderBean other = (ImageSelectorFolderBean) o;
            return this.mFristImagePath.equalsIgnoreCase(other.mFristImagePath);
        }catch (ClassCastException e){
            e.printStackTrace();
        }
        return super.equals(o);
    }
}
