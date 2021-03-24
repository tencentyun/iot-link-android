package com.tencent.iot.video.link.util.audio;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import com.tencent.xnet.XP2P;


public class AudioRecordUtil implements EncoderListener {

    //设置音频采样率，44100是目前的标准，但是某些设备仍然支持22050，16000，11025
    private int sampleRateInHz = 44100;
    //设置音频的录制的声道CHANNEL_IN_STEREO为双声道，CHANNEL_CONFIGURATION_MONO为单声道
    private int channelConfig = AudioFormat.CHANNEL_IN_STEREO;
    //音频数据格式:PCM 16位每个样本。保证设备支持。PCM 8位每个样本。不一定能得到设备支持。
    private int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    //录制状态
    private boolean recorderState = true;
    private byte[] buffer;
    private AudioRecord audioRecord;
    private PCMEncoder pcmEncoder;
    private FLVPacker flvPacker;
    private Context context;

    public AudioRecordUtil(Context ctx) {
        context = ctx;
        init(sampleRateInHz, channelConfig, audioFormat);
    }
    public AudioRecordUtil(Context ctx, int sampleRate, int channel, int bitDepth) {
        context = ctx;
        init(sampleRate, channel, bitDepth);
    }

    private void init(int sampleRate, int channel, int bitDepth) {
        int recordMinBufferSize = AudioRecord.getMinBufferSize(sampleRate, channel, bitDepth);
        //指定 AudioRecord 缓冲区大小
        buffer = new byte[recordMinBufferSize];
        //根据录音参数构造AudioRecord实体对象
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, channel,
                bitDepth, recordMinBufferSize);
        //初始化编码器
        pcmEncoder = new PCMEncoder(sampleRateInHz, this, PCMEncoder.AAC_FORMAT);
        flvPacker = new FLVPacker();
    }

    /**
     * 开始录制
     */
    public void start() {
        if (audioRecord.getState() == AudioRecord.RECORDSTATE_STOPPED) {
            recorderState = true;
            audioRecord.startRecording();
            new RecordThread().start();
        }
    }

    /**
     * 停止录制
     */
    public void stop() {
        recorderState = false;
        if (audioRecord.getState() == AudioRecord.RECORDSTATE_RECORDING) {
            audioRecord.stop();
        }
    }

    public void release() {
        audioRecord.release();
    }

    @Override
    public void encodeAAC(byte[] data, long time) {
        byte[] flvData = flvPacker.getFLV(data);
        String deviceID = "AQTV2839QJ/sp01_32820237_6";
        XP2P.dataSend(deviceID, flvData, flvData.length);
    }

    @Override
    public void encodeG711(byte[] data) {
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
