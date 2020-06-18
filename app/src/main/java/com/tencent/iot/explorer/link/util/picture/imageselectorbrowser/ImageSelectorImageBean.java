package com.tencent.iot.explorer.link.util.picture.imageselectorbrowser;

/**  
 * 单张图片的实体类
 */
public class ImageSelectorImageBean {
	private String mImagePath;
	private String mImageName;
	private long mImageFoundTime;
	
	public ImageSelectorImageBean(String imagePath,String imageName,long imageFoundTime) {
		this.mImageFoundTime = imageFoundTime;
		this.mImageName = imageName;
		this.mImagePath = imagePath;
	}
    
    public String getImagePath() {
		return mImagePath;
	}

	public void setImagePath(String mImagePath) {
		this.mImagePath = mImagePath;
	}

	public String getImageName() {
		return mImageName;
	}

	public void setImageName(String mImageName) {
		this.mImageName = mImageName;
	}

	public long getImageFoundTime() {
		return mImageFoundTime;
	}

	public void setImageFoundTime(long mImageFoundTime) {
		this.mImageFoundTime = mImageFoundTime;
	}

    @Override
    public boolean equals(Object o) {
        try {
            ImageSelectorImageBean other = (ImageSelectorImageBean) o;
            return this.mImagePath.equalsIgnoreCase(other.mImagePath);
        }catch (ClassCastException e){
            e.printStackTrace();
        }
        return super.equals(o);
    }
}
