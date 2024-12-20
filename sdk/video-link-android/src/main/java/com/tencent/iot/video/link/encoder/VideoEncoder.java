package com.tencent.iot.video.link.encoder;

import android.annotation.TargetApi;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.tencent.iot.video.link.listener.OnEncodeListener;
import com.tencent.iot.video.link.param.VideoEncodeParam;

import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class VideoEncoder {

    private final String TAG = VideoEncoder.class.getSimpleName();
    private final VideoEncodeParam videoEncodeParam;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private MediaCodec mediaCodec;
    private MediaFormat mediaFormat;
    private OnEncodeListener encoderListener;
    private long seq = 0L;
    private int MAX_BITRATE_LENGTH = 1000000;
    private int beginBitRate = 0;

    private boolean isSupportNV21 = false;

    private String firstSupportColorFormatCodecName = "";  //  OMX.qcom.video.encoder.avc 和 c2.android.avc.encoder 过滤，这两个h264编码性能好一些。如果都不支持COLOR_FormatYUV420Planar，就用默认的方式。

    public VideoEncoder(VideoEncodeParam param) {
        this.videoEncodeParam = param;
        checkSupportedColorFormats();
        initMediaCodec();
    }

    private void initMediaCodec() {
        try {
            checkSupportedColorFormats("video/avc"); // H.264 编码器

            if (!firstSupportColorFormatCodecName.isEmpty()) {
                mediaCodec = MediaCodec.createByCodecName(firstSupportColorFormatCodecName);
            } else {
                mediaCodec = MediaCodec.createEncoderByType("video/avc");
            }
            //height和width一般都是照相机的height和width。
            //TODO 因为获取到的视频帧数据是逆时针旋转了90度的，所以这里宽高需要对调
            MediaFormat mediaFormat = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, videoEncodeParam.getHeight(), videoEncodeParam.getWidth());
            //描述平均位速率（以位/秒为单位）的键。 关联的值是一个整数
            int bitRate = videoEncodeParam.getBitRate();
            if (bitRate > MAX_BITRATE_LENGTH) {
                bitRate = MAX_BITRATE_LENGTH;
            }
            beginBitRate = bitRate;
            mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, bitRate);
            //描述视频格式的帧速率（以帧/秒为单位）的键。帧率，一般在15至30之内，太小容易造成视频卡顿。
            mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, videoEncodeParam.getFrameRate());
            if (isSupportNV21) {
                //色彩格式，具体查看相关API，不同设备支持的色彩格式不尽相同
                mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar);
            } else {
                //色彩格式，具体查看相关API，不同设备支持的色彩格式不尽相同
                mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar);
            }
            //关键帧间隔时间，单位是秒
            mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, videoEncodeParam.getiFrameInterval());
            mediaFormat.setInteger(MediaFormat.KEY_BITRATE_MODE, MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_VBR);
            //设置压缩等级  默认是 baseline
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mediaFormat.setInteger(MediaFormat.KEY_LEVEL, MediaCodecInfo.CodecProfileLevel.AVCLevel3);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mediaFormat.setInteger(MediaFormat.KEY_PROFILE, MediaCodecInfo.CodecProfileLevel.AVCProfileMain);
            }
            mediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            //开始编码
            mediaCodec.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void checkSupportedColorFormats(String mimeType) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            MediaCodecList codecList = null;
            codecList = new MediaCodecList(MediaCodecList.ALL_CODECS);
            MediaCodecInfo[] codecInfos = codecList.getCodecInfos();

            for (MediaCodecInfo codecInfo : codecInfos) {
                if (!codecInfo.isEncoder()) {
                    continue;
                }

                String[] supportedTypes = codecInfo.getSupportedTypes();
                for (String type : supportedTypes) {
                    if (type.equalsIgnoreCase(mimeType)) {
                        MediaCodecInfo.CodecCapabilities capabilities = codecInfo.getCapabilitiesForType(type);
                        int[] colorFormats = capabilities.colorFormats;

                        for (int colorFormat : colorFormats) {
                            switch (colorFormat) {
                                case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar:
                                    Log.e(TAG, "Supported color format: COLOR_FormatYUV420Planar");
                                    isSupportNV21 = false;
                                    return;
                                case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar:
                                    Log.e(TAG, "Supported color format: COLOR_FormatYUV420SemiPlanar");
                                    isSupportNV21 = true;
                                    return;
                                default:
                                    Log.e(TAG, "Supported color format: " + colorFormat);
                                    break;
                            }
                        }
                    }
                }
            }
        }
    }

    //描述平均位速率（以位/秒为单位）的键。 关联的值是一个整数
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void setVideoBitRate(int bitRate) {
        int nowBitrate = videoEncodeParam.getBitRate();

        if ((bitRate > beginBitRate) || (bitRate < 10000) || (nowBitrate == bitRate) || (bitRate > MAX_BITRATE_LENGTH)) {
            return;
        }

        videoEncodeParam.setBitRate(bitRate);

        try {
            Bundle params = new Bundle();
            params.putInt(MediaCodec.PARAMETER_KEY_VIDEO_BITRATE, bitRate);
            mediaCodec.setParameters(params);

        } catch (IllegalStateException e) {
            Log.e(TAG, "updateBitrate failed", e);
        }
    }

    public int getVideoBitRate() {
        return videoEncodeParam.getBitRate();
    }
    /**
     * 将NV21编码成H264
     */
    public void encoderH264(byte[] data, boolean mirror) {
        if (executor.isShutdown()) return;
        executor.submit(() -> {
            byte[] readyToProcessBytes;
            if (isSupportNV21) {
                //将NV21编码成NV12
                byte[] bytes = NV21ToNV12(data, videoEncodeParam.getWidth(), videoEncodeParam.getHeight());
                //视频顺时针旋转90度
                byte[] nv12 = rotateNV290(bytes, videoEncodeParam.getWidth(), videoEncodeParam.getHeight());

                if (mirror) {
                    verticalMirror(nv12, videoEncodeParam.getHeight(), videoEncodeParam.getWidth());
                }
                readyToProcessBytes = nv12;
            } else {
                byte[] rotateBytes;
                //视频顺时针旋转90度
                if (mirror) {
                    rotateBytes = nv21Rotate270(data, videoEncodeParam.getWidth(), videoEncodeParam.getHeight());
                } else {
                    rotateBytes = nv21Rotate90(data, videoEncodeParam.getWidth(), videoEncodeParam.getHeight());
                }
                //将NV21编码成I420
                byte[] i420 = toI420(rotateBytes, videoEncodeParam.getHeight(), videoEncodeParam.getWidth());
                readyToProcessBytes = i420;
            }


            try {
                //拿到输入缓冲区,用于传送数据进行编码
                ByteBuffer[] inputBuffers = mediaCodec.getInputBuffers();
                //拿到输出缓冲区,用于取到编码后的数据
                ByteBuffer[] outputBuffers = mediaCodec.getOutputBuffers();
                int inputBufferIndex = mediaCodec.dequeueInputBuffer(-1);
                //当输入缓冲区有效时,就是>=0
                if (inputBufferIndex >= 0) {
                    ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
                    inputBuffer.clear();
                    //往输入缓冲区写入数据
                    inputBuffer.put(readyToProcessBytes);
                    //五个参数，第一个是输入缓冲区的索引，第二个数据是输入缓冲区起始索引，第三个是放入的数据大小，第四个是时间戳，保证递增就是
                    mediaCodec.queueInputBuffer(inputBufferIndex, 0, readyToProcessBytes.length, System.nanoTime() / 1000, 0);
                }
                MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
                //拿到输出缓冲区的索引
                int outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 0);
                while (outputBufferIndex >= 0) {
                    ByteBuffer outputBuffer = outputBuffers[outputBufferIndex];

                    outputBuffer.position(bufferInfo.offset);
                    outputBuffer.limit(bufferInfo.offset + bufferInfo.size);

                    byte[] outData = new byte[outputBuffer.remaining()];
                    outputBuffer.get(outData, 0, outData.length);

                    if (bufferInfo.flags == MediaCodec.BUFFER_FLAG_KEY_FRAME) {
                        // I 帧的处理逻辑
//                        Log.e(TAG, "==========I帧==============="+seq);
                        ByteBuffer spsb = mediaCodec.getOutputFormat().getByteBuffer("csd-0");
                        byte[] sps = new byte[spsb.remaining()];
                        spsb.get(sps, 0, sps.length);
                        ByteBuffer ppsb = mediaCodec.getOutputFormat().getByteBuffer("csd-1");
                        byte[] pps = new byte[ppsb.remaining()];
                        ppsb.get(pps, 0, pps.length);

                        byte[] dataBytes = new byte[sps.length + pps.length + outData.length];
                        System.arraycopy(sps, 0, dataBytes, 0, sps.length);
                        System.arraycopy(pps, 0, dataBytes, sps.length, pps.length);
                        System.arraycopy(outData, 0, dataBytes, pps.length + sps.length, outData.length);
                        if (encoderListener != null) {
                            encoderListener.onVideoEncoded(dataBytes, System.currentTimeMillis(), seq);
                            seq++;
                        }
                    } else {
                        //outData就是输出的h264数据
//                        Log.e(TAG, "==========P帧===============" + seq);
                        if (encoderListener != null) {
                            encoderListener.onVideoEncoded(outData, System.currentTimeMillis(), seq);
                            seq++;
                        }
                    }

                    mediaCodec.releaseOutputBuffer(outputBufferIndex, false);
                    outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 0);
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        });
    }

    /**
     * 因为从MediaCodec不支持NV21的数据编码，所以需要先讲NV21的数据转码为NV12
     */
    private byte[] NV21ToNV12(byte[] nv21, int width, int height) {
        byte[] nv12 = new byte[width * height * 3 / 2];
        int frameSize = width * height;
        int i, j;
        System.arraycopy(nv21, 0, nv12, 0, frameSize);
        for (i = 0; i < frameSize; i++) {
            nv12[i] = nv21[i];
        }
        for (j = 0; j < frameSize / 2; j += 2) {
            nv12[frameSize + j - 1] = nv21[j + frameSize];
        }
        for (j = 0; j < frameSize / 2; j += 2) {
            nv12[frameSize + j] = nv21[j + frameSize - 1];
        }
        return nv12;
    }

    public byte[] toI420(byte[] input, int width, int height) {
        byte[] preAllocatedBufferColor = new byte[width * height * 3 / 2];
        final int frameSize = width * height;
        final int qFrameSize = frameSize / 4;
        System.arraycopy(input, 0, preAllocatedBufferColor, 0, frameSize); // Y
        for (int i = 0; i < qFrameSize; i++) {
            preAllocatedBufferColor[frameSize + i] = input[frameSize + i * 2 + 1]; // Cb (U)
            preAllocatedBufferColor[frameSize + i + qFrameSize] = input[frameSize + i * 2]; // Cr (V)
        }
        return preAllocatedBufferColor;
    }

    public byte[] nv21Rotate90(byte[] data, int imageWidth, int imageHeight) {
        byte[] preAllocatedBufferRotate = new byte[imageWidth * imageHeight * 3 / 2];
        // Rotate the Y luma
        int i = 0;
        for (int x = 0; x < imageWidth; x++) {
            for (int y = imageHeight - 1; y >= 0; y--) {
                preAllocatedBufferRotate[i++] = data[y * imageWidth + x];
            }
        }
        // Rotate the U and V color components
        int size = imageWidth * imageHeight;
        i = size * 3 / 2 - 1;
        for (int x = imageWidth - 1; x > 0; x = x - 2) {
            for (int y = 0; y < imageHeight / 2; y++) {
                preAllocatedBufferRotate[i--] = data[size + (y * imageWidth) + x];
                preAllocatedBufferRotate[i--] = data[size + (y * imageWidth) + (x - 1)];
            }
        }
        return preAllocatedBufferRotate;
    }

    public byte[] nv21Rotate270(byte[] data, int imageWidth, int imageHeight) {
        byte[] preAllocatedBufferRotate = new byte[imageWidth * imageHeight * 3 / 2];
        // Rotate the Y luma
        int i = 0;
        for (int x = imageWidth - 1; x >= 0; x--) {
            for (int y = 0; y < imageHeight; y++) {
                preAllocatedBufferRotate[i++] = data[y * imageWidth + x];
            }
        }

        // Rotate the U and V color components
        i = imageWidth * imageHeight;
        int uvHeight = imageHeight / 2;
        for (int x = imageWidth - 1; x >= 0; x -= 2) {
            for (int y = imageHeight; y < uvHeight + imageHeight; y++) {
                preAllocatedBufferRotate[i++] = data[y * imageWidth + x - 1];
                preAllocatedBufferRotate[i++] = data[y * imageWidth + x];
            }
        }
        return preAllocatedBufferRotate;
    }

    /**
     * 此处为顺时针旋转旋转90度
     *
     * @param data        旋转前的数据
     * @param imageWidth  旋转前数据的宽
     * @param imageHeight 旋转前数据的高
     * @return 旋转后的数据
     */
    private byte[] rotateNV290(byte[] data, int imageWidth, int imageHeight) {
        byte[] yuv = new byte[imageWidth * imageHeight * 3 / 2];
        // Rotate the Y luma
        int i = 0;
        for (int x = 0; x < imageWidth; x++) {
            for (int y = imageHeight - 1; y >= 0; y--) {
                yuv[i] = data[y * imageWidth + x];
                i++;
            }
        }
        // Rotate the U and V color components
        i = imageWidth * imageHeight * 3 / 2 - 1;
        for (int x = imageWidth - 1; x > 0; x = x - 2) {
            for (int y = 0; y < imageHeight / 2; y++) {
                yuv[i] = data[(imageWidth * imageHeight) + (y * imageWidth) + x];
                i--;
                yuv[i] = data[(imageWidth * imageHeight) + (y * imageWidth) + (x - 1)];
                i--;
            }
        }
        return yuv;
    }

    private void verticalMirror(byte[] src, int w, int h) { //src是原始yuv数组
        int i;
        int index;
        byte temp;
        int a, b;
        //mirror y
        for (i = 0; i < w; i++) {
            a = i;
            b = (h - 1) * w + i;
            while (a < b) {
                temp = src[a];
                src[a] = src[b];
                src[b] = temp;
                a += w;
                b -= w;
            }
        }

        // mirror u and v
        index = w * h;
        for (i = 0; i < w; i++) {
            a = i;
            b = (h / 2 - 1) * w + i;
            while (a < b) {
                temp = src[a + index];
                src[a + index] = src[b + index];
                src[b + index] = temp;
                a += w;
                b -= w;
            }
        }
    }

    /**
     * 设置编码成功后数据回调
     */
    public void setEncoderListener(OnEncodeListener listener) {
        encoderListener = listener;
    }

    public void stop() {
        executor.shutdown();
    }

    private void checkSupportedColorFormats() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            MediaCodecList codecList = new MediaCodecList(MediaCodecList.ALL_CODECS);
            MediaCodecInfo[] codecInfos = codecList.getCodecInfos();

            for (MediaCodecInfo codecInfo : codecInfos) {
                if (!codecInfo.isEncoder()) {
                    continue;
                }

                if (codecInfo.getName().equals("OMX.qcom.video.encoder.avc") || codecInfo.getName().equals("c2.android.avc.encoder")) {

                    String[] supportedTypes = codecInfo.getSupportedTypes();
                    for (String type : supportedTypes) {
                        if (type.startsWith("video/")) {
                            MediaCodecInfo.CodecCapabilities capabilities = codecInfo.getCapabilitiesForType(type);
                            int[] colorFormats = capabilities.colorFormats;
                            for (int colorFormat : colorFormats) {

                                if (colorFormat == MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar) {
                                    Log.d(TAG, "Video encoder: " + codecInfo.getName() + ", supported color format: " + colorFormat);
                                    firstSupportColorFormatCodecName = codecInfo.getName();
                                    return;
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
