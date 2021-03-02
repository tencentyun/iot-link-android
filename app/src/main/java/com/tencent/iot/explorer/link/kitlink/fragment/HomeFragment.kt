package com.tencent.iot.explorer.link.kitlink.fragment

import android.os.Handler
import android.text.TextUtils
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alibaba.fastjson.JSON
import com.google.android.material.appbar.AppBarLayout
import com.scwang.smart.refresh.footer.ClassicsFooter
import com.scwang.smart.refresh.header.ClassicsHeader
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.scwang.smart.refresh.layout.listener.OnRefreshLoadMoreListener
import com.tencent.iot.explorer.link.App
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.T
import com.tencent.iot.explorer.link.core.auth.callback.MyCallback
import com.tencent.iot.explorer.link.core.auth.entity.DeviceEntity
import com.tencent.iot.explorer.link.core.auth.entity.FamilyEntity
import com.tencent.iot.explorer.link.core.auth.entity.RoomEntity
import com.tencent.iot.explorer.link.core.auth.response.BaseResponse
import com.tencent.iot.explorer.link.core.auth.response.FamilyInfoResponse
import com.tencent.iot.explorer.link.core.auth.util.JsonManager
import com.tencent.iot.explorer.link.core.log.L
import com.tencent.iot.explorer.link.kitlink.activity.ControlPanelActivity
import com.tencent.iot.explorer.link.kitlink.activity.DevicePanelActivity
import com.tencent.iot.explorer.link.kitlink.activity.MainActivity
import com.tencent.iot.explorer.link.kitlink.adapter.RoomDevAdapter
import com.tencent.iot.explorer.link.kitlink.adapter.RoomsAdapter
import com.tencent.iot.explorer.link.kitlink.entity.ProdConfigDetailEntity
import com.tencent.iot.explorer.link.kitlink.entity.WeatherInfo
import com.tencent.iot.explorer.link.kitlink.response.ProductsConfigResponse
import com.tencent.iot.explorer.link.kitlink.util.*
import com.tencent.iot.explorer.link.mvp.IPresenter
import com.tencent.iot.explorer.link.mvp.presenter.HomeFragmentPresenter
import com.tencent.iot.explorer.link.mvp.view.HomeFragmentView
import com.tencent.iot.explorer.link.rtc.model.RoomKey
import kotlinx.android.synthetic.main.fragment_main.*
import kotlinx.android.synthetic.main.header.*
import kotlinx.android.synthetic.main.inside_fixed_bar.*
import kotlinx.android.synthetic.main.title_with_family.*


class HomeFragment : BaseFragment(), HomeFragmentView, MyCallback {

    private lateinit var presenter: HomeFragmentPresenter
    var popupListener: PopupListener? = null
    private var devList: ArrayList<DeviceEntity> = ArrayList()
    private var roomDevAdapter: RoomDevAdapter? = null
    private var shareDevList: ArrayList<DeviceEntity> = ArrayList()
    private var roomShareDevAdapter: RoomDevAdapter? = null
    private var roomList: ArrayList<RoomEntity> = ArrayList()
    private var roomsAdapter: RoomsAdapter? = null
    private var handler = Handler()

    override fun getContentView(): Int {
        return R.layout.fragment_main
    }

    override fun getPresenter(): IPresenter? {
        return presenter
    }

    override fun startHere(view: View) {
        presenter = HomeFragmentPresenter(this)
        initView()
        setListener()
    }

    override fun onResume() {
        super.onResume()
        if (App.data.refresh) { //更新数据
            requestData()
        } else {    //更新界面
            showData()
        }
    }

    /**
     * 请求数据
     */
    private fun requestData() {
        when (App.data.getRefreshLevel()) {
            0 -> presenter.refreshFamilyList()
            1 -> presenter.refreshRoomList()
            2 -> presenter.refreshDeviceList()
        }
        //级别降为设备刷新
        App.data.resetRefreshLevel()
        loadCurrentWeather()
    }

    private fun loadCurrentWeather() {
        HttpRequest.instance.familyInfo(App.data.getCurrentFamily().FamilyId, this)
        WeatherUtils.getWeatherInfoByLocation(39.1, 116.4, weatherListener)
    }

    private fun formateWeather(weatherText: String): String {
        var testTag = weatherText.toLowerCase()
        if (testTag.contains(getString(R.string.sunny_tag)) ||
            testTag.contains("clear")) {
            return "sunny"
        } else if (testTag.contains(getString(R.string.cloud_tag))) {
            return "cloud"
        } else if (testTag.contains(getString(R.string.rainy_tag))) {
            return "rainy"
        } else if (testTag.contains(getString(R.string.snow_tag))) {
            return "snow"
        }
        return ""
    }

