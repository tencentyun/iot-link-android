package com.tencent.iot.explorer.link.kitlink.activity.videoui;

import android.hardware.Camera;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tencent.iot.explorer.link.App;
import com.tencent.iot.explorer.link.R;
import com.tencent.iot.explorer.link.kitlink.activity.BaseActivity;
import com.tencent.iot.explorer.link.kitlink.adapter.FrameRateListAdapter;
import com.tencent.iot.explorer.link.kitlink.adapter.ResolutionListAdapter;
import com.tencent.iot.explorer.link.kitlink.entity.FrameRateEntity;
import com.tencent.iot.explorer.link.kitlink.entity.ResolutionEntity;
import com.tencent.iot.video.link.consts.CameraConstants;

import java.util.ArrayList;
import java.util.List;

public class ParamSettingActivity extends BaseActivity {

    private static final String TAG = ParamSettingActivity.class.getSimpleName();

    private TextView titleTv;
    private ImageView backIv;
    private RecyclerView resolutionRv;
    private ResolutionListAdapter resolutionAdapter = null;
    private ArrayList<ResolutionEntity> resolutionDatas = new ArrayList<>();
    private RecyclerView frameRateRv;
    private FrameRateListAdapter frameRateAdapter = null;
    private ArrayList<FrameRateEntity> frameRateDatas = new ArrayList<>();
    private EditText frameDropEt;
    private EditText frameSpeedEt;
    private Button confirm;

    @Override
    public int getContentView() {
        return R.layout.activity_param_setting;
    }

    @Override
    public void initView() {
        titleTv = findViewById(R.id.tv_title);
        titleTv.setText("参数设置");
        backIv = findViewById(R.id.iv_back);
        backIv.setVisibility(View.INVISIBLE);
        resolutionRv = findViewById(R.id.rv_resolution);
        getSupportedPreviewSizes();
        LinearLayoutManager resolutionLayoutManager = new LinearLayoutManager(this);
        resolutionRv.setLayoutManager(resolutionLayoutManager);
        resolutionRv.setHasFixedSize(false);
        resolutionAdapter = new ResolutionListAdapter(ParamSettingActivity.this, resolutionDatas);
        resolutionRv.setAdapter(resolutionAdapter);

        frameRateRv = findViewById(R.id.rv_frame_rate);
        frameRateDatas = new ArrayList<FrameRateEntity>();
        frameRateDatas.add(new FrameRateEntity(15, true));
        frameRateDatas.add(new FrameRateEntity(30));
        LinearLayoutManager frameLayoutManager = new LinearLayoutManager(this);
        frameRateRv.setLayoutManager(frameLayoutManager);
        frameRateRv.setHasFixedSize(false);
        frameRateAdapter = new FrameRateListAdapter(ParamSettingActivity.this, frameRateDatas);
        frameRateRv.setAdapter(frameRateAdapter);

        frameDropEt = findViewById(R.id.et_framedrop);
        frameSpeedEt = findViewById(R.id.et_framespeed);
        confirm = findViewById(R.id.confirm);
    }

    @Override
    public void setListener() {
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ResolutionEntity selectedResolutionEntity = resolutionAdapter.selectedResolutionEntity();
                FrameRateEntity selectedFrameRateEntity = frameRateAdapter.selectedFrameRateEntity();
                App.Companion.getData().setResolutionWidth(selectedResolutionEntity.getWidth());
                App.Companion.getData().setResolutionHeight(selectedResolutionEntity.getHeight());
                App.Companion.getData().setFrameRate(selectedFrameRateEntity.getRate());
                String framedrop = frameDropEt.getText().toString();
                if (!TextUtils.isEmpty(framedrop)) {
                    int fd = Integer.parseInt(framedrop);
                    App.Companion.getData().setFrameDrop(fd);
                }
                String framespeed = frameSpeedEt.getText().toString();
                if (!TextUtils.isEmpty(framespeed)) {
                    float fs = Float.parseFloat(framespeed);
                    App.Companion.getData().setFrameSpeed(fs);
                }
                finish();
            }
        });
    }

    /**
     * 获取设备支持的最大分辨率
     */
    private void getSupportedPreviewSizes() {
        Camera camera = Camera.open(CameraConstants.facing.BACK);
        //获取相机参数
        Camera.Parameters parameters = camera.getParameters();
        List<Camera.Size> list = parameters.getSupportedPreviewSizes();
        resolutionDatas = new ArrayList<ResolutionEntity>();
        for (Camera.Size size : list) {
            Log.e(TAG, "****========== " + size.width + " " + size.height);
            ResolutionEntity entity = new ResolutionEntity(size.width, size.height);
            resolutionDatas.add(entity);
        }
        if (resolutionDatas.size() > 0) {
            ResolutionEntity entity = resolutionDatas.get(resolutionDatas.size() - 1);
            entity.setIsSelect(true);
        } else {
            Toast.makeText(ParamSettingActivity.this, "无法获取到设备Camera支持的分辨率", Toast.LENGTH_SHORT).show();
        }
        camera.setPreviewCallback(null);
        camera.stopPreview();
        camera.release();
        camera = null;
    }
}