package com.tencent.iot.video.link.util.audio;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.media.audiofx.AcousticEchoCanceler;
import android.media.audiofx.AutomaticGainControl;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.iot.soundtouch.interfaces.SoundTouch;
import com.iot.voice.changer.VoiceChangerJNIBridge;
import com.tencent.iot.thirdparty.flv.FLVListener;
import com.tencent.iot.thirdparty.flv.FLVPacker;
import com.tencent.xnet.XP2P;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;

import com.iot.gvoice.interfaces.GvoiceJNIBridge;


public class AudioRecordUtil implements EncoderListener, FLVListener, Handler.Callback {
    private static final int DEFAULT_CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_STEREO; //设置音频的录制的声道CHANNEL_IN_STEREO为双声道，CHANNEL_CONFIGURATION_MONO为单声道
    private static final int DEFAULT_AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT; //音频数据格式:PCM 16位每个样本。保证设备支持。PCM 8位每个样本。不一定能得到设备支持。
    private static final String TAG = AudioRecordUtil.class.getName() + "[IOT-XP2P]";
    private static final int MSG_START = 1;
    private static final int MSG_STOP = 2;
    private static final int MSG_REC_PLAY_PCM = 3;
    private static final int MSG_RELEASE = 4;
    private final HandlerThread readThread;
    private final ReadHandler mReadHandler;
    private volatile boolean recorderState = true; //录制状态
    private byte[] buffer;
    private AudioRecord audioRecord;
    private volatile AcousticEchoCanceler canceler;
    private volatile AutomaticGainControl control;
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
    private FileOutputStream fos1;
    private FileOutputStream fos2;
    private FileOutputStream fos3;
    private String speakPcmFilePath = "/storage/emulated/0/speak_pcm_";

    private SoundTouch st;

    private static final int SAVE_PCM_DATA = 1;

    private LinkedBlockingDeque<Byte> playPcmData = new LinkedBlockingDeque<>();  // 内存队列，用于缓存获取到的播放器音频pcm

    @Override
    public boolean handleMessage(@NotNull Message msg) {
        switch (msg.what) {
            case MSG_START:
                startInternal();
                break;
            case MSG_STOP:
                stopInternal();
                break;
            case MSG_REC_PLAY_PCM:
                recPlayPcmInternal((byte[]) msg.obj);
                break;
            case MSG_RELEASE:
                releaseInternal();
                break;
        }
        return false;
    }

    private class MyHandler extends Handler {

        public MyHandler() {
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            try {
                if (msg.what == SAVE_PCM_DATA && fos1 != null && fos2 != null && fos3 != null) {
                    JSONObject jsonObject = (JSONObject) msg.obj;
                    byte[] nearBytesData = (byte[]) jsonObject.get("nearPcmBytes");
                    fos1.write(nearBytesData);
                    fos1.flush();
                    byte[] playerPcmBytes = (byte[]) jsonObject.get("playerPcmBytes");
                    fos2.write(playerPcmBytes);
                    fos2.flush();
                    byte[] aecPcmBytes = (byte[]) jsonObject.get("aecPcmBytes");
                    fos3.write(aecPcmBytes);
                    fos3.flush();
                }

            } catch (IOException e) {
                Log.e(TAG, "*======== IOException: " + e);
                e.printStackTrace();
            } catch (JSONException e) {
                Log.e(TAG, "*======== JSONException: " + e);
                e.printStackTrace();
            }
        }
    }
    private final Handler mHandler = new MyHandler();

