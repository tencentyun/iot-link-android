package com.tencent.iot.video.link.recorder.opengles.render;

public interface Renderer {

    void onCreate();
    void onChange(int width, int height);
    void onDraw();
}
