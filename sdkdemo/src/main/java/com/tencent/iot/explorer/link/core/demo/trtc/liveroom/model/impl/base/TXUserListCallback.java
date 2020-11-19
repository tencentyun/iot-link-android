package com.tencent.iot.explorer.link.core.demo.trtc.liveroom.model.impl.base;

import java.util.List;

public interface TXUserListCallback {
    void onCallback(int code, String msg, List<TXUserInfo> list);
}
