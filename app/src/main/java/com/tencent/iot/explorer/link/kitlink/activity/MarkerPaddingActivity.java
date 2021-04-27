package com.tencent.iot.explorer.link.kitlink.activity;

import android.Manifest.permission;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Looper;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.scwang.smart.refresh.footer.ClassicsFooter;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnRefreshLoadMoreListener;
import com.tencent.iot.explorer.link.R;
import com.tencent.iot.explorer.link.T;
import com.tencent.iot.explorer.link.core.auth.callback.MyCallback;
import com.tencent.iot.explorer.link.core.auth.http.HttpCallBack;
import com.tencent.iot.explorer.link.core.auth.http.HttpUtil;
import com.tencent.iot.explorer.link.core.auth.response.BaseResponse;
import com.tencent.iot.explorer.link.kitlink.adapter.PostionsAdapter;
import com.tencent.iot.explorer.link.kitlink.consts.CommonField;
import com.tencent.iot.explorer.link.kitlink.entity.AdInfo;
import com.tencent.iot.explorer.link.kitlink.entity.Address;
import com.tencent.iot.explorer.link.kitlink.entity.Location;
import com.tencent.iot.explorer.link.kitlink.entity.LocationResp;
import com.tencent.iot.explorer.link.kitlink.entity.Postion;
import com.tencent.iot.explorer.link.kitlink.util.HttpRequest;
import com.tencent.iot.explorer.link.kitlink.util.StatusBarHeightView;
import com.tencent.map.geolocation.TencentLocation;
import com.tencent.map.geolocation.TencentLocationListener;
import com.tencent.map.geolocation.TencentLocationManager;
import com.tencent.map.geolocation.TencentLocationRequest;
import com.tencent.tencentmap.mapsdk.maps.CameraUpdateFactory;
import com.tencent.tencentmap.mapsdk.maps.MapView;
import com.tencent.tencentmap.mapsdk.maps.TencentMap;
import com.tencent.tencentmap.mapsdk.maps.model.BitmapDescriptorFactory;
import com.tencent.tencentmap.mapsdk.maps.model.CameraPosition;
import com.tencent.tencentmap.mapsdk.maps.model.LatLng;
import com.tencent.tencentmap.mapsdk.maps.model.Marker;
import com.tencent.tencentmap.mapsdk.maps.model.MarkerOptions;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class MarkerPaddingActivity extends BaseActivity implements TencentMap.OnCameraChangeListener, View.OnClickListener, TencentLocationListener {

    private final int PAGE_SIZE = 20;
    private final String[] permissions = {permission.ACCESS_COARSE_LOCATION, permission.READ_PHONE_STATE, permission.WRITE_EXTERNAL_STORAGE};
    public MapView mMapView;
    private TencentMap mTencentMap;
    private RecyclerView recyclerView;
    private BottomSheetBehavior<View> behavior;
    private View bottomSheet;
    private int heightPixels = 0;
    private int widthPixels = 0;
    private int mapMinHeight = 0;
    private ConstraintLayout mapcontain;
    private ConstraintLayout mapLayout;
    private Marker marker = null;
    private LatLng startPostion = null;
    private String tencentMapKey = "";
    private List<Postion> postions = new CopyOnWriteArrayList();
    private PostionsAdapter adapter = null;
    private TextView title;
    private StatusBarHeightView menuFamilyAddress;
    private volatile int pageSize = 1;
    private ImageView backBtn;
    private ConstraintLayout searchLayout;
    private ConstraintLayout layoutSeachLayout;
    private TextView okBtn;
    private LinearLayoutManager linearLayoutManager;
    private String defaultAddress = "";
    private String familyId = "";
    private String familyName = "";
    private ImageView reloc;
    private volatile boolean requestFlag = true;
    private SmartRefreshLayout smartRefreshLayout = null;
    private volatile boolean firstZoom = true;
    private TencentLocationManager locationManager = null;
    private TencentLocationRequest locationRequest = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            tencentMapKey = this.getPackageManager().getApplicationInfo(this.getPackageName(), PackageManager.GET_META_DATA)
                    .metaData.getString("TencentMapSDK");
        } catch (PackageManager.NameNotFoundException e) {
            tencentMapKey = "";
            e.printStackTrace();
        }

        Bundle bundle = getIntent().getBundleExtra(CommonField.ADDRESS);
        if (bundle != null) {
            defaultAddress = bundle.getString(CommonField.ADDRESS, "");
            familyId = bundle.getString(CommonField.FAMILY_ID, "");
            familyName = bundle.getString(CommonField.FAMILY_NAME, "");
        }

        initView();
        setListener();
        autoLocate(defaultAddress);
    }

    public void autoLocate(String address) {
        if (checkPermissions(permissions)) {
            if (TextUtils.isEmpty(address)) {
                startLocation();
            } else {
                tagPostionByAddress(address);
            }
        } else {
            requestPermission(permissions);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NotNull String[] permissions, @NotNull int[] grantResults) {
        for (int i = 0; i < permissions.length; i++) {
            if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                permissionDenied(permissions[i]);
                return;
            }
        }
        permissionAllGranted();
    }

    @Override
    public void permissionAllGranted() {
        autoLocate(defaultAddress);
    }

    private void tagPostionByAddress(String address) {
        try {
            Address loc = JSONObject.parseObject(address, Address.class);
            if (loc.getLatitude() == 0f && loc.getLongitude() == 0f) {
                startLocation();
                return;
            }
            startPostion = new LatLng(loc.getLatitude(), loc.getLongitude());
            mTencentMap.moveCamera(CameraUpdateFactory.newLatLngZoom(startPostion, 16));
            if (marker != null) marker.remove();
            marker = mTencentMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)).position(startPostion));

        } catch (JSONException e) {
            e.printStackTrace();
            startLocation();
        }
    }

    private void startLocation() {
        //用于访问腾讯定位服务的类, 周期性向客户端提供位置更新
        locationManager = TencentLocationManager.getInstance(this);
        //设置坐标系
        locationManager.setCoordinateType(TencentLocationManager.COORDINATE_TYPE_GCJ02);
        //创建定位请求
        locationRequest = TencentLocationRequest.create();
        //设置定位周期（位置监听器回调周期）为3s
        locationRequest.setInterval(3000);
        //设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
        mTencentMap.setMyLocationEnabled(true);

        int err = locationManager.requestLocationUpdates(locationRequest, this, Looper.myLooper());
        switch (err) {
            case 1:
            case 2:
            case 3:
                T.show(getString(R.string.location_failed));
                break;
        }
    }

    @Override
    public void onLocationChanged(TencentLocation tencentLocation, int i, String s) {
        if (tencentLocation != null && i == TencentLocation.ERROR_OK) {
            android.location.Location location = new android.location.Location(tencentLocation.getProvider());
            //设置经纬度以及精度
            location.setLatitude(tencentLocation.getLatitude());
            location.setLongitude(tencentLocation.getLongitude());
            location.setAccuracy(tencentLocation.getAccuracy());
            startPostion = new LatLng(tencentLocation.getLatitude(), tencentLocation.getLongitude());
            mTencentMap.moveCamera(CameraUpdateFactory.newLatLngZoom(startPostion, 16));
            if (marker != null) marker.remove();
            marker = mTencentMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)).position(startPostion));

        } else {
            T.show(s);
        }
        locationManager.removeUpdates(this);

    }

    @Override
    public void onStatusUpdate(String s, int i, String s1) {}

    public void initView() {
        //获取屏幕高度
        heightPixels = getResources().getDisplayMetrics().heightPixels;
        widthPixels = getResources().getDisplayMetrics().widthPixels;
        mapMinHeight = dip2px(this, 180);

        title = findViewById(R.id.tv_title);
        title.setText(R.string.map_select_postion);
        recyclerView = findViewById(R.id.rec);
        mapcontain = findViewById(R.id.mapcontain);
        menuFamilyAddress = findViewById(R.id.menu_family_address);
        mMapView = findViewById(R.id.map);
        backBtn = findViewById(R.id.iv_back);
        bottomSheet = findViewById(R.id.bottom_sheet);
        searchLayout = findViewById(R.id.layout_seach);
        okBtn = findViewById(R.id.btn_add_family);
        behavior = BottomSheetBehavior.from(bottomSheet);
        reloc = findViewById(R.id.iv_reset_loaction);
        smartRefreshLayout = findViewById(R.id.smart_refreshLayout);
        layoutSeachLayout = findViewById(R.id.layout_seach_layout);
        mapLayout = findViewById(R.id.map_layout);

        smartRefreshLayout.setEnableRefresh(false);
        smartRefreshLayout.setEnableLoadMore(true);
        smartRefreshLayout.setRefreshFooter(new ClassicsFooter(this));
        linearLayoutManager = new LinearLayoutManager(this);
        adapter = new PostionsAdapter(postions);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(linearLayoutManager);
        mTencentMap = mMapView.getMap();

        mapLayout.postDelayed(new Runnable() {
            @Override
            public void run() {
                heightPixels = mapLayout.getHeight();
                widthPixels = mapLayout.getWidth();
                int height = mapLayout.getHeight() - mapLayout.getWidth() - menuFamilyAddress.getHeight();
                behavior.setPeekHeight(height);
            }
        }, 200);
    }

    @Override
    protected void onStart() {
        mMapView.onStart();
        super.onStart();
    }

    @Override
    protected void onResume() {
        mMapView.onResume();
        super.onResume();
    }

    @Override
    protected void onPause() {
        mMapView.onPause();
        super.onPause();
    }

    @Override
    protected void onStop() {
        mMapView.onStop();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        mMapView.onDestroy();
        super.onDestroy();
    }

    public static Float px2dp(Context context, Float f) {
        Float x = context.getResources().getDisplayMetrics().density;
        return f / x;
    }

    private static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) { }

    @Override
    public void onCameraChangeFinished(CameraPosition cameraPosition) {
        if (firstZoom) {
            firstZoom = false;
            return;
        }
        Point point = mTencentMap.getProjection().toScreenLocation(cameraPosition.target);
        if (marker != null) marker.setFixingPoint(point.x, point.y);

        if (!requestFlag) { //只调整指针位置不做网络请求
            requestFlag = true;
        } else { //调整整指针位置，同时从第一页进行网络请求
            startPostion = cameraPosition.target;
            getPostionInfo(startPostion);
        }
    }

    public void getPostionInfo(final LatLng latLng) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                postions.clear();
                pageSize = 1;
                adapter.setSelectPos(0);
                adapter.notifyDataSetChanged();
                linearLayoutManager.scrollToPosition(0);
                getPostionInfo(latLng, pageSize);
            }
        });
    }

    private void moveCameraWithoutRequest(LatLng target) {
        if (mTencentMap == null) return;
        requestFlag = false;
        mTencentMap.moveCamera(CameraUpdateFactory.newLatLng(target));
    }

    public void getPostionInfo(LatLng latLng, int page, final Address firstItem) {
        StringBuilder sb = new StringBuilder("https://apis.map.qq.com/ws/geocoder/v1/?location=");
        sb.append(latLng.latitude).append(",").append(latLng.longitude).append("&key=").append(tencentMapKey);
        sb.append("&get_poi=1").append("&poi_options=page_size=" + PAGE_SIZE + ";page_index=" + page);
        HttpUtil.INSTANCE.get(sb.toString(), new HttpCallBack() {
            @Override public void onError(@NotNull String error) { }
            @Override public void onSuccess(@NotNull String response) {
                final LocationResp locationResp = JSONObject.parseObject(response, LocationResp.class);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (locationResp.getResult().getPois().size() > 0 &&
                                (postions.size() + locationResp.getResult().getPois().size()) < locationResp.getResult().getPoi_count()) {
                            if (firstItem != null) {
                                initFirstItem(locationResp.getResult().getPois(), firstItem);
                            }
                            postions.addAll(locationResp.getResult().getPois());
                            pageSize++;
                        }
                        adapter.notifyDataSetChanged();
                        if (smartRefreshLayout != null) {
                            smartRefreshLayout.finishLoadMore();
                        }
                    }
                });
            }
        });
    }

    public void getPostionInfo(LatLng latLng, int page) {
        getPostionInfo(latLng, page, null);
    }

    private void initFirstItem(List<Postion> all, Address selectedPostion) {
        Postion firstItem = new Postion();
        firstItem.setTitle(selectedPostion.getName());
        firstItem.setAddress(selectedPostion.getAddress());
        firstItem.setLocation(new Location());
        firstItem.getLocation().setLat(selectedPostion.getLatitude());
        firstItem.getLocation().setLng(selectedPostion.getLongitude());
        firstItem.setAd_info(new AdInfo());
        firstItem.getAd_info().setCity(selectedPostion.getCity());
        firstItem.setId(selectedPostion.getId());

        for (int i = 0; i < all.size(); i++) {
            if (all.get(i).getId().equals(firstItem.getId())) {
                all.set(i, all.get(0));
                break;
            }
        }
        all.set(0, firstItem);
    }

    @Override
    public int getContentView() {
        return R.layout.activity_map_address;
    }

    public void setListener() {
        backBtn.setOnClickListener(this);
        searchLayout.setOnClickListener(this);
        okBtn.setOnClickListener(this);
        reloc.setOnClickListener(this);
        mTencentMap.setOnCameraChangeListener(this);
        adapter.setOnItemClicked(new PostionsAdapter.OnItemClicked (){
            @Override
            public void onItemClicked(int pos) {
                adapter.setSelectPos(pos);
                adapter.notifyDataSetChanged();
                LatLng target = new LatLng(adapter.getSelectPostion().getLocation().getLat(), adapter.getSelectPostion().getLocation().getLng());
                moveCameraWithoutRequest(target);
            }
        });
        mapcontain.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (adapter != null && adapter.getSelectPostion() != null && adapter.getSelectPostion().getLocation() != null) {
                    LatLng target = new LatLng(adapter.getSelectPostion().getLocation().getLat(), adapter.getSelectPostion().getLocation().getLng());
                    moveCameraWithoutRequest(target);
                }
            }
        });

        behavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View view, int newState) {
                ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
                int tagHeight = heightPixels - mapMinHeight - menuFamilyAddress.getHeight();
                if (bottomSheet.getHeight() > tagHeight) {
                    layoutParams.height = tagHeight;
                    bottomSheet.setLayoutParams(layoutParams);
                }

                ViewGroup.LayoutParams mapLayoutParams = mapcontain.getLayoutParams();
                switch (newState) {
                    case 3: //处于完全展开的状态
                        mapLayoutParams.height = mapMinHeight;
                        mapcontain.setLayoutParams(mapLayoutParams);
                        break;
                    case 4: //默认的折叠状态
                        mapLayoutParams.height = widthPixels;
                        mapcontain.setLayoutParams(mapLayoutParams);
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View view, float v) { }
        });

        smartRefreshLayout.setOnRefreshLoadMoreListener(new OnRefreshLoadMoreListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                refreshLayout.finishRefresh();
            }

            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                if (startPostion == null || pageSize > 20) {
                    refreshLayout.finishLoadMore();
                    return;
                }
                getPostionInfo(startPostion, pageSize);
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.iv_back) {
            finish();
        } else if (v.getId() == R.id.layout_seach) {
            Intent postionIntent = new Intent(this, SelectPointActivity.class);
            startActivityForResult(postionIntent, CommonField.SELECT_MAP_POSTION_REQ_CODE);
        } else if (v.getId() == R.id.btn_add_family) {
            if (TextUtils.isEmpty(familyId)) { // 新增房间的方式
                if (adapter != null) {
                    Intent data = new Intent();
                    data.putExtra(CommonField.ADDRESS, JSON.toJSONString(adapter.getSelectPostion()));
                    setResult(RESULT_OK, data);
                }
                finish();

            } else { // 修改房间地址的方式
                if (adapter != null && adapter.getSelectPostion() != null) {
                    Address address = new Address();
                    address.setName(adapter.getSelectPostion().getTitle());
                    address.setAddress(adapter.getSelectPostion().getAddress());
                    if (adapter.getSelectPostion().getLocation() != null) {
                        address.setLatitude(adapter.getSelectPostion().getLocation().getLat());
                        address.setLongitude(adapter.getSelectPostion().getLocation().getLng());
                    }
                    if (adapter.getSelectPostion().getAd_info() != null) {
                        address.setCity(adapter.getSelectPostion().getAd_info().getCity());
                    }
                    HttpRequest.Companion.getInstance().modifyFamily(familyId, familyName,
                            JSON.toJSONString(address), new MyCallback() {
                                @Override
                                public void fail(@org.jetbrains.annotations.Nullable String msg, int reqCode) {
                                    T.show(msg);
                                }

                                @Override
                                public void success(@NotNull BaseResponse response, int reqCode) {
                                    if (response.isSuccess()) {
                                        Intent data = new Intent();
                                        data.putExtra(CommonField.ADDRESS, JSON.toJSONString(adapter.getSelectPostion()));
                                        setResult(RESULT_OK, data);
                                        finish();
                                    } else {
                                        T.show(response.getMsg());
                                    }
                                }
                            });
                }
            }

        } else if (v.getId() == R.id.iv_reset_loaction) {
            autoLocate(null);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CommonField.SELECT_MAP_POSTION_REQ_CODE && resultCode == Activity.RESULT_OK && data != null) {
            String extraInfo = data.getStringExtra(CommonField.EXTRA_INFO);
            Address selectedPostion = JSON.parseObject(extraInfo, Address.class);
            LatLng target = new LatLng(selectedPostion.getLatitude(), selectedPostion.getLongitude());
            startPostion = new LatLng(target);
            mTencentMap.moveCamera(CameraUpdateFactory.newLatLng(target));
            postions.clear();
            pageSize = 1;
            adapter.setSelectPos(0);
            linearLayoutManager.scrollToPosition(0);
            getPostionInfo(target, pageSize, selectedPostion);
        }
    }
}

