package com.tencent.iot.video.link.rtc;

import java.io.Serializable;
import java.util.List;

public class IntentParams implements Serializable {
    public List<UserInfo> mUserInfos;

    public IntentParams(List<UserInfo> userInfos) {
        mUserInfos = userInfos;
    }
}
