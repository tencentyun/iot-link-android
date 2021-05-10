package com.tencent.iot.explorer.link.customview.verticaltab;


public interface TabAdapter {

    TabView.TabTitle getTitle(int position);

    int getBackground(int position);

    int getCount();
}
