package com.tencent.iot.video.link.util.audio;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import com.tencent.iot.thirdparty.flv.FLVListener;
import com.tencent.iot.thirdparty.flv.FLVPacker;
import com.tencent.xnet.XP2P;


public class AudioRecordUtil implements EncoderListener, FLVListener {
    private static final int DEFAULT_CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_STEREO; //设置音频的录制的声道CHANNEL_IN_STEREO为双声道，CHANNEL_CONFIGURATION_MONO为单声道
    private static final int DEFAULT_AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT; //音频数据格式:PCM 16位每个样本。保证设备支持。PCM 8位每个样本。不一定能得到设备支持。
    private volatile boolean recorderState = true; //录制状态
    private byte[] buffer;
    private AudioRecord audioRecord;
    private volatile PCMEncoder pcmEncoder;
    private volatile FLVPacker flvPacker;
    private Context context;
    private String deviceId; //"productId/deviceName"
    private int recordMinBufferSize;
    private int sampleRate; //音频采样率
    private int channel;
    private int bitDepth;
    private int channelCount; //声道数

    public AudioRecordUtil(Context ctx, String id, int sampleRate) {
        context = ctx;
        deviceId = id;
        init(sampleRate, DEFAULT_CHANNEL_CONFIG, DEFAULT_AUDIO_FORMAT);
    }
    public AudioRecordUtil(Context ctx, String id, int sampleRate, int channel, int bitDepth) {
        context = ctx;
        deviceId = id;
        init(sampleRate, channel, bitDepth);
    }

    private void init(int sampleRate, int channel, int bitDepth) {
        recordMinBufferSize = AudioRecord.getMinBufferSize(sampleRate, channel, bitDepth);
        this.sampleRate = sampleRate;
        this.channel = channel;
        this.bitDepth = bitDepth;
        if (channel == AudioFormat.CHANNEL_IN_MONO) {
            this.channelCount = 1;
        } else if (channel == AudioFormat.CHANNEL_IN_STEREO) {
            this.channelCount = 2;
        }
    }

    /**
     * 开始录制
     */
    public void start() {
        reset();
        recorderState = true;
        audioRecord.startRecording();
        new RecordThread().start();
    }

    private void reset() {
        buffer = new byte[recordMinBufferSize];
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, channel, bitDepth, recordMinBufferSize);
        pcmEncoder = new PCMEncoder(sampleRate, channelCount, this, PCMEncoder.AAC_FORMAT);
        flvPacker = new FLVPacker(this, true, false);
    }

    /**
     * 停止录制
     */
    public void stop() {
        recorderState = false;
        if (audioRecord != null) {
            audioRecord.stop();
        }
        audioRecord = null;
        pcmEncoder = null;
        flvPacker.release();
        flvPacker = null;
    }

    public void release() {
        audioRecord.release();
    }

    @Override
    public void encodeAAC(byte[] data, long time) {
        flvPacker.encodeFlv(data, FLVPacker.TYPE_AUDIO, System.currentTimeMillis());
    }

    @Override
    public void encodeG711(byte[] data) { }

    @Override
    public void onFLV(byte[] data) {
        XP2P.dataSend(deviceId, data, data.length);
    }

    private class RecordThread extends Thread {
        @Override
        public void run() {
            while (recorderState) {
                int read = audioRecord.read(buffer, 0, buffer.length);
                if (AudioRecord.ERROR_INVALID_OPERATION != read) {
                    //获取到的pcm数据就是buffer了
                    pcmEncoder.encodeData(buffer);
                }
            }
        }
    }
}
