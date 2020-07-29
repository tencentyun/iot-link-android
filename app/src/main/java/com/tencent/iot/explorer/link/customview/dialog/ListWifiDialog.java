package com.tencent.iot.explorer.link.customview.dialog;

import android.app.Dialog;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import com.tencent.iot.explorer.link.R;
import com.tencent.iot.explorer.link.util.check.LocationUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ListWifiDialog extends Dialog implements View.OnClickListener {

    private DisplayMetrics displayMetrics;
    private View view;
    private Context mContext;
    private WifiListAdapter.OnWifiClicked wifiClicked;
    TextView refreshWifi;
    TextView cancel;
    TextView title;
    RelativeLayout tipWifi;
    ListView wifiList;
    ConstraintLayout layout;
    RelativeLayout tiplayout;

    public ListWifiDialog(Context context, WifiListAdapter.OnWifiClicked onWifiClicked) {
        super(context, R.style.iOSDialog);
        this.wifiClicked = onWifiClicked;
        mContext = context;
        displayMetrics = context.getResources().getDisplayMetrics();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //设置view 弹出的平移动画，从底部-100% 平移到自身位置
        TranslateAnimation animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF,
                0f, Animation.RELATIVE_TO_SELF, 1f, Animation.RELATIVE_TO_SELF, 0);
        animation.setInterpolator(new DecelerateInterpolator());
        animation.setDuration(350);
        animation.setStartOffset(150);

        view = View.inflate(mContext, R.layout.popup_smart_config_network, null);
        view.setAnimation(animation);//设置动画

        refreshWifi = view.findViewById(R.id.btn_wifi_refresh);
        cancel = view.findViewById(R.id.btn_cancel);
        title = view.findViewById(R.id.dialog_title);
        tipWifi = view.findViewById(R.id.btn_tip_wifi);
        wifiList = view.findViewById(R.id.wifi_list);
        layout = view.findViewById(R.id.dialog_layout);
        tiplayout = view.findViewById(R.id.check_wifi_tip);
        showViewByGPSService();
        cancel.setOnClickListener(this);
        layout.setOnClickListener(this);
        title.setOnClickListener(this);
        tipWifi.setOnClickListener(this);
    }

    private void showViewByGPSService() {
        if (LocationUtil.isLocationServiceEnable(mContext)) {
            tiplayout.setVisibility(View.GONE);
            List<WifiInfo> allWifi = refreshAllWifi();
            WifiListAdapter adapter = new WifiListAdapter(mContext, allWifi);
            adapter.setOnWifiClicked(new WifiListAdapter.OnWifiClicked() {

                @Override
                public void OnWifiClicked(WifiInfo item) {
                    if (wifiClicked != null) {
                        wifiClicked.OnWifiClicked(item);
                        dismiss();
                    }
                }
            });
            wifiList.setAdapter(adapter);
            refreshWifi.setOnClickListener(this);

        } else {
            refreshWifi.setVisibility(View.GONE);
            tiplayout.setVisibility(View.VISIBLE);
        }
    }

    private List<WifiInfo> refreshAllWifi() {
        WifiManager WifiManager = (WifiManager) mContext.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        android.net.wifi.WifiInfo currentWifi = WifiManager.getConnectionInfo();
        if (WifiManager != null) {
            List<ScanResult> scanResults = WifiManager.getScanResults();
            List<WifiInfo> allWifi = new ArrayList<>();
            Set<String> ssidSet = new HashSet<>();
            for (ScanResult wifi : scanResults) {
                // 同样的 wifi 只添加一次，没有 wifi 明的不添加
                if (wifi.SSID.isEmpty() || ssidSet.contains(wifi.SSID)) {
                    continue;
                }

                WifiInfo ele = new WifiInfo();
                String tagSSid = "\"" + wifi.SSID + "\"";
                if (tagSSid.equals(currentWifi.getSSID())) {
                    ele.setConnected(true);
                } else {
                    ele.setConnected(false);
                }
                ele.setSsid(wifi.SSID);
                ele.setWithoutPwd(true);
                ele.setBssid(wifi.BSSID);
                ssidSet.add(ele.getSsid());
                allWifi.add(ele);
            }
            return allWifi;
        }
        return null;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_wifi_refresh:
                return;
            case R.id.dialog_title:
                if (onResultChangeListener != null) {
                    onResultChangeListener.OnCancelClicked();
                }
                // 刷新不会结束当前对话框
                return;
            case R.id.btn_cancel:
                if (onResultChangeListener != null) {
                    onResultChangeListener.OnRefreshWifiClicked();
                }
                break;
            case R.id.btn_tip_wifi:
                showViewByGPSService();
                return;
            default:
                break;
        }
        dismiss();
    }

    private OnResultChangeListener onResultChangeListener;

    //对外的接口回调
    public interface OnResultChangeListener {
        void OnCancelClicked();
        void OnRefreshWifiClicked();
    }

    public void setOnResultChangeListener(OnResultChangeListener onResultChangeListener) {
        this.onResultChangeListener = onResultChangeListener;
    }

    @Override
    public void show() {
        super.show();
        // 设置dialog的宽高是全屏，注意：一定要放在show的后面，否则不是全屏显示
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.width = displayMetrics.widthPixels;
        params.height = ViewGroup.LayoutParams.MATCH_PARENT;
        params.gravity = Gravity.BOTTOM;
        getWindow().setAttributes(params);
        getWindow().setContentView(view);
    }

}
