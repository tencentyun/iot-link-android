package com.tencent.iot.explorer.link.kitlink.util.picture.utils;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;


import com.tencent.iot.explorer.link.R;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;


/**
 * 位图处理类
 */
public class BitmapUtils {
    public static final int UNCONSTRAINED = -1;

    /**
     * 将大的位图压缩
     */
    public static Bitmap compress(Bitmap image) {
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.JPEG, 100, os);
            Log.e("--------","length: "+os.toByteArray().length / 1024 + " kb");
            if (os.toByteArray().length / 1024 > 300) {//判断如果图片大于150kb,进行压缩避免在生成图片（BitmapFactory.decodeStream）时溢出
                os.reset();//重置baos即清空baos
                image.compress(Bitmap.CompressFormat.JPEG, 20, os);//这里压缩20%，把压缩后的数据存放到baos中
            } else {
                return image;
            }
            BitmapFactory.Options newOpts = new BitmapFactory.Options();
            //开始读入图片，此时把options.inJustDecodeBounds 设回true了
            newOpts.inJustDecodeBounds = true;
            newOpts.inPreferredConfig = Bitmap.Config.RGB_565;
            newOpts.inJustDecodeBounds = false;
//            int w = newOpts.outWidth;
//            int h = newOpts.outHeight;
//            float hh = 240;// 设置高度为240f时，可以明显看到图片缩小了
//            float ww = 120;// 设置宽度为120f，可以明显看到图片缩小了
//            //缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
//            int be = 1;//be=1表示不缩放
//            if (w > h && w > ww) {//如果宽度大的话根据宽度固定大小缩放
//                be = (int) (newOpts.outWidth / ww);
//            } else if (w < h && h > hh) {//如果高度高的话根据宽度固定大小缩放
//                be = (int) (newOpts.outHeight / hh);
//            }
//            if (be <= 0) be = 1;
//            newOpts.inSampleSize = be;//设置缩放比例

            //重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
            ByteArrayInputStream  is = new ByteArrayInputStream(os.toByteArray());
            Bitmap  bitmap = BitmapFactory.decodeStream(is, null, newOpts);
            //压缩好比例大小后再进行质量压缩
//            return compress(bitmap); // 这里再进行质量压缩的意义不大，反而耗资源，删除
            return bitmap;
        } catch (Exception e) {
            //在这里最好return 一个默认的图片
            return image;
        }