    private fun formateHumidity(humidity: String): String {
        var humidityInt = humidity.toIntOrNull()

        if (humidityInt == null) {
            return ""
        }

        if (humidityInt <= 40) {
            return getString(R.string.dry_tag)
        } else if (humidityInt > 40 && humidityInt <= 70) {
            return getString(R.string.comfortable_tag)
        } else {
            return getString(R.string.damp_tag)
        }
    }

    private var weatherListener = object : OnWeatherListener {
        override fun onWeatherSuccess(weatherInfo: WeatherInfo) {
            handler.post {
                tv_temperature.text = weatherInfo.temp
                tv_outside_humidity.text = getString(R.string.outside_humidity,
                    formateHumidity(weatherInfo.humidity))
                tv_outside_wind_dir.text = getString(R.string.outside_wind_dir, weatherInfo.windDir)
                tv_text.text = weatherInfo.text
                tv_location.text = weatherInfo.cityInfo?.name
                var tag = formateWeather(weatherInfo.text)
                if (!TextUtils.isEmpty(tag)) {
                    weather_iv.imageAssetsFolder = "lottie/" + tag
                    weather_iv.setAnimation("lottie/${tag}/${tag}.json")
                    weather_iv.playAnimation()
                }
            }
        }

        override fun onWeatherFailed(reason: Int) {

        }
    }

    /**
     * 更新界面
     */
    private fun showData() {
        presenter.model!!.run {
            showFamily()
            showRoomList()
            showDeviceList(App.data.deviceList.size, roomId, deviceListEnd, shareDeviceListEnd)
        }
        loadCurrentWeather()
    }

    private fun initView() {
        var devGridLayoutManager = GridLayoutManager(context, 2)
        roomDevAdapter = RoomDevAdapter(devList)
        grid_devs.setLayoutManager(devGridLayoutManager)
        grid_devs.setNestedScrollingEnabled(false)
        grid_devs.setAdapter(roomDevAdapter)

        var shareGridLayoutManager = GridLayoutManager(context, 2)
        roomShareDevAdapter = RoomDevAdapter(shareDevList)
        grid_share_devs.setLayoutManager(shareGridLayoutManager)
        grid_share_devs.setNestedScrollingEnabled(false)
        grid_share_devs.setAdapter(roomShareDevAdapter)

        var roomLayoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
        roomsAdapter = RoomsAdapter(roomList)
        lv_rooms.setLayoutManager(roomLayoutManager)
        lv_rooms.setAdapter(roomsAdapter)

        smart_refreshLayout.setEnableRefresh(true)
        smart_refreshLayout.setRefreshHeader(ClassicsHeader(context))
        smart_refreshLayout.setEnableLoadMore(false)
        smart_refreshLayout.setRefreshFooter(ClassicsFooter(context))
    }

    private fun setListener() {
        app_bar.addOnOffsetChangedListener(object : AppBarStateChangeListener() {
            override fun onStateChanged(appBarLayout: AppBarLayout, state: State, percent: Float) {
                weather_iv.alpha = percent
            }
        })
        tv_add_dev.setOnClickListener {
            var start = this.context as MainActivity
            start.jumpAddDevActivity()
        }
        left_layout.setOnClickListener {
            popupListener?.onPopupListener(App.data.familyList)
        }
        roomDevAdapter?.setOnItemClicked(onItemClickedListener)
        roomShareDevAdapter?.setOnItemClicked(onItemClickedListener)
        roomsAdapter?.setOnItemClicked(object: RoomsAdapter.OnItemClicked {
            override fun onItemClicked(pos: Int, dev: RoomEntity) {
                roomsAdapter?.selectPos = pos
                roomsAdapter?.notifyDataSetChanged()
                tabRoom(pos)
            }
        })
        smart_refreshLayout.setOnRefreshLoadMoreListener(object : OnRefreshLoadMoreListener {
            override fun onLoadMore(refreshLayout: RefreshLayout) {
                refreshLayout.finishLoadMore()
                presenter.loadDeviceList()
            }

            override fun onRefresh(refreshLayout: RefreshLayout) {
                refreshLayout.finishRefresh()
                presenter.refreshFamilyList()
            }
        })
    }

    var onItemClickedListener = object: RoomDevAdapter.OnItemClicked {
        override fun onItemClicked(pos: Int, dev: DeviceEntity) {
            put("device", dev)
            val productList  = arrayListOf<String>()
            productList.add(dev.ProductId)
            HttpRequest.instance.getProductsConfig(productList, this@HomeFragment)
        }
    }

