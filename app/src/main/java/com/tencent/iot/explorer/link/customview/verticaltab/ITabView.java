package com.tencent.iot.explorer.link.customview.verticaltab;

import android.view.View;


public interface ITabView {

    ITabView setTitle(TabTitle title);
    ITabView setBackground(int resid);
    TabTitle getTitle();
    View getTabView();

    class TabTitle {
        private Builder mBuilder;

        private TabTitle(Builder mBuilder) {
            this.mBuilder = mBuilder;
        }

        public int getColorSelected() {
            return mBuilder.mColorSelected;
        }

        public int getColorNormal() {
            return mBuilder.mColorNormal;
        }

        public int getTitleTextSize() {
            return mBuilder.mTitleTextSize;
        }

        public String getContent() {
            return mBuilder.mContent;
        }

        public static class Builder {
            private int mColorSelected;
            private int mColorNormal;
            private int mTitleTextSize;
            private String mContent;

            public Builder() {
                this.mColorSelected = 0xFFFF4081;
                this.mColorNormal = 0xFF757575;
                this.mTitleTextSize = 16;
                this.mContent = "";
            }

            public Builder setTextColor(int colorSelected, int colorNormal) {
                mColorSelected = colorSelected;
                mColorNormal = colorNormal;
                return this;
            }

            public Builder setTextSize(int sizeSp) {
                mTitleTextSize = sizeSp;
                return this;
            }

            public Builder setContent(String content) {
                mContent = content;
                return this;
            }

            public TabTitle build() {
                return new TabTitle(this);
            }
        }
    }
}
