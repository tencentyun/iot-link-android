package com.tencent.iot.explorer.link.demo.video.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;

public class VolumeChangeObserver {
    private static final String VOLUME_CHANGED_ACTION = "android.media.VOLUME_CHANGED_ACTION";
    private static final String EXTRA_VOLUME_STREAM_TYPE = "android.media.EXTRA_VOLUME_STREAM_TYPE";

    public interface VolumeChangeListener {
        void onVolumeChanged(int volume);
    }

    private VolumeChangeListener mVolumeChangeListener;
    private VolumeBroadcastReceiver mVolumeBroadcastReceiver;
    private Context mContext;
    private AudioManager mAudioManager;
    private boolean mRegistered = false;

    public VolumeChangeObserver(Context context) {
        mContext = context;
        mAudioManager = (AudioManager) context.getApplicationContext()
                .getSystemService(Context.AUDIO_SERVICE);
    }

    public int getCurrentMusicVolume() {
        return mAudioManager != null ? mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC) : -1;
    }

    public int getMaxMusicVolume() {
        return mAudioManager != null ? mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) : 15;
    }

    public VolumeChangeListener getVolumeChangeListener() {
        return mVolumeChangeListener;
    }

    public void setVolumeChangeListener(VolumeChangeListener volumeChangeListener) {
        this.mVolumeChangeListener = volumeChangeListener;
    }

    public void registerReceiver() {
        mVolumeBroadcastReceiver = new VolumeBroadcastReceiver(this);
        IntentFilter filter = new IntentFilter();
        filter.addAction(VOLUME_CHANGED_ACTION);
        mContext.registerReceiver(mVolumeBroadcastReceiver, filter);
        mRegistered = true;
    }
    
    public void unregisterReceiver() {
        if (mRegistered) {
            try {
                mContext.unregisterReceiver(mVolumeBroadcastReceiver);
                mVolumeChangeListener = null;
                mRegistered = false;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static class VolumeBroadcastReceiver extends BroadcastReceiver {
        private VolumeChangeObserver mObserver;

        public VolumeBroadcastReceiver(VolumeChangeObserver volumeChangeObserver) {
            mObserver = volumeChangeObserver;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            //媒体音量改变才通知
            int test = intent.getIntExtra(EXTRA_VOLUME_STREAM_TYPE, -1);
            if (VOLUME_CHANGED_ACTION.equals(intent.getAction())
                    && (intent.getIntExtra(EXTRA_VOLUME_STREAM_TYPE, -1) == AudioManager.STREAM_MUSIC)) {
                if (mObserver != null) {
                    VolumeChangeListener listener = mObserver.getVolumeChangeListener();
                    if (listener != null) {
                        int volume = mObserver.getCurrentMusicVolume();
                        if (volume >= 0) {
                            listener.onVolumeChanged(volume);
                        }
                    }
                }
            }
        }
    }
}
