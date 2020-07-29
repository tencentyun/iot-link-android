package com.tencent.iot.explorer.link.customview.progress;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tencent.iot.explorer.link.R;
import com.tencent.iot.explorer.link.customview.progress.bean.StepBean;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HorizontalStepView extends LinearLayout implements HorizontalStepsViewIndicator.OnDrawIndicatorListener {

    private RelativeLayout mTextContainer;
    private HorizontalStepsViewIndicator mStepsViewIndicator;
    private List<StepBean> mStepBeanList;
    private Map<Integer, TextView> showTxts = new HashMap<>();
    private int mComplectingPosition = 0;
    private int mUnComplectedTextColor = getResources().getColor(R.color.uncomplete_progress);//定义默认未完成文字的颜色;
    private int mComplectedTextColor = getResources().getColor(R.color.complete_progress);//定义默认完成文字的颜色;
    private int mTextSize = 14; //默认文字尺寸

    public int getCurrentStep() {
        if (mStepsViewIndicator == null) {
            return -1;
        }
        return mStepsViewIndicator.getCurrentStep();
    }

    public void setCurrentStep(int currentStep) {
        if (mStepsViewIndicator != null) {
            mStepsViewIndicator.setCurrentStep(currentStep);
        }
    }

    public HorizontalStepView(Context context) {
        this(context, null);
    }

    public HorizontalStepView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HorizontalStepView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        View rootView = LayoutInflater.from(getContext()).inflate(R.layout.widget_horizontal_stepsview, this);
        mStepsViewIndicator = rootView.findViewById(R.id.steps_indicator);
        mStepsViewIndicator.setOnDrawListener(this);
        mTextContainer = rootView.findViewById(R.id.rl_text_container);
    }

    // 设置显示的文字
    public void setStepViewTexts(List<StepBean> stepsBeanList) {
        mStepBeanList = stepsBeanList;
        mStepsViewIndicator.setStepNum(mStepBeanList);
    }

    public void refreshStepViewState() {
        mStepsViewIndicator.invalidate();
    }

    // 设置未完成文字的颜色
    public void setStepViewUnComplectedTextColor(int unComplectedTextColor) {
        mUnComplectedTextColor = unComplectedTextColor;
    }

    // 设置完成文字的颜色
    public void setStepViewComplectedTextColor(int complectedTextColor) {
        this.mComplectedTextColor = complectedTextColor;
    }

    // 设置StepsViewIndicator未完成线的颜色
    public void setStepsViewIndicatorUnCompletedLineColor(int unCompletedLineColor) {
        mStepsViewIndicator.setUnCompletedLineColor(unCompletedLineColor);
    }

    // 设置StepsViewIndicator完成线的颜色
    public void setStepsViewIndicatorCompletedLineColor(int completedLineColor) {
        mStepsViewIndicator.setCompletedLineColor(completedLineColor);
    }

    // 设置StepsViewIndicator默认图片
    public void setStepsViewIndicatorDefaultIcon(Drawable defaultIcon) {
        mStepsViewIndicator.setDefaultIcon(defaultIcon);
    }

    // 设置StepsViewIndicator已完成图片
    public void setStepsViewIndicatorCompleteIcon(Drawable completeIcon) {
        mStepsViewIndicator.setCompleteIcon(completeIcon);
    }

    public void setTextSize(int textSize) {
        if (textSize > 0) mTextSize = textSize;
    }

    @Override
    public void ondrawIndicator() {
        if (mTextContainer != null) {
            List<Float> complectedXPosition = mStepsViewIndicator.getCircleCenterPointPositionList();

            if (mStepBeanList != null && complectedXPosition != null && complectedXPosition.size() > 0) {
                for(int i = 0; i < mStepBeanList.size(); i++) {
                    TextView text = showTxts.get(i);
                    if (text == null) {
                        text = new TextView(getContext());
                    } else {
                        mTextContainer.removeView(text);
                    }

                    // 设置尺寸，显示位置
                    text.setTextSize(TypedValue.COMPLEX_UNIT_SP, mTextSize);
                    text.setText(mStepBeanList.get(i).getName());
                    int spec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
                    text.measure(spec, spec);
                    text.setPadding(0,10,0,0);
                    int measuredWidth = text.getMeasuredWidth();
                    text.setX(complectedXPosition.get(i) - measuredWidth / 2);
                    text.setLayoutParams(new ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                    // 设置字体颜色
                    if (i < getCurrentStep()) {
                        text.setTypeface(null, Typeface.BOLD);
                        text.setTextColor(mComplectedTextColor);
                    } else {
                        text.setTextColor(mUnComplectedTextColor);
                    }

                    showTxts.put(i, text);
                    mTextContainer.addView(text);
                }
            }

            mStepsViewIndicator.refreshDrawableState();
        }
    }
}
