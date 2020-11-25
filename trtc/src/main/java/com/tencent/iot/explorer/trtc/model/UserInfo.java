package com.tencent.iot.explorer.trtc.model;

import com.tencent.iot.explorer.trtc.ui.utils.Utils;

import java.io.Serializable;

public class UserInfo implements Serializable {
    private String userId;
    public String userAvatar;
    public String userName;

    public void setUserId(String userId) {
        this.userId = userId;
        userAvatar = Utils.getAvatarUrl(this.userId);
        userName = this.userId;
    }

    public String getUserId() {
        return userId;
    }
}