    public AudioRecordUtil(Context ctx, String id, int sampleRate) {
        context = ctx;
        deviceId = id;
        init(sampleRate, DEFAULT_CHANNEL_CONFIG, DEFAULT_AUDIO_FORMAT);
        readThread = new HandlerThread(TAG);
        readThread.start();
        mReadHandler = new ReadHandler(readThread.getLooper(), this);
    }
    public AudioRecordUtil(Context ctx, String id, int sampleRate, int channel, int bitDepth) {
        context = ctx;
        deviceId = id;
        init(sampleRate, channel, bitDepth);
        readThread = new HandlerThread(TAG);
        readThread.start();
        mReadHandler = new ReadHandler(readThread.getLooper(), this);
    }
    public AudioRecordUtil(Context ctx, String id, int sampleRate, int channel, int bitDepth, int pitch) {
        context = ctx;
        deviceId = id;
        this.pitch = pitch;
        init(sampleRate, channel, bitDepth);
        readThread = new HandlerThread(TAG);
        readThread.start();
        mReadHandler = new ReadHandler(readThread.getLooper(), this);
    }
    public AudioRecordUtil(Context ctx, String id, int sampleRate, int channel, int bitDepth, boolean enableAEC, boolean enableAGC) {
        context = ctx;
        deviceId = id;
        this.enableAEC = enableAEC;
        this.enableAGC = enableAGC;
        init(sampleRate, channel, bitDepth);
        readThread = new HandlerThread(TAG);
        readThread.start();
        mReadHandler = new ReadHandler(readThread.getLooper(), this);
    }
    public AudioRecordUtil(Context ctx, String id, int sampleRate, int channel, int bitDepth, int pitch, boolean enableAEC, boolean enableAGC) {
        context = ctx;
        deviceId = id;
        this.pitch = pitch;
        this.enableAEC = enableAEC;
        this.enableAGC = enableAGC;
        init(sampleRate, channel, bitDepth);
        readThread = new HandlerThread(TAG);
        readThread.start();
        mReadHandler = new ReadHandler(readThread.getLooper(), this);
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
        Log.e(TAG, "recordMinBufferSize is: "+ recordMinBufferSize);
        recordMinBufferSize = (sampleRate*this.channelCount*this.encodeBit/8)/1000*20; //20ms数据长度
        Log.e(TAG, "20ms recordMinBufferSize is: "+ recordMinBufferSize);
        Log.e(TAG, "AudioRecordUtil init Pitch is: "+ pitch);
        GvoiceJNIBridge.init(context);
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

    private FileOutputStream createFiles(String format) {

        if (!TextUtils.isEmpty(speakPcmFilePath)) {
            File file1 = new File(speakPcmFilePath+format+".pcm");
            Log.i(TAG, "speak cache pcm file path:" + speakPcmFilePath);
            if (file1.exists()) {
                file1.delete();
            }
            try {
                file1.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
            try {
                FileOutputStream fos = new FileOutputStream(file1);
                return fos;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Log.e(TAG, "临时缓存文件未找到");
                return null;
            }
        }
        return null;
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

    public void setPlayerPcmData(byte[] pcmData) {
        mReadHandler.obtainMessage(MSG_REC_PLAY_PCM, pcmData).sendToTarget();
    }

    /**
     * 开始录制
     */
    public void start() {
        Log.i(TAG, "start");
        mReadHandler.obtainMessage(MSG_START).sendToTarget();
    }

    private void startInternal() {
        if (isRecord) {
            fos1 = createFiles("near");
            fos2 = createFiles("far");
            fos3 = createFiles("aec");
        }
        if (!playPcmData.isEmpty()) {
            playPcmData.clear();
        }
        reset();
        if (!VoiceChangerJNIBridge.isAvailable()) {
            if (st == null && pitch != 0) {
                st = new SoundTouch(0,channelCount,sampleRate,bitDepth,1.0f, pitch);
            }
        } else {
            VoiceChangerJNIBridge.init(sampleRate,channelCount);
            VoiceChangerJNIBridge.setMode(this.mode.getValue());
        }
        recorderState = true;
        Log.e(TAG, "turn recorderState : " + recorderState);
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
        Log.e(TAG, "reset new FLVPacker");
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
        Log.i(TAG, "stop");
        mReadHandler.obtainMessage(MSG_STOP).sendToTarget();
    }

    private void stopInternal() {
        recorderState = false;
        Log.e(TAG, "turn recorderState : " + recorderState);
        try {
            Thread.sleep(200);
            Log.e(TAG, "Thread.sleep 200 turn recorderState : " + recorderState);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (audioRecord != null) {
            audioRecord.stop();
        }
        executor.shutdown();
        audioRecord = null;
        pcmEncoder = null;
        if (flvPacker != null) {
            flvPacker.release();
            flvPacker = null;
        }
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
//                GvoiceJNIBridge.destory();
    }

    public void release() {
        Log.i(TAG, "release");
        mReadHandler.obtainMessage(MSG_STOP).sendToTarget();
    }

    private void releaseInternal() {
        audioRecord.release();
    }

    @Override
    public void encodeAAC(byte[] data, long time) {
        if (flvPacker != null && data != null && data.length != 0) {
            flvPacker.encodeFlv(data, FLVPacker.TYPE_AUDIO, System.currentTimeMillis());
        }
    }

    @Override
    public void encodeG711(byte[] data) { }

    @Override
    public void onFLV(byte[] data) {
        if (recorderState) {
            Log.i(TAG, "===== XP2P.dataSend dataLen:" + data.length);
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
    }

    private void writePcmBytesToFile(byte[] nearPcmBytes, byte[] playerPcmBytes, byte[] aecPcmBytes) {
        if (mHandler != null) {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("nearPcmBytes", nearPcmBytes);
                jsonObject.put("playerPcmBytes", playerPcmBytes);
                jsonObject.put("aecPcmBytes", aecPcmBytes);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Message message = mHandler.obtainMessage(SAVE_PCM_DATA, jsonObject);
            mHandler.sendMessage(message);
        }
    }

    private class RecordThread extends Thread {
        @Override
        public void run() {
            while (recorderState) {
                int read = audioRecord.read(buffer, 0, buffer.length);
                Log.i(TAG, "audioRecord.read: "+read + "， buffer.length： " + buffer.length + ", recorderState: " + recorderState);
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
                    if (buffer != null && pcmEncoder != null) {
                        byte [] playerPcmBytes = onReadPlayerPlayPcm(buffer.length);
                        if (playerPcmBytes != null && playerPcmBytes.length > 0) {
                            byte[] aecPcmBytes = GvoiceJNIBridge.cancellation(buffer, playerPcmBytes);
                            if (isRecord) {
                                writePcmBytesToFile(buffer, playerPcmBytes, aecPcmBytes);
                            }
                            pcmEncoder.encodeData(aecPcmBytes);
                        } else {
                            pcmEncoder.encodeData(buffer);
                        }
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
        if (canceler != null) {
            canceler.setEnabled(true);
            return canceler.getEnabled();
        } else {
            return false;
        }
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
        if (control != null) {
            control.setEnabled(true);
            return control.getEnabled();
        } else {
            return false;
        }
    }

    private byte[] onReadPlayerPlayPcm(int length) {
        if (playPcmData.size() > length) {
            byte[] res = new byte[length];
            try {
                for (int i = 0 ; i < length ; i++) {
                    res[i] = playPcmData.take();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Log.e(TAG, "onReadPlayerPlayPcm  playPcmData.length： " + playPcmData.size());
            if (playPcmData.size()>20000) {
                playPcmData.clear();
            }
            return res;
        } else {
            return new byte[length];
        }
    }

    private void recPlayPcmInternal(byte [] pcmData) {
        if (pcmData != null && pcmData.length > 0 && recorderState) {
            List<Byte> tmpList = new ArrayList<>();
            for (byte b : pcmData) {
                tmpList.add(b);
            }
            playPcmData.addAll(tmpList);
        }
    }

    public static class ReadHandler extends Handler {

        public ReadHandler(Looper looper, Callback callback) {
            super(looper, callback);
        }

        public void runAndWaitDone(final Runnable runnable) {
            final CountDownLatch countDownLatch = new CountDownLatch(1);
            post(() -> {
                runnable.run();
                countDownLatch.countDown();
            });

            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
