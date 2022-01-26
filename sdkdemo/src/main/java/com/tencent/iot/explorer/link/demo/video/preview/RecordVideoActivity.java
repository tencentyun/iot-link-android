package com.tencent.iot.explorer.link.demo.video.preview;

import android.graphics.SurfaceTexture;
import android.net.Uri;
import android.os.Handler;
import android.view.Surface;
import android.view.TextureView;
import android.widget.Button;

import com.tencent.iot.explorer.link.demo.R;
import com.tencent.iot.explorer.link.demo.VideoBaseActivity;
import com.tencent.iot.video.link.recorder.VideoRecorder;
import com.tencent.iot.video.link.recorder.opengles.view.CameraView;

import java.io.IOException;

import tv.danmaku.ijk.media.player.IjkMediaPlayer;

public class RecordVideoActivity extends VideoBaseActivity implements TextureView.SurfaceTextureListener{

    private String TAG = RecordVideoActivity.class.getSimpleName();
    private CameraView cameraView;
    private Button btnSwitch;
    private final VideoRecorder videoRecorder = new VideoRecorder();
    private String path; // 保存源文件的路径
    private Handler handler = new Handler();
    private IjkMediaPlayer player;
    private volatile Surface surface;
    private TextureView playView;

    @Override
    protected void onResume() {
        super.onResume();
        cameraView.openCamera();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraView.closeCamera();
        videoRecorder.cancel();
        videoRecorder.stop();
        if (player != null) {
            player.stop();
        }
    }

    @Override
    public int getContentView() {
        return R.layout.activity_record_video;
    }

    @Override
    public void initView() {
        path = getFilesDir().getAbsolutePath();
        setContentView(R.layout.activity_record_video);
        cameraView = findViewById(R.id.camera_view);
        btnSwitch = findViewById(R.id.btn_switch);
        videoRecorder.attachCameraView(cameraView);
        playView = findViewById(R.id.v_play);
        playView.setSurfaceTextureListener(this);

    }

    @Override
    public void setListener() {
        btnSwitch.setOnClickListener(v -> cameraView.switchCamera());
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        if (surface != null) {
            this.surface = new Surface(surface);
            play();
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    private void play() {
        player = new IjkMediaPlayer();
        player.reset();

        player.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "analyzemaxduration", 100);
        player.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "probesize", 25 * 1024);
//        player.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "analyzeduration", 1000);
//        player.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "probesize", 64);
        player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "packet-buffering", 0);
        player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "start-on-prepared", 1);
        player.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "threads", 1);
        player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "sync-av-start", 0);
        player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec",1);
        player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-auto-rotate", 1);
        player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-handle-resolution-change", 1);
        player.setSurface(this.surface);
        try {
            player.setDataSource("http://www.helpexamples.com/flash/video/cuepoints.flv");
        } catch (IOException e) {
            e.printStackTrace();
        }
        player.prepareAsync();
        player.start();
    }
}
