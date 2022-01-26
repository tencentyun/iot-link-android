package com.tencent.iot.video.link.recorder.core.camera;

import com.tencent.iot.video.link.recorder.data.base.Size;

public interface OnCameraListener {

    void onCameraOpened(Size cameraSize, int facing); // 相机打开
    void onCameraClosed(); // 相机关闭
    void onCameraError(Exception e); // 相机异常
}
