package com.tencent.iot.explorer.link.customview;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.tencent.iot.explorer.link.kitlink.fragment.BaseFragment;
import com.tencent.iot.explorer.link.kitlink.fragment.MessageFragment;

import java.util.List;

public class PageAdapter extends FragmentPagerAdapter {
    List<BaseFragment> mPages;

    public PageAdapter(FragmentManager fm, List<BaseFragment> pages) {
        super(fm);
        mPages = pages;
    }

    @Override
    public BaseFragment getItem(int arg0) {
        try {
            return mPages.get(arg0);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public int getCount() {
        if (mPages == null) {
            return 0;
        }
        return mPages.size();
    }

}
