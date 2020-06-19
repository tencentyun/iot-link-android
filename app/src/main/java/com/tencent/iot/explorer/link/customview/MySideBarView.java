package com.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;


public class MySideBarView extends View {
    private String str[] = {"#", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};
    private int srcChoose = -1;
    private boolean showBkg = false;
    private Paint paint = new Paint();
    private OnTouchingLetterChangedListener onTouchingLetterChangedListener;
    private TextView tv;
    //private Animation anim;
    private Context context;

    public MySideBarView(Context context) {
        super(context);
    }

    public MySideBarView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MySideBarView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setContext(Context c) {
        this.context = c;

    }

//	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//		setMeasuredDimension(width, height);
//	}

    protected void onDraw(Canvas c) {   //最后调用onDraw();
        super.onDraw(c);
        if (showBkg) {
            //c.drawColor(Color.RED);
        }

        int height = super.getHeight() - super.getHeight() / 96;
        int width = super.getWidth();       //宽度为90pixels，在main.xml中设置的width=30dip;

        System.out.println("Height=" + getHeight() + ",Width=" + getWidth());

        int singleHeight = height / str.length;

        for (int i = 0; i < str.length; i++) {
            paint.setColor(Color.BLACK);
            paint.setAntiAlias(true);
            paint.setTextSize(super.getHeight() / 42);

            if (i == srcChoose) {
                paint.setColor(Color.RED);
                paint.setFakeBoldText(true);
            }
            float xPos = (width - paint.measureText(str[i])) / 2;
            float yPos = singleHeight * i + singleHeight;

            c.drawText(str[i], xPos, yPos, paint);
            paint.reset();
        }
    }

    //============================================================================================================
    public boolean dispatchTouchEvent(MotionEvent e) {
        float y = e.getY();

        int oldChoose = srcChoose;  //srcChoose==-1;

        int currentPosition = (int) (y / getHeight() * str.length);
        System.out.println("CurrentPosition=" + currentPosition);

        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                this.showBkg = true;
                if (oldChoose != currentPosition && this.onTouchingLetterChangedListener != null) {
                    if (currentPosition >= 0 && currentPosition < str.length) {
                        this.onTouchingLetterChangedListener.onTouchingLetterChanged(str[currentPosition], currentPosition);
                        srcChoose = currentPosition;
                        super.invalidate();
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (oldChoose != currentPosition && this.onTouchingLetterChangedListener != null) {
                    if (currentPosition >= 0 && currentPosition < str.length) {
                        this.onTouchingLetterChangedListener.onTouchingLetterChanged(str[currentPosition], currentPosition);
                        srcChoose = currentPosition;
                        super.invalidate();
                    }
                }
                break;

            case MotionEvent.ACTION_UP:
                showBkg = false;
                srcChoose = -1;
                super.invalidate();
                tv.setVisibility(View.GONE);
                break;

        }
        return true;
    }

    //============================================================================================================
    public boolean onTouchEvent(MotionEvent e) {
        System.out.println("onTouchEvent()");
        return super.onTouchEvent(e);
    }

    //============================================================================================================
    public interface OnTouchingLetterChangedListener {
        public void onTouchingLetterChanged(String string, int position);
    }

    public void setOnTouchingLetterChangedListener(OnTouchingLetterChangedListener TCL) {
        this.onTouchingLetterChangedListener = TCL;
    }

    //==============================================================================================================
    public void setTextView(TextView tv) {
        this.tv = tv;
    }

    public String[] getAtoZAlpha() {
        return str;
    }

}
