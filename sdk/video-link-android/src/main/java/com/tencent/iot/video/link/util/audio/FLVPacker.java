package com.tencent.iot.video.link.util.audio;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class FLVPacker {

    private long pts = 0;
    private long sudioPts = 0;
    private boolean isHead = false;

    public synchronized byte[] getFLV(byte[] data) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            if (!isHead) {
                baos.write(flvHeader());
            }
            baos.write(aacToFlv(data));
            baos.flush();
            baos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return baos.toByteArray();
    }

    public synchronized byte[] aacToFlv(byte[] date) {
        byte[] data = Arrays.copyOfRange(date, 7, date.length);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            baos.write(0x08);
            // 长度
            baos.write(integerTo3Bytes(data.length + 2));
            if (sudioPts == 0) {
                // 时间戳
                baos.write(0x00);
                baos.write(0x00);
                baos.write(0x00);
                baos.write(0x00);
                sudioPts = System.currentTimeMillis();
            } else {
                byte[] b = integerTo4Bytes((int) (System.currentTimeMillis() - pts));
                baos.write(b[1]);
                baos.write(b[2]);
                baos.write(b[3]);
                baos.write(b[0]);
            }
            // StreamID
            baos.write(0x00);
            baos.write(0x00);
            baos.write(0x00);
            baos.write(0xAF);
            if (data.length < 10) {
                baos.write(0x00);
            } else {
                baos.write(0x01);
            }
            baos.write(data);

            int len = data.length + 13;
            byte[] bbDS = integerTo4Bytes(len);
            baos.write(bbDS[0]);
            baos.write(bbDS[1]);
            baos.write(bbDS[2]);
            baos.write(bbDS[3]);
            baos.flush();
            baos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return baos.toByteArray();
    }

    private byte[] flvHeader() {
        isHead = true;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            baos.write(0x46);
            baos.write(0x4C);
            baos.write(0x56);
            baos.write(0x01);
            baos.write(0x04); // 用来控制是否含有音频 音视频都有为0x05 纯视频是0x01 纯音频为 0x04

            baos.write(0x00);
            baos.write(0x00);
            baos.write(0x00);
            baos.write(0x09);

            baos.write(0x00);
            baos.write(0x00);
            baos.write(0x00);
            baos.write(0x00);
            baos.flush();
            baos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return baos.toByteArray();
    }

    public static byte[] integerTo3Bytes(int value) {
        byte[] result = new byte[3];
        result[0] = (byte) ((value >>> 16) & 0xFF);
        result[1] = (byte) ((value >>> 8) & 0xFF);
        result[2] = (byte) (value & 0xFF);
        return result;
    }

    public static byte[] integerTo4Bytes(int value) {
        byte[] result = new byte[4];
        result[0] = (byte) ((value >>> 24) & 0xFF);
        result[1] = (byte) ((value >>> 16) & 0xFF);
        result[2] = (byte) ((value >>> 8) & 0xFF);
        result[3] = (byte) (value & 0xFF);
        return result;
    }

}

