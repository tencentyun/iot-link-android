package com.tencent.iot.explorer.link.demo.common.customView;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import com.tencent.iot.explorer.link.demo.core.fragment.BaseFragment;

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
