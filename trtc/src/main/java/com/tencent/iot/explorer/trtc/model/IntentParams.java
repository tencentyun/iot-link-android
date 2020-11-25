package com.tencent.iot.explorer.trtc.model;

import com.tencent.iot.explorer.trtc.ui.audiocall.TRTCAudioCallActivity;

import java.io.Serializable;
import java.util.List;

public class IntentParams implements Serializable {
    public List<UserInfo> mUserInfos;

    public IntentParams(List<UserInfo> userInfos) {
        mUserInfos = userInfos;
    }
}
