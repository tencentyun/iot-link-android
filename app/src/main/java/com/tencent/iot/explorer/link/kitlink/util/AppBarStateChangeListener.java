package com.tencent.iot.explorer.link.kitlink.util;

import com.google.android.material.appbar.AppBarLayout;

public abstract class AppBarStateChangeListener implements AppBarLayout.OnOffsetChangedListener {
    public enum State {EXPANDED, COLLAPSED, IDLE}

    private State mCurrentState = State.IDLE;

    @Override
    public final void onOffsetChanged(AppBarLayout appBarLayout, int i) {
        if (i == 0) {
            if (mCurrentState != State.EXPANDED) {
                onStateChanged(appBarLayout, State.EXPANDED, 1f);
            }
            mCurrentState = State.EXPANDED;

        } else if (Math.abs(i) >= appBarLayout.getTotalScrollRange()) {
            if (mCurrentState != State.COLLAPSED) {
                onStateChanged(appBarLayout, State.COLLAPSED, 0f);
            }
            mCurrentState = State.COLLAPSED;

        } else {
            mCurrentState = State.IDLE;
            int total = appBarLayout.getTotalScrollRange();
            float percent = (total - (float) Math.abs(i)) / total;
            onStateChanged(appBarLayout, State.IDLE, percent);
        }
    }

    public abstract void onStateChanged(AppBarLayout appBarLayout, State state, float percent);
}
