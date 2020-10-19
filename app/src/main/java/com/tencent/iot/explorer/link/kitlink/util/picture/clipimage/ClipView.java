package com.tencent.iot.explorer.link.kitlink.util.picture.clipimage;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.view.View;

import com.tencent.iot.explorer.link.R;;

public class ClipView extends View {
    private Paint paint = new Paint();
    private Paint borderPaint = new Paint();
    private Paint guidePaint = new Paint();
    private Paint mCornerPaint = new Paint();

    /**
     * 自定义顶部栏高度，如不是自定义，则默认为0即可
     */
    private int customTopBarHeight = 0;
    /**
     * 裁剪框长宽比，默认4：3
     */
    private double clipRatio = 0.75;
    /**
     * 裁剪框宽度
     */
    private int clipWidth = -1;
    /**
     * 裁剪框高度
     */
    private int clipHeight = -1;
    /**
     * 裁剪框左边空留宽度
     */
    private int clipLeftMargin = 0;
    /**
     * 裁剪框上边空留宽度
     */
    private int clipTopMargin = 0;
    /**
     * 裁剪框边框宽度
     */
    private int clipBorderWidth = 4;
    private boolean isSetMargin = false;
    private OnDrawListenerComplete listenerComplete;

    private float mBorderThickness;
    private float mCornerLength;
    private float mCornerThickness;
    private float mBottomThickness;
    private Context mContext;
    private boolean isClip = false;

    private int mMaxImageSize = 0;

    public ClipView(Context context) {
        super(context);
        init(context);
    }

    public ClipView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ClipView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        mBorderThickness = context.getResources().getDimension(R.dimen.border_thickness);
        mCornerThickness = context.getResources().getDimension(R.dimen.corner_thickness);
        mCornerLength = context.getResources().getDimension(R.dimen.corner_length);
        mBottomThickness = context.getResources().getDimension(R.dimen.common_normal_title_height_size);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if(isClip){
            return;
        }
        if (mMaxImageSize == 0){
            mMaxImageSize = (canvas.getMaximumBitmapHeight()>canvas.getMaximumBitmapWidth())?
                    canvas.getMaximumBitmapHeight()-1:canvas.getMaximumBitmapWidth()-1;
        }

        int width = this.getWidth();
        int height = this.getHeight();
        // 如没有显示设置裁剪框高度和宽度，取默认值
        if (clipWidth == -1 || clipHeight == -1) {
            clipWidth = width - 50;
            clipHeight = (int) (clipWidth * clipRatio);
            // 横屏
            if (width > height) {
                clipHeight = height - 50;
                clipWidth = (int) (clipHeight / clipRatio);
            }
        }
        // 如没有显示设置裁剪框左和上预留宽度，取默认值
        if (!isSetMargin) {
            clipLeftMargin = (width - clipWidth) / 2;
            clipTopMargin = (height - clipHeight) / 2;
        }
        // 画阴影
        paint.setAlpha(200);
//		paint = PaintUtil.newBorderPaint(getContext().getResources());
        // top
        canvas.drawRect(0, customTopBarHeight, width, clipTopMargin, paint);
        // left
        canvas.drawRect(0, clipTopMargin, clipLeftMargin, clipTopMargin
                + clipHeight, paint);
        // right
        canvas.drawRect(clipLeftMargin + clipWidth, clipTopMargin, width,
                clipTopMargin + clipHeight, paint);
        // bottom
        canvas.drawRect(0, clipTopMargin + clipHeight, width, height-mBottomThickness, paint);

        //画网格
        guidePaint.setStyle(Style.STROKE);
        guidePaint.setColor(mContext.getResources().getColor(R.color.guideline));
        guidePaint.setStrokeWidth(1);
        final float oneThirdCropWidth = clipWidth / 3;
        final float x1 = clipLeftMargin + oneThirdCropWidth;
        canvas.drawLine(x1, clipTopMargin, x1, clipTopMargin + clipHeight, guidePaint);
        final float x2 = clipLeftMargin + oneThirdCropWidth + oneThirdCropWidth;
        canvas.drawLine(x2, clipTopMargin, x2, clipTopMargin + clipHeight, guidePaint);

        // Draw horizontal guidelines.
        final float oneThirdCropHeight = clipHeight / 3;
        final float y1 = clipTopMargin + oneThirdCropHeight;
        canvas.drawLine(clipLeftMargin, y1, clipLeftMargin + clipWidth, y1, guidePaint);
        final float y2 = clipTopMargin + oneThirdCropHeight * 2;
        canvas.drawLine(clipLeftMargin, y2, clipLeftMargin + clipWidth, y2, guidePaint);

        // 画边框
        borderPaint.setStyle(Style.STROKE);
        borderPaint.setColor(mContext.getResources().getColor(R.color.border));
        borderPaint.setStrokeWidth(mBorderThickness);
        canvas.drawRect(clipLeftMargin, clipTopMargin, clipLeftMargin
                + clipWidth, clipTopMargin + clipHeight, borderPaint);

