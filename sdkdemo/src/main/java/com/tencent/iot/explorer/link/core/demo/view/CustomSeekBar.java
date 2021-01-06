package com.tencent.iot.explorer.link.core.demo.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatSeekBar;

import com.tencent.iot.explorer.link.core.demo.R;

import java.util.ArrayList;

public class CustomSeekBar extends AppCompatSeekBar {
    private ArrayList<ProgressItem> mProgressItemsList;
    private int colorDefault = R.color.gray_e6e6e6;
    private int colorValid = R.color.green_1aad19;

    public CustomSeekBar(Context context) {
        super(context);
    }

    public CustomSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomSeekBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void initData(ArrayList<ProgressItem> progressItemsList) {
        this.mProgressItemsList = progressItemsList;
    }

    @Override
    protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    protected void onDraw(Canvas canvas) {
        int thumbWidth = getThumb().getBounds().height();
        int thumbheight = getThumb().getBounds().height();
        int progressBarWidth = getProgressDrawable().getBounds().width();
        Rect allProgressRect = new Rect();
        Paint allProgressPaint = new Paint();
        allProgressPaint.setColor(getResources().getColor(colorDefault));
        allProgressRect.set(thumbWidth, 0, progressBarWidth + thumbWidth, thumbheight);
        canvas.drawRect(allProgressRect, allProgressPaint);

        if (mProgressItemsList != null && mProgressItemsList.size() > 0) {
            int progressItemLeft, progressItemRight;
            for (int i = 0; i < mProgressItemsList.size(); i++) {
                ProgressItem progressItem = mProgressItemsList.get(i);
                Paint progressPaint = new Paint();
                progressPaint.setColor(getResources().getColor(colorValid));
                progressItemLeft = (int) (progressItem.progressItemPercentage * progressBarWidth / getMax()) + thumbWidth;
                progressItemRight = (int) (progressItem.progressItemPercentageEnd * progressBarWidth / getMax()) + thumbWidth;

                Rect progressRect = new Rect();
                progressRect.set(progressItemLeft, 0, progressItemRight, thumbheight);
                canvas.drawRect(progressRect, progressPaint);
            }
            super.onDraw(canvas);
        }

    }
}
