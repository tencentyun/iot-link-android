package com.tencent.iot.explorer.link.customview;

import android.widget.GridView;

public class FullGridView extends GridView {

    public FullGridView(android.content.Context context, android.util.AttributeSet attrs) {
        super(context, attrs);
    }


    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);
    }
}

