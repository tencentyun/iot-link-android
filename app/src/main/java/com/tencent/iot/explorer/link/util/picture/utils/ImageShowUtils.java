package com.tencent.iot.explorer.link.util.picture.utils;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.AbsoluteSizeSpan;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.tencent.iot.explorer.link.App;
import com.tencent.iot.explorer.link.R;
import com.tencent.iot.explorer.link.util.T;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Random;

/**
 * 常用公共方法工具类
 */
public class ImageShowUtils {

	private static final String TAG    = ImageShowUtils.class.getName();
	protected static boolean    isExit = false;
	private static long         lastClickTime;



	/**
	 * Methods: isFastClick
	 * Description: 防止多次点击
	 * 
	 * @exception null
	 * @return
	 *         boolean
	 */
	public synchronized static boolean isFastClick() {
		long time = System.currentTimeMillis();
		if (time - lastClickTime < 1500) {
			return true;
		}
		lastClickTime = time;
		return false;
	}

	/**
	 * Methods: exitApp
	 * Description: 退出程序
	 * 
	 * @exception null
	 *                void
	 */
	public static void exitApp() {
		// stopService(new Intent(this, MyLocationService.class));
		// app.exit();
	}

	/**
	 * Methods: hideSoftinput
	 * Description: 隐藏键盘
	 * 
	 * @exception null
	 * @param context
	 * @param view
	 *            void
	 */
	public static void hideSoftinput(Context context, View view) {
		InputMethodManager manager = (InputMethodManager) context
		        .getSystemService(Service.INPUT_METHOD_SERVICE);
		if (manager.isActive()) {
			manager.hideSoftInputFromWindow(view.getWindowToken(), 0);
		}
	}

	static DisplayMetrics  dm;
	/** 获取屏幕大小 dm.widthPixels*/
	public static DisplayMetrics getWindowMetrics(Activity activity) {
		dm = new DisplayMetrics();
		activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
		return dm;
	}

    public static String randNumber(){
		int max=10000;
		int min=1000;
		Random random = new Random();
		int s = random.nextInt(max)%(max-min+1) + min;
		long time = System.currentTimeMillis();
		return String.valueOf(s)+String.valueOf(time);
	}
	// 根据路径获得图片并压缩，返回bitmap用于显示
	public static Bitmap getSmallBitmap(String filePath) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inPreferredConfig =  Bitmap.Config.RGB_565;
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(filePath, options);

		// Calculate inSampleSize
		options.inSampleSize = calculateInSampleSize(options, 480, 800);

		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;

