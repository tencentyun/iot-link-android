package com.tencent.iot.explorer.link.customview.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;

import com.tencent.iot.explorer.link.R;

public class IosCenterStyleDialog extends Dialog {

    protected DisplayMetrics displayMetrics;
    protected View view;
    private Context mContext;
    private int layoutId;

    public IosCenterStyleDialog(Context context, int layoutId) {
        super(context, R.style.iOSDialog);
        mContext = context;
        displayMetrics = context.getResources().getDisplayMetrics();
        this.layoutId = layoutId;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //设置view 弹出的平移动画，从底部-100% 平移到自身位置
        TranslateAnimation animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF,
                0f, Animation.RELATIVE_TO_SELF, 1f, Animation.RELATIVE_TO_SELF, 0);
        animation.setInterpolator(new DecelerateInterpolator());
        animation.setDuration(350);
        animation.setStartOffset(150);

        view = View.inflate(mContext, this.layoutId, null);
        view.setAnimation(animation);//设置动画
        initView();
    }

    public void initView() {

    }

    @Override
    public void show() {
        super.show();
        // 设置dialog的宽高是全屏，注意：一定要放在show的后面，否则不是全屏显示
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.width = displayMetrics.widthPixels;
        params.height = ViewGroup.LayoutParams.MATCH_PARENT;
        params.gravity = Gravity.CENTER;
        getWindow().setAttributes(params);
        getWindow().setContentView(view);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                        //布局位于状态栏下方
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                        //全屏
                        View.SYSTEM_UI_FLAG_FULLSCREEN |
                        //隐藏导航栏
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
                uiOptions |= 0x00001000;
                getWindow().getDecorView().setSystemUiVisibility(uiOptions);
            }
        });
    }

}
