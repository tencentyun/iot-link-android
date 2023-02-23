package com.tencent.iot.video.link.util.audio;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.media.audiofx.AcousticEchoCanceler;
import android.media.audiofx.AutomaticGainControl;
import android.text.TextUtils;
import android.util.Log;

import com.iot.soundtouch.interfaces.SoundTouch;
import com.iot.voice.changer.VoiceChangerJNIBridge;
import com.tencent.iot.thirdparty.flv.FLVListener;
import com.tencent.iot.thirdparty.flv.FLVPacker;
import com.tencent.xnet.XP2P;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class AudioRecordUtil implements EncoderListener, FLVListener {
    private static final int DEFAULT_CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_STEREO; //设置音频的录制的声道CHANNEL_IN_STEREO为双声道，CHANNEL_CONFIGURATION_MONO为单声道
    private static final int DEFAULT_AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT; //音频数据格式:PCM 16位每个样本。保证设备支持。PCM 8位每个样本。不一定能得到设备支持。
    private static final String TAG = AudioRecordUtil.class.getName();;
    private volatile boolean recorderState = true; //录制状态
    private byte[] buffer;
    private AudioRecord audioRecord;
    private AcousticEchoCanceler canceler;
    private AutomaticGainControl control;
    private volatile PCMEncoder pcmEncoder;
    private volatile FLVPacker flvPacker;
    private Context context;
    private String deviceId; //"productId/deviceName"
    private int recordMinBufferSize;
    private int sampleRate; //音频采样率
    private int channel;
    private int bitDepth;
    private int channelCount; //声道数
    private int encodeBit; //位深
    private int pitch = 0; //变调【-12~12】
    private VoiceChangerMode mode = VoiceChangerMode.VOICE_CHANGER_MODE_NONE;
    private boolean enableAEC = false;
    private boolean enableAGC = false;

    private boolean isRecord = false;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private String speakFlvFilePath = "/storage/emulated/0/speak.flv";
    private FileOutputStream fos;

    private SoundTouch st;

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
    public AudioRecordUtil(Context ctx, String id, int sampleRate, int channel, int bitDepth, int pitch) {
        context = ctx;
        deviceId = id;
        this.pitch = pitch;
        init(sampleRate, channel, bitDepth);
    }
    public AudioRecordUtil(Context ctx, String id, int sampleRate, int channel, int bitDepth, boolean enableAEC, boolean enableAGC) {
        context = ctx;
        deviceId = id;
        this.enableAEC = enableAEC;
        this.enableAGC = enableAGC;
        init(sampleRate, channel, bitDepth);
    }
    public AudioRecordUtil(Context ctx, String id, int sampleRate, int channel, int bitDepth, int pitch, boolean enableAEC, boolean enableAGC) {
        context = ctx;
        deviceId = id;
        this.pitch = pitch;
        this.enableAEC = enableAEC;
        this.enableAGC = enableAGC;
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
        if (bitDepth == AudioFormat.ENCODING_PCM_16BIT) {
            this.encodeBit = 16;
        } else if (bitDepth == AudioFormat.ENCODING_PCM_8BIT) {
            this.encodeBit = 8;
        }
        recordMinBufferSize = (sampleRate*this.channelCount*this.encodeBit/8)/1000*20; //20ms数据长度
        Log.e(TAG, "AudioRecordUtil init Pitch is: "+ pitch);
    }

    public void recordSpeakFlv(boolean isRecord) {
        this.isRecord = isRecord;
        if (isRecord && !TextUtils.isEmpty(speakFlvFilePath)) {
            File file = new File(speakFlvFilePath);
            Log.i(TAG, "speak cache flv file path:" + speakFlvFilePath);
            if (file.exists()) {
                file.delete();
            }
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                fos = new FileOutputStream(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Log.e(TAG, "临时缓存文件未找到");
            }
        }
    }

    // start之前设置有效 start过程中无法改变本次对讲音调
    public void setPitch(int pitch) {
        Log.e(TAG, "setPitch is: "+ pitch);
        this.pitch = pitch;
    }

    public void setMode(VoiceChangerMode mode) {
        Log.e(TAG, "setMode is: "+ mode);
        this.mode = mode;
        if (mode == VoiceChangerMode.VOICE_CHANGER_MODE_MAN) {
            this.pitch = -6;
        } else if (mode == VoiceChangerMode.VOICE_CHANGER_MODE_WOMAN) {
            this.pitch = 6;
        } else {
            this.pitch = 0;
        }
    }

    /**
     * 开始录制
     */
    public void start() {
        reset();
        if (!VoiceChangerJNIBridge.isAvailable()) {
            if (st == null) {
                st = new SoundTouch(0,channelCount,sampleRate,bitDepth,1.0f, pitch);
            }
        } else {
            VoiceChangerJNIBridge.init(sampleRate,channelCount);
            VoiceChangerJNIBridge.setMode(this.mode.getValue());
        }
        recorderState = true;
        audioRecord.startRecording();
        new RecordThread().start();
    }

    private void reset() {
        buffer = new byte[recordMinBufferSize];
        if (enableAEC) {
            audioRecord = new AudioRecord(MediaRecorder.AudioSource.VOICE_COMMUNICATION, sampleRate, channel, bitDepth, recordMinBufferSize);
        } else {
            audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, channel, bitDepth, recordMinBufferSize);
        }
        pcmEncoder = new PCMEncoder(sampleRate, channelCount, this, PCMEncoder.AAC_FORMAT);
        flvPacker = new FLVPacker(this, true, false);
        int audioSessionId = audioRecord.getAudioSessionId();
        if (enableAEC) {
            Log.e(TAG, "=====initAEC result: " + initAEC(audioSessionId));
        }
        if (enableAGC) {
            Log.e(TAG, "=====initAGC result: " + initAGC(audioSessionId));
        }
    }

    /**
     * 停止录制
     */
    public void stop() {
        recorderState = false;
        if (audioRecord != null) {
            audioRecord.stop();
        }

        executor.shutdown();
        audioRecord = null;
        pcmEncoder = null;
        flvPacker.release();
        flvPacker = null;
        if (canceler != null) {
            canceler.setEnabled(false);
            canceler.release();
            canceler = null;
        }
        if (control != null) {
            control.setEnabled(false);
            control.release();
            control = null;
        }

        if (!VoiceChangerJNIBridge.isAvailable()) {
            if (st != null) {
                st.finish();
                st.clearBuffer(0);
                st = null;
            }
        } else {
            VoiceChangerJNIBridge.destory();
        }
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

        if (executor.isShutdown()) return;
        executor.submit(() -> {
            if (fos != null) {
                try {
                    fos.write(data);
                    fos.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private class RecordThread extends Thread {
        @Override
        public void run() {
            while (recorderState) {
                int read = audioRecord.read(buffer, 0, buffer.length);
                if (!VoiceChangerJNIBridge.isAvailable()) {
                    if (pitch != 0 && st != null) {
                        st.putBytes(buffer);
                        int bytesReceived = st.getBytes(buffer);
                    }
                } else {
                    if (pitch != 0) {
                        VoiceChangerJNIBridge.voiceChangerRun(buffer, buffer, buffer.length/(encodeBit/8));
                    }
                }
                if (AudioRecord.ERROR_INVALID_OPERATION != read) {
                    //获取到的pcm数据就是buffer了
                    if (buffer != null) {
                        pcmEncoder.encodeData(buffer);
                    }
                }
            }
        }
    }

    public boolean isDevicesSupportAEC() {
        return AcousticEchoCanceler.isAvailable();
    }
    private boolean initAEC(int audioSession) {
        boolean isDevicesSupportAEC = isDevicesSupportAEC();
        Log.e(TAG, "isDevicesSupportAEC: "+isDevicesSupportAEC);
        if (!isDevicesSupportAEC) {
            return false;
        }
        if (canceler != null) {
            return false;
        }
        canceler = AcousticEchoCanceler.create(audioSession);
        canceler.setEnabled(true);
        return canceler.getEnabled();
    }
    public boolean isDevicesSupportAGC() {
        return AutomaticGainControl.isAvailable();
    }
    private boolean initAGC(int audioSession) {
        boolean isDevicesSupportAGC = isDevicesSupportAGC();
        Log.e(TAG, "isDevicesSupportAGC: "+isDevicesSupportAGC);
        if (!isDevicesSupportAGC) {
            return false;
        }
        if (control != null) {
            return false;
        }
        control = AutomaticGainControl.create(audioSession);
        control.setEnabled(true);
        return control.getEnabled();
    }
}
