package com.tencent.iot.video.link.util.audio;

public interface EncoderListener {
    void encodeAAC(byte[] data, long time);
    void encodeG711(byte[] data);
}