    // 切换家庭
    fun tabFamily(position: Int) {
        presenter.tabFamily(position)
        showFamily()
    }

    // 切换房间
    fun tabRoom(position: Int) {
        presenter.tabRoom(position)
    }

    // 显示家庭名称
    override fun showFamily() {
        App.data.getCurrentFamily()?.let {
            tv_home_name.text = it.FamilyName
        }
    }

    // 显示房间列表
    override fun showRoomList() {
        roomList.clear()
        roomList.addAll(App.data.roomList)
        if (roomList == null) return

        for (i in 0 .. roomList.size - 1) {
            if (TextUtils.isEmpty(roomList.get(i).RoomName)) {
                roomList[i].RoomName = getString(R.string.all_dev)
            }
        }
        roomsAdapter?.notifyDataSetChanged()
    }

    // 显示设备列表
    override fun showDeviceList(deviceSize: Int, roomId: String, deviceListEnd: Boolean, shareDeviceListEnd: Boolean) {
        devList.clear()
        shareDevList.clear()
        if (deviceSize > 0) {
            devList.addAll(presenter.getIModel(this).deviceList)
            devList.removeAll(presenter.getIModel(this).shareDeviceList)
            shareDevList.addAll(presenter.getIModel(this).shareDeviceList)
        }

        roomDevAdapter?.notifyDataSetChanged()
        roomShareDevAdapter?.notifyDataSetChanged()
        tv_dev_title.setText(resources.getString(R.string.all_dev_num, devList.size.toString()))
        tv_share_dev_title.setText(resources.getString(R.string.all_share_dev_num, shareDevList.size.toString()))

        if (shareDevList.size <= 0) {
            layout_share_dev_title.visibility = View.GONE
        } else {
            layout_share_dev_title.visibility = View.VISIBLE
        }

        if (devList.size <= 0) {
            layout_dev_title.visibility = View.GONE
        } else {
            layout_dev_title.visibility = View.VISIBLE
        }

        if (shareDevList.size <= 0 && devList.size <= 0) {
            layout_no_dev_2_show.visibility = View.VISIBLE
        } else {
            layout_no_dev_2_show.visibility = View.GONE
        }
    }

    override fun showDeviceOnline() {
        for (i in 0 until devList.size) {
            devList.get(i).online = presenter.getIModel(this).deviceList.get(i).online
        }
        for (i in 0 until shareDevList.size) {
            if (i + devList.size >= presenter.getIModel(this).deviceList.size) {
                continue
            }
            shareDevList.get(i).online = presenter.getIModel(this).deviceList.get(i + devList.size).online
        }
        roomDevAdapter?.notifyDataSetChanged()
        roomShareDevAdapter?.notifyDataSetChanged()
    }

    /**
     * 被设备呼叫进入trtc房间通话
     */
    override fun enterRoom(room: RoomKey, deviceId: String) {
//        this.activity?.runOnUiThread {
//            if (room.callType == TRTCCalling.TYPE_VIDEO_CALL) {
//                TRTCVideoCallActivity.startBeingCall(this.activity, room, deviceId)
//            } else if (room.callType == TRTCCalling.TYPE_AUDIO_CALL) {
//                TRTCAudioCallActivity.startBeingCall(this.activity, room, deviceId)
//            }
//        }
    }

    interface PopupListener {
        fun onPopupListener(familyList: List<FamilyEntity>)
    }

    override fun fail(msg: String?, reqCode: Int) {
        msg?.let { L.e(it) }
    }

    override fun success(response: BaseResponse, reqCode: Int) {
        when (reqCode) {
            RequestCode.get_products_config -> {
                if (response.isSuccess()) {
                    response.parse(ProductsConfigResponse::class.java)?.run {
                        val config = JsonManager.parseJson(
                            Data[0].Config,
                            ProdConfigDetailEntity::class.java
                        )
                        if (config == null) {
                            jumpActivity(ControlPanelActivity::class.java)
                        } else {
                            val panelInfo = JSON.parseObject(config.Panel)
                            if (panelInfo != null && panelInfo["type"] == "h5") {
                                jumpActivity(DevicePanelActivity::class.java)
                            } else {
                                jumpActivity(ControlPanelActivity::class.java)
                            }
                        }
                    }
                } else {
                    T.show(response.msg)
                }
            }

            RequestCode.family_info -> {
                if (response.isSuccess()) {
                    response.parse(FamilyInfoResponse::class.java)?.Data?.run {

                    }
                }
            }
        }
    }
}