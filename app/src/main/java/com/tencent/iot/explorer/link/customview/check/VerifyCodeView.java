package com.tencent.iot.explorer.link.customview.check;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;

import com.tencent.iot.explorer.link.R;

import java.security.Key;

/**
 * 自定义短信认证码控件
 */
public class VerifyCodeView extends ViewGroup {

    private EditText editText;
    private CenterTextView[] textViews;
    private int length = 4;
    private float textSize = 0;
    private int tvWidth, tvHeight;
    private boolean textBold = false;
    private @ColorInt
    int textColor;
    private @DrawableRes
    int selectedBackground, unselectedBackground;
    private OnTextLengthListener onTextLengthListener;

    public VerifyCodeView(Context context, int length) {
        super(context);
        this.length = length;
    }

    public VerifyCodeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public VerifyCodeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int measuredWidth = widthMode == MeasureSpec.EXACTLY ? widthSize : this.getResources().getDisplayMetrics().widthPixels;
        int measuredHeight = heightMode == MeasureSpec.EXACTLY ? heightSize : dp2px(40);
        setMeasuredDimension(measuredWidth, measuredHeight);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int childTop, childBottom, childLeft, childRight;
        int count = this.getChildCount();
        int distance = dp2px(7);
        tvWidth = (getWidth() - getPaddingStart() - getPaddingEnd() - (length - 1) * distance) / length;
        for (int i = 0; i < count; i++) {
            View view = this.getChildAt(i);
            if (i < count - 1) {
                childLeft = this.getPaddingStart() + distance * i + tvWidth * i;
                childRight = childLeft + tvWidth;
                if (this.getMeasuredHeight() > tvHeight) {
                    childTop = this.getMeasuredHeight() / 2 - tvHeight / 2;
                    childBottom = this.getMeasuredHeight() / 2 + tvHeight / 2;
                } else {
                    childTop = 0;
                    childBottom = tvHeight;
                }
            } else {
                childLeft = this.getPaddingLeft();
                childRight = this.getPaddingStart() + distance * (i + 1) + tvWidth * i;
                childTop = this.getPaddingTop();
                childBottom = this.getMeasuredHeight() - this.getPaddingBottom();
            }
            view.layout(childLeft, childTop, childRight, childBottom);
        }
    }

    /**
     * 初始化
     */
    private void init(Context context, AttributeSet attrs) {
        final TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.VerifyCodeView);
        final int count = typedArray.getIndexCount();
        for (int i = 0; i < count; i++) {
            int attr = typedArray.getIndex(i);
            if (attr == R.styleable.VerifyCodeView_length) {
                length = typedArray.getInteger(attr, 4);
            } else if (attr == R.styleable.VerifyCodeView_text_color) {
                textColor = typedArray.getColor(attr, Color.GRAY);
            } else if (attr == R.styleable.VerifyCodeView_text_size) {
                textSize = typedArray.getDimension(attr, 12);
            } else if (attr == R.styleable.VerifyCodeView_text_bold) {
                textBold = typedArray.getBoolean(attr, false);
            } else if (attr == R.styleable.VerifyCodeView_verify_height) {
                tvHeight = typedArray.getDimensionPixelOffset(attr, LayoutParams.MATCH_PARENT);
            } else if (attr == R.styleable.VerifyCodeView_verify_width) {
                tvWidth = typedArray.getDimensionPixelOffset(attr, LayoutParams.WRAP_CONTENT);
            } else if (attr == R.styleable.VerifyCodeView_selected_background) {
                selectedBackground = typedArray.getResourceId(attr, Color.GRAY);
            } else if (attr == R.styleable.VerifyCodeView_unselected_background) {
                unselectedBackground = typedArray.getResourceId(attr, Color.GRAY);
            }
        }
        typedArray.recycle();
        initialise();
    }

    public int getLength() {
        return length;
    }

    public void initialise() {
        textViews = new CenterTextView[length];
        for (int i = 0; i < textViews.length; i++) {
            CenterTextView textView = new CenterTextView(getContext());
            LayoutParams layoutParams = new LayoutParams(tvWidth,
                    tvHeight == 0 ? LayoutParams.MATCH_PARENT : tvHeight);
            textView.setLayoutParams(layoutParams);
            textView.setTextColor(textColor);
            textView.setTextSize(textSize == 0 ? 12 : textSize);
            textView.setTypeface(textBold ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
            textView.setBackgroundResource(selectedBackground);
            this.addView(textView);
            textViews[i] = textView;
        }
        editText = new EditText(getContext());
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, tvHeight);
        editText.setLayoutParams(params);
        editText.setBackground(null);
        editText.setCursorVisible(false);
        editText.addTextChangedListener(textWatcher);
        editText.setInputType(InputType.TYPE_CLASS_NUMBER);
        editText.setFadingEdgeLength(length);
        editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(length)});
        editText.setTextColor(Color.TRANSPARENT);
        this.addView(editText);
        setTextListener();
        this.invalidate();
    }

    public String getText() {
        return editText.getText().toString().trim();
    }

    public void setText(String text) {
        editText.setText(text);
    }

    /**
     * 设置键盘监听器
     */
    private void setTextListener() {
        editText.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_DEL) {
                    editText.setSelection(editText.getText().length());
                }
                return false;
            }
        });
    }

    public void setOnTextLengthListener(OnTextLengthListener onTextLengthListener) {
        this.onTextLengthListener = onTextLengthListener;
    }

    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            showText(editText.getText().toString());
        }
    };

    private synchronized void showText(String text) {
        for (int i = 0; i < textViews.length; i++) {
            if (i < text.length()) {
                textViews[i].setText(String.valueOf(text.charAt(i)));
            } else {
                textViews[i].setText("");
            }
        }
        if (onTextLengthListener != null) onTextLengthListener.onTextLengthListener(text);
    }


    public interface OnTextLengthListener {
        void onTextLengthListener(@NonNull String text);
    }

    private int dp2px(int dp) {
        if (getContext() == null) return 0;
        return (int) (getContext().getResources().getDisplayMetrics().density * dp + 0.5);
    }

    private float sp2px(int sp) {
        return this.getContext().getResources().getDisplayMetrics().scaledDensity * sp;
    }

    class CenterTextView extends View {
        private Paint paint;
        private String text = "";

        public CenterTextView(Context context) {
            super(context);
        }

        public CenterTextView(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public CenterTextView(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
        }

        public void setTextColor(@ColorInt int color) {
            getPaint().setColor(color);
            invalidate();
        }

        public void setTextSize(float sp) {
            getPaint().setTextSize(sp);
            invalidate();
        }

        public void setTypeface(Typeface typeface) {
            getPaint().setTypeface(typeface);
            invalidate();
        }

        public void setText(String text) {
            this.text = text;
            invalidate();
        }

        Paint getPaint() {
            if (paint == null) {
                paint = new Paint();
                paint.setTextSize(sp2px(20));
                paint.setColor(Color.GREEN);
                this.setBackgroundResource(R.drawable.verify_code_bg);
            }
            return paint;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            if (TextUtils.isEmpty(text)) return;
            float textWidth = getPaint().measureText(text);
            float x = this.getWidth() / 2 - textWidth / 2;
            float y = this.getWidth() / 2 + textWidth / 2;
            canvas.drawText(text, x, y, getPaint());
        }
    }
}