        //描角
        mCornerPaint.setStyle(Style.STROKE);
        mCornerPaint.setColor(mContext.getResources().getColor(R.color.corner));
        mCornerPaint.setStrokeWidth(mCornerThickness);
        final float lateralOffset = (mCornerThickness - mBorderThickness) / 2f;
        // Absolute value of the offset by which to start the corner line such that the line is drawn all the way to form a corner edge with the adjacent side.
        final float startOffset = mCornerThickness - (mBorderThickness / 2f);

        // Top-left corner: left side
        canvas.drawLine(clipLeftMargin - lateralOffset, clipTopMargin - startOffset, clipLeftMargin - lateralOffset, clipTopMargin + mCornerLength, mCornerPaint);
//		// Top-left corner: top side
        canvas.drawLine(clipLeftMargin - startOffset, clipTopMargin - lateralOffset, clipLeftMargin + mCornerLength, clipTopMargin - lateralOffset, mCornerPaint);

        // Top-right corner: right side
        canvas.drawLine(clipLeftMargin + clipWidth + lateralOffset, clipTopMargin - startOffset, clipLeftMargin + clipWidth + lateralOffset, clipTopMargin + mCornerLength, mCornerPaint);
        // Top-right corner: top side
        canvas.drawLine(clipLeftMargin + clipWidth + startOffset, clipTopMargin - lateralOffset, clipLeftMargin + clipWidth - mCornerLength, clipTopMargin - lateralOffset, mCornerPaint);

        // Bottom-left corner: left side
        canvas.drawLine(clipLeftMargin - lateralOffset, clipTopMargin + clipHeight + startOffset, clipLeftMargin - lateralOffset, clipTopMargin + clipHeight - mCornerLength, mCornerPaint);
        // Bottom-left corner: bottom side
        canvas.drawLine(clipLeftMargin - startOffset, clipTopMargin + clipHeight + lateralOffset, clipLeftMargin + mCornerLength, clipTopMargin + clipHeight + lateralOffset, mCornerPaint);

        // Bottom-right corner: right side
        canvas.drawLine(clipLeftMargin + clipWidth + lateralOffset, clipTopMargin + clipHeight + startOffset, clipLeftMargin + clipWidth + lateralOffset, clipTopMargin + clipHeight - mCornerLength, mCornerPaint);
        // Bottom-right corner: bottom side
        canvas.drawLine(clipLeftMargin + clipWidth + startOffset, clipTopMargin + clipHeight + lateralOffset, clipLeftMargin + clipWidth - mCornerLength, clipTopMargin + clipHeight + lateralOffset, mCornerPaint);

        if (listenerComplete != null) {
            listenerComplete.onDrawCompelete();
        }
    }

    public int getMaxImageSize() {
        System.out.println("getMaxImageSize "+mMaxImageSize);
        return mMaxImageSize;
    }

    public void setMaxImageSize(int maxImageSize) {
        mMaxImageSize = maxImageSize;
    }

    public boolean isClip() {
        return isClip;
    }

    public void setIsClip(boolean isClip) {
        this.isClip = isClip;
    }

    public int getCustomTopBarHeight() {
        return customTopBarHeight;
    }

    public void setCustomTopBarHeight(int customTopBarHeight) {
        this.customTopBarHeight = customTopBarHeight;
    }

    public double getClipRatio() {
        return clipRatio;
    }

    public void setClipRatio(double clipRatio) {
        this.clipRatio = clipRatio;
    }

    public int getClipWidth() {
        // 减clipBorderWidth原因：截图时去除边框白线
        return clipWidth - clipBorderWidth;
    }

    public void setClipWidth(int clipWidth) {
        this.clipWidth = clipWidth;
    }

    public int getClipHeight() {
        return clipHeight - clipBorderWidth;
    }

    public void setClipHeight(int clipHeight) {
        this.clipHeight = clipHeight;
    }

    public int getClipLeftMargin() {
        return clipLeftMargin + clipBorderWidth;
    }

    public void setClipLeftMargin(int clipLeftMargin) {
        this.clipLeftMargin = clipLeftMargin;
        isSetMargin = true;
    }

    public int getClipTopMargin() {
        return clipTopMargin + clipBorderWidth;
    }

    public void setClipTopMargin(int clipTopMargin) {
        this.clipTopMargin = clipTopMargin;
        isSetMargin = true;
    }

    public void addOnDrawCompleteListener(OnDrawListenerComplete listener) {
        this.listenerComplete = listener;
    }

    public void removeOnDrawCompleteListener() {
        this.listenerComplete = null;
    }

    /**
     * 裁剪区域画完时调用接口
     *
     * @author Cow
     */
    public interface OnDrawListenerComplete {
        public void onDrawCompelete();
    }

}