		return BitmapFactory.decodeFile(filePath, options);
	}

	//计算图片的缩放值
	public static int calculateInSampleSize(BitmapFactory.Options options,int reqWidth, int reqHeight) {
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {
			final int heightRatio = Math.round((float) height/ (float) reqHeight);
			final int widthRatio = Math.round((float) width / (float) reqWidth);
			inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
		}
		return inSampleSize;
	}

	//把bitmap转换成String
	public static String loadBitmapBase64(String filePath) {
		Bitmap bm = getSmallBitmap(filePath);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bm.compress(Bitmap.CompressFormat.JPEG, 60, baos);
		byte[] b = baos.toByteArray();
		return Base64.encodeToString(b, Base64.DEFAULT);
	}

	//直接把文件转换成String
	public static String loadBitmapToBase64(String filePath) {
//		Bitmap bm = getSmallBitmap(filePath);
		ByteArrayOutputStream baos=null;
		FileInputStream inputStream = null;
		try {
			inputStream= new FileInputStream(filePath);
			baos = new ByteArrayOutputStream();

			int len = 0;
			byte[] bytes = new byte[1024];
			while ((len=inputStream.read(bytes))!=-1){
				baos.write(bytes,0,len);
			}
			byte[] b = baos.toByteArray();
			return Base64.encodeToString(b, Base64.DEFAULT);
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			try {
				inputStream.close();
				baos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	//把语音转化为String
	public static String loadVoiceBase64(String filePath) {
		FileInputStream inputStream = null;
		ByteArrayOutputStream baos = null;
		try {
			inputStream= new FileInputStream(filePath);
			baos = new ByteArrayOutputStream();
			int len = 0;
			byte[] bytes = new byte[1024];
			while ((len=inputStream.read(bytes))!=-1){
				baos.write(bytes,0,len);
			}
			byte[] b = baos.toByteArray();
			return Base64.encodeToString(b, Base64.DEFAULT);
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			try {
				inputStream.close();
				baos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return null;
	}

//	public static  String loadBitmapBase64(Bitmap bitmap) {
//		String result = null;
//		ByteArrayOutputStream baos = null;
//		try {
//			if (bitmap != null) {
//				baos = new ByteArrayOutputStream();
//				bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
//
//				baos.flush();
//				baos.close();
//				byte[] bitmapBytes = baos.toByteArray();
//				result = Base64.encodeToString(bitmapBytes, Base64.DEFAULT);
//			}
//		} catch (IOException e) {
//			e.printStackTrace();
//		} finally {
//			try {
//				if (baos != null) {
//					baos.flush();
//					baos.close();
//				}
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		}
//		return result;
//	}
	public static String bitmapToBase64(Bitmap bitmap) {
		String result = null;
		ByteArrayOutputStream baos = null;
		try {
			if (bitmap != null) {
				baos = new ByteArrayOutputStream();
				bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
				baos.flush();
				baos.close();
				byte[] bitmapBytes = baos.toByteArray();
				result = Base64.encodeToString(bitmapBytes, Base64.DEFAULT);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (baos != null) {
					baos.flush();
					baos.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	public static int getTotalPageSize(int totalPage,int pageSize) {//总页数
		System.out.println("total" + totalPage);
		System.out.println("pageSize" + pageSize);
		if(totalPage%pageSize == 0) {
			return totalPage/pageSize;
		}else{
			return (totalPage/pageSize) +1;
		}
	}

	public static void setFontDiffrentSize(TextView v,String content,int dividerId,int bigSize,int smallSize) {
		if(null ==content || content.isEmpty()) return;
		int len = content.toString().trim().length();
		Spannable styledText = new SpannableString(content);
		if(null != dm) {
			bigSize = (int) (bigSize * dm.density);
			smallSize = (int) (smallSize * dm.density);
		}
		styledText.setSpan(new AbsoluteSizeSpan(smallSize), 0, dividerId, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		styledText.setSpan(new AbsoluteSizeSpan(bigSize), dividerId, len, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		v.setText(styledText, TextView.BufferType.SPANNABLE);
	}

	public static void setFontDiffrentOneSize(TextView v,String content,int dividerId,int bigSize,int smallSize) {
		if(null ==content || content.isEmpty()) return;
		int len = content.toString().trim().length();
		Spannable styledText = new SpannableString(content);
		if(null != dm) {
			bigSize = (int) (bigSize * dm.density);
			smallSize = (int) (smallSize * dm.density);
		}
		styledText.setSpan(new AbsoluteSizeSpan(smallSize), 0, dividerId, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		styledText.setSpan(new AbsoluteSizeSpan(bigSize), dividerId, len-1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		styledText.setSpan(new AbsoluteSizeSpan(smallSize), len-1, len, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		v.setText(styledText, TextView.BufferType.SPANNABLE);
	}

	//直辖市
	public static boolean isMunicipalities(String address) {
		if(address == null) return false;
		if(address.equals(T.getContext().getString(R.string.city_beijing))||
				address.equals(T.getContext().getString(R.string.city_tianjin))||
				address.equals(T.getContext().getString(R.string.city_shanghai))||
				address.equals(T.getContext().getString(R.string.city_chongqing))){
			return true;
		}
		return false;
	}
}