//        return null;
    }

    /**
     * 将图片路径转为Bitmap
     */
    public static Bitmap convertToBitmap(String path, int w, int h) {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        // 设置为ture只获取图片大小
        opts.inJustDecodeBounds = true;
        opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
        // 返回为空
        BitmapFactory.decodeFile(path, opts);
        int width = opts.outWidth;
        int height = opts.outHeight;
        float scaleWidth = 0.f, scaleHeight = 0.f;
        if (width > w || height > h) {
            // 缩放
            scaleWidth = ((float) width) / w;
            scaleHeight = ((float) height) / h;
        }
        opts.inJustDecodeBounds = false;
        float scale = Math.max(scaleWidth, scaleHeight);
        opts.inSampleSize = (int) scale;
        WeakReference<Bitmap> weak = new WeakReference<Bitmap>(BitmapFactory.decodeFile(path, opts));
        if (weak == null) {
            return null;
        }
        try {
            return Bitmap.createScaledBitmap(weak.get(), w, h, true);
        } catch (Exception e) {
            return getBitmapFromDrawableRes(R.drawable.imageselector_default_error);
        }
    }

    /**
     * 下载失败与获取失败时都统一显示默认下载失败图片
     *
     * @return
     */
    public static Bitmap getBitmapFromDrawableRes(int res) {
        try {
            return null;
//            return getBitmapImmutableCopy(MyUtil.getContext().getResources(), res);
        } catch (Exception e) {
            return null;
        }
    }

    public static final Bitmap getBitmapImmutableCopy(Resources res, int id) {
        return getBitmap(res.getDrawable(id)).copy(Bitmap.Config.RGB_565, false);
    }

    public static final Bitmap getBitmap(Drawable dr) {
        if (dr == null) {
            return null;
        }

        if (dr instanceof BitmapDrawable) {
            return ((BitmapDrawable) dr).getBitmap();
        }
        return null;
    }


    public static Bitmap getBitmap(String filePath, int width, int height) throws FileNotFoundException {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new FileNotFoundException();
        }
        return getBitmap(new FileInputStream(file), width, height);
    }

    public static Bitmap getBitmap(InputStream is, int width, int height) throws FileNotFoundException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        try {
            while ((len = is.read(buffer)) != -1) {
                baos.write(buffer, 0, len);
            }
        } catch (IOException e) {
        } finally {
            if (is != null) try {
                is.close();
            } catch (IOException e) {
            }
        }
        byte[] bytes = baos.toByteArray();
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(bytes, 0, bytes.length, opts);

        opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
        opts.inPurgeable = true;
        opts.inInputShareable = true;
        width = width == -1 ? opts.outWidth : width;
        height = height == -1 ? opts.outHeight : height;
        opts.inSampleSize = computeSampleSize(opts, width > height ? width
                : height, width * height);
        opts.inJustDecodeBounds = false;
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length, opts);
    }

    private static int computeSampleSize(BitmapFactory.Options options,
                                         int minSideLength, int maxNumOfPixels) {
        int initialSize = computeInitialSampleSize(options, minSideLength,
                maxNumOfPixels);

        int roundedSize;
        if (initialSize <= 8) {
            roundedSize = 1;
            while (roundedSize < initialSize) {
                roundedSize <<= 1;
            }
        } else {
            roundedSize = (initialSize + 7) / 8 * 8;
        }
        return roundedSize;
    }

    private static int computeInitialSampleSize(BitmapFactory.Options options,
                                                int minSideLength, int maxNumOfPixels) {
        double w = options.outWidth;
        double h = options.outHeight;
        int lowerBound = (maxNumOfPixels == UNCONSTRAINED) ? 1 : (int) Math
                .ceil(Math.sqrt(w * h / maxNumOfPixels));
        int upperBound = (minSideLength == UNCONSTRAINED) ? 128 : (int) Math
                .min(Math.floor(w / minSideLength),
                        Math.floor(h / minSideLength));
        if (upperBound < lowerBound) {
            return lowerBound;
        }
        if ((maxNumOfPixels == UNCONSTRAINED)
                && (minSideLength == UNCONSTRAINED)) {
            return 1;
        } else if (minSideLength == UNCONSTRAINED) {
            return lowerBound;
        } else {
            return upperBound;
        }
    }

    public static Bitmap getBitmap(Resources resources, int resID, int width, int height) {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(resources, resID, opts);

        opts.inPreferredConfig = Bitmap.Config.RGB_565;
        opts.inPurgeable = true;
        opts.inInputShareable = true;
        opts.inSampleSize = computeSampleSize(opts, width > height ? width
                : height, width * height);
        opts.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(resources, resID, opts);
    }

    public static Bitmap convertToBitmap(Drawable drawable, int widthPixels) {
        Bitmap mutableBitmap = Bitmap.createBitmap(widthPixels, widthPixels, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(mutableBitmap);
        drawable.setBounds(0, 0, widthPixels, widthPixels);
        drawable.draw(canvas);
        return mutableBitmap;
    }

    public static Bitmap createWaterMark(Bitmap target, String mark, int width, int textSize) {
        Bitmap bmp = Bitmap.createBitmap(width, width, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmp);

        Paint p = new Paint();
        p.setColor(Color.WHITE);    // 水印的颜色
        p.setTextSize(textSize);  // 水印的字体大小
        p.setAntiAlias(true);   // 去锯齿
        canvas.drawBitmap(target, 0, 0, p);
        float len = p.measureText(mark);
        float hei = Math.abs(p.ascent() + p.descent());
        // 在左边的中间位置开始添加水印
        canvas.drawText(mark, width / 2 - len / 2, width / 2 + hei / 2, p);
        canvas.save();
        canvas.restore();
        return bmp;
    }

    public static Bitmap createWaterMark(Drawable drawable, String mark, int width, int textSize) {
        Bitmap target = convertToBitmap(drawable, width);
        return createWaterMark(target, mark, width, textSize);
    }
}