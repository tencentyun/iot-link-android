package com.tencent.iot.explorer.link.kitlink.fragment

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Handler
import android.text.TextUtils
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONException
import com.alibaba.fastjson.JSONObject
import com.google.android.material.appbar.AppBarLayout
import com.scwang.smart.refresh.footer.ClassicsFooter
import com.scwang.smart.refresh.header.ClassicsHeader
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.scwang.smart.refresh.layout.listener.OnRefreshLoadMoreListener
import com.tencent.iot.explorer.link.App
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.T
import com.tencent.iot.explorer.link.core.auth.IoTAuth
import com.tencent.iot.explorer.link.core.auth.callback.MyCallback
import com.tencent.iot.explorer.link.core.auth.entity.*
import com.tencent.iot.explorer.link.core.auth.message.MessageConst
import com.tencent.iot.explorer.link.core.auth.message.payload.Payload
import com.tencent.iot.explorer.link.core.auth.response.BaseResponse
import com.tencent.iot.explorer.link.core.auth.response.FamilyInfoResponse
import com.tencent.iot.explorer.link.core.auth.socket.callback.PayloadMessageCallback
import com.tencent.iot.explorer.link.core.auth.util.JsonManager
import com.tencent.iot.explorer.link.core.log.L
import com.tencent.iot.explorer.link.customview.dialog.DevModeSetDialog
import com.tencent.iot.explorer.link.customview.dialog.entity.KeyBooleanValue
import com.tencent.iot.explorer.link.customview.dialog.MoreOptionDialog
import com.tencent.iot.explorer.link.customview.dialog.PermissionDialog
import com.tencent.iot.explorer.link.customview.dialog.entity.DevOption
import com.tencent.iot.explorer.link.customview.dialog.entity.OptionMore
import com.tencent.iot.explorer.link.kitlink.activity.ControlPanelActivity
import com.tencent.iot.explorer.link.kitlink.activity.DevicePanelActivity
import com.tencent.iot.explorer.link.kitlink.activity.FamilyActivity
import com.tencent.iot.explorer.link.kitlink.activity.MainActivity
import com.tencent.iot.explorer.link.kitlink.adapter.RoomDevAdapter
import com.tencent.iot.explorer.link.kitlink.adapter.RoomsAdapter
import com.tencent.iot.explorer.link.kitlink.consts.CommonField
import com.tencent.iot.explorer.link.kitlink.entity.ModeInt
import com.tencent.iot.explorer.link.kitlink.entity.ProdConfigDetailEntity
import com.tencent.iot.explorer.link.kitlink.entity.WeatherInfo
import com.tencent.iot.explorer.link.kitlink.response.ProductsConfigResponse
import com.tencent.iot.explorer.link.kitlink.util.*
import com.tencent.iot.explorer.link.mvp.IPresenter
import com.tencent.iot.explorer.link.mvp.presenter.HomeFragmentPresenter
import com.tencent.iot.explorer.link.mvp.view.HomeFragmentView
import com.tencent.iot.explorer.link.rtc.model.RoomKey
import com.tencent.iot.explorer.link.core.utils.Utils
import kotlinx.android.synthetic.main.fragment_main.*
import kotlinx.android.synthetic.main.header.*
import kotlinx.android.synthetic.main.inside_fixed_bar.*
import kotlinx.android.synthetic.main.title_with_family.*

class HomeFragment : BaseFragment(), HomeFragmentView, MyCallback, PayloadMessageCallback {

    private lateinit var presenter: HomeFragmentPresenter
    var popupListener: PopupListener? = null
    private var devList: ArrayList<DeviceEntity> = ArrayList()
    private var roomDevAdapter: RoomDevAdapter? = null
    private var shareDevList: ArrayList<DeviceEntity> = ArrayList()
    private var roomShareDevAdapter: RoomDevAdapter? = null
    private var roomList: ArrayList<RoomEntity> = ArrayList()
    private var roomsAdapter: RoomsAdapter? = null
    private var handler = Handler()
    private var permissionDialog: PermissionDialog? = null
    private var requestCameraPermission = false
    private var requestStoragePermission = false
    private var requestRecordAudioPermission = false
    private var deviceListEnd = false
    private var shareDeviceListEnd = false

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
        registBrodcast()
        showWeather(false)
        onHiddenChanged(false)
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            IoTAuth.addDeviceStatusCallback(this)
            if (App.data.refresh) { //更新数据
                requestData()
            } else {    //更新界面
                showData()
            }
        }
    }

    private fun registBrodcast() {
        var broadcastManager = LocalBroadcastManager.getInstance(context!!)
        var intentFilter = IntentFilter()
        intentFilter.addAction("android.intent.action.CART_BROADCAST")
        var recevier = object: BroadcastReceiver(){
            override fun onReceive(context: Context?, intent: Intent?) {
                var refreshTag = intent?.getIntExtra(CommonField.EXTRA_REFRESH, 0);
                if (refreshTag != 0){
                    if (App.data.refresh) { //更新数据
                        requestData()
                    }
                }
            }
        }
        broadcastManager.registerReceiver(recevier, intentFilter);
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
    }

    private fun loadCurrentWeather() {
        HttpRequest.instance.familyInfo(App.data.getCurrentFamily().FamilyId, this)
    }

    private fun formateWeather(weatherText: String): String {
        var testTag = weatherText.toLowerCase()
        if (testTag.contains(getString(R.string.sunny_tag)) ||
            testTag.contains("clear")) {
            return "sunny"
        } else if (testTag.contains(getString(R.string.cloud_tag))
                || testTag.contains(getString(R.string.cloud_b_tag))) {
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
                showWeather(true)
            }
        }

        override fun onWeatherFailed(reason: Int) {
            handler.post {
                showWeather(false)
            }
        }
    }

    private fun showWeather(show: Boolean) {
        if (show) {
            layout_space?.visibility = View.VISIBLE
            layout_2_set_location?.visibility = View.GONE
            weather_iv?.visibility = View.VISIBLE
        } else {
            layout_space?.visibility = View.INVISIBLE
            layout_2_set_location?.visibility = View.VISIBLE
            weather_iv?.visibility = View.GONE
        }
    }

    private fun showData() {
        presenter.model?.let {
            showFamily()
            showRoomList()
        }
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
            override fun onLoadMore(refreshLayout: RefreshLayout) {}
            override fun onRefresh(refreshLayout: RefreshLayout) {
                refreshLayout.finishRefresh()
                App.data.resetRefreshLevel() // 仅刷新设备列表
                requestData()
                loadCurrentWeather()
            }
        })
        layout_2_set_location.setOnClickListener {
            put("family", App.data.getCurrentFamily())
            jumpActivity(FamilyActivity::class.java)
        }
    }

    var onItemClickedListener = object: RoomDevAdapter.OnItemClicked {
        override fun onItemClicked(pos: Int, dev: DeviceEntity) {
            put("device", dev)
            val productList  = arrayListOf<String>()
            productList.add(dev.ProductId)
            HttpRequest.instance.getProductsConfig(productList, this@HomeFragment)
        }

        override fun onSwitchClicked(pos: Int, dev: DeviceEntity, shortCut: ProductUIDevShortCutConfig?) {
            if (dev == null || shortCut == null) return

            // 存在数据内容的
            for (devDataEntity in dev.deviceDataList) {
                if (devDataEntity.id == shortCut.powerSwitch) {
                    switchStatus(dev, shortCut.powerSwitch, devDataEntity.value)
                    return
                }
            }

            var devData = DeviceDataEntity()
            devData.id = shortCut.powerSwitch
            devData.value = "0"
            dev.deviceDataList.add(devData)
            switchStatus(dev, shortCut.powerSwitch, devData.value)

        }

        override fun onMoreClicked(pos: Int, dev: DeviceEntity, shortCut: ProductUIDevShortCutConfig?) {
            if (dev == null || shortCut == null || shortCut.devModeInfos == null ||
                    shortCut.devModeInfos.size <= 0) {
                return
            }

            var optionMore = convertOptionMore(dev, shortCut)
            var moreOptionDialog = MoreOptionDialog(this@HomeFragment.context, optionMore)
            moreOptionDialog.show()
            moreOptionDialog.setOnDismisListener(object: MoreOptionDialog.OnDismisListener {
                override fun onDismissed() {}
                override fun onItemClicked(pos: Int, devOption: DevOption?) {
                    if (devOption == null) return
                    if (devOption.type == DevOption.TYPE_BAR) {
                        devOption.modeInt!!.ifInteger = devOption.modeInt!!.type == "int"
                        showNumDialog(dev, devOption)
                    } else if (devOption.type == DevOption.TYPE_LIST) {
                        showMapDialog(dev, devOption)
                    }
                }

                override fun onGoClicked() {
                    put("device", dev)
                    val productList  = arrayListOf<String>()
                    productList.add(dev.ProductId)
                    HttpRequest.instance.getProductsConfig(productList, this@HomeFragment)
                }
            })
        }
    }

    private fun showMapDialog(dev: DeviceEntity, devOption: DevOption) {
        if (devOption == null || devOption.mapJson == null) return
        var keyBooleanValues = ArrayList<KeyBooleanValue>()
        var mapJson = devOption.mapJson

        var startIndex = -1
        var i = 0
        for (key in mapJson!!.keys) {
            var keyBooleanValue = KeyBooleanValue()
            keyBooleanValue.key = key
            keyBooleanValue.value = mapJson[key].toString()
            keyBooleanValues.add(keyBooleanValue)
            if (devOption.key == keyBooleanValue.key) {  // 当对应界面存在进度值时候，使用存在的进度值做数据
                startIndex = i
            }
            i++
        }
        var dialog = DevModeSetDialog(this@HomeFragment.context, keyBooleanValues, devOption.optionName, startIndex)
        dialog.show()
        dialog.setOnDismisListener(object : DevModeSetDialog.OnDismisListener{
            override fun onSaveClicked() {
                if (dialog.currentIndex >= 0) {
                    var value = keyBooleanValues.get(dialog.currentIndex).key
                    updateDev(dev, devOption, value)
                }
            }

            override fun onCancelClicked() {}
        })
    }

    private fun updateDev(dev: DeviceEntity, devOption: DevOption, value: String) {
        var data = "{\"${devOption.id}\":\"$value\"}"
        HttpRequest.instance.controlDevice(dev.ProductId, dev.DeviceName, data, object: MyCallback{
            override fun fail(msg: String?, reqCode: Int) {
                T.show(msg?:"")
            }

            override fun success(response: BaseResponse, reqCode: Int) {
                if (response.isSuccess()) {
                    for (devData in dev.deviceDataList) {
                        if (devData.id == devOption.id) {
                            devData.value = value
                            roomDevAdapter?.notifyDataSetChanged()
                            roomShareDevAdapter?.notifyDataSetChanged()
                            break
                        }
                    }
                } else {
                    T.show(response.msg)
                }
            }
        })
    }

    private fun showNumDialog(dev: DeviceEntity, devOption: DevOption) {
        if (devOption == null || devOption.modeInt == null) return
        var dialog = DevModeSetDialog(this@HomeFragment.context, devOption.optionName, devOption.modeInt)
        dialog.show()
        dialog.setOnDismisListener(object : DevModeSetDialog.OnDismisListener{
            override fun onSaveClicked() {
                var value = ""
                if (!devOption.modeInt!!.ifInteger) {
                    var len = w_length(devOption!!.modeInt!!.step)
                    value = String.format("%.${len}f", dialog.progress)
                } else {
                    value = dialog.progress.toInt().toString()
                }
                updateDev(dev, devOption, value)
            }

            override fun onCancelClicked() {}
        })
    }

    fun w_length(num: Float): Int {
        var len = 1
        val str = num.toString()
        val parts = str.split(".")
        if (parts != null && parts.size == 2) {
            for (i in parts[1].length - 1 downTo 1) {
                if (parts[1][i].toString() != "0") {
                    len = i + 1
                    break
                }
            }
        }
        if (len <= 0) {
            len = 1
        }
        return len
    }

    private fun convertOptionMore(dev: DeviceEntity, shortCut: ProductUIDevShortCutConfig) : OptionMore {
        var optionMore = OptionMore()
        optionMore.title = dev.getAlias()
        for (devModeInfo in shortCut.devModeInfos) {
            var showFlag = false
            var res = ""
            for (resConfig in shortCut.shortcut) {
                if (devModeInfo.id == resConfig.id) {
                    if (resConfig.ui != null && resConfig.ui!!.visible) {
                        res = resConfig.ui!!.icon
                    }
                    showFlag = true
                    break
                }
            }

            if(!showFlag) continue

            var devOption = DevOption()
            devOption.id = devModeInfo.id
            devOption.res = res
            var type = devModeInfo.define!!.get("type")
            if (type == "bool" || type == "enum" || type == "stringenum") {
                var mapJson = devModeInfo.define!!.getJSONObject("mapping")
                devOption.mapJson = mapJson
                devOption.type = DevOption.TYPE_LIST
            } else if (type == "int" || type == "float") {
                var modeInt = JSON.parseObject(devModeInfo.define!!.toJSONString(), ModeInt::class.java)
                modeInt.ifInteger = type == "int"
                if (modeInt.start < modeInt.min) {
                    modeInt.start = modeInt.min
                }
                devOption.modeInt = modeInt
                devOption.type = DevOption.TYPE_BAR
            }
            devOption.optionName = devModeInfo.name

            var hasValue = false
            for (devData in dev.deviceDataList) {
                if (devModeInfo.id == devData.id) {
                    var type = devModeInfo.define!!.get("type")
                    if (type == "bool" || type == "enum" || type == "stringenum") {
                        var mapJson = devModeInfo.define!!.getJSONObject("mapping")
                        if (!TextUtils.isEmpty(devData.value)) {
                            devOption.value = mapJson.getString(devData.value)
                            devOption.key = devData.value
                        }
                    } else if (type == "int" || type == "float") {
                        var modeInt = JSON.parseObject(devModeInfo.define!!.toJSONString(), ModeInt::class.java)
                        if (devData.value.toDoubleOrNull() != null) {
                            devOption.value = devData.value + modeInt.unit
                            modeInt.start = devData.value.toFloat()
                        }
                        if (modeInt.start < modeInt.min) {
                            modeInt.start = modeInt.min
                        }
                        devOption.modeInt = modeInt
                    }
                    hasValue = true
                    break
                }
            }
            if (!hasValue) {
                var initData = DeviceDataEntity()
                initData.id = devModeInfo.id
                if (type == "bool" || type == "enum" || type == "stringenum") {

                } else if (type == "int" || type == "float") {

                }
                dev.deviceDataList.add(initData)
            }

            optionMore.options.add(devOption)
        }
        return optionMore
    }

    fun switchStatus(dev: DeviceEntity, id: String, currentStaus: String) {
        L.d("上报数据:id=$id value=$currentStaus")
        var status = ""
        if (currentStaus == "1") {
            status = "0"
        } else {
            status = "1"
        }
        var data = "{\"$id\":\"$status\"}"
        HttpRequest.instance.controlDevice(dev.ProductId, dev.DeviceName, data, object : MyCallback{
            override fun fail(msg: String?, reqCode: Int) {
                T.show(msg?:"")
            }

            override fun success(response: BaseResponse, reqCode: Int) {
                if (response.isSuccess()) {
                    for (devDataEntity in dev.deviceDataList) {
                        if (devDataEntity.id == id) {
                            devDataEntity.value = status
                            break
                        }
                    }
                    roomDevAdapter?.notifyDataSetChanged()
                    roomShareDevAdapter?.notifyDataSetChanged()
                } else {
                    T.show(response.msg)
                }
            }
        })
    }

    // 切换家庭
    fun tabFamily(position: Int) {
        presenter.tabFamily(position)
        roomsAdapter?.selectPos = 0
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
        if (roomList == null) return

        roomList.clear()
        roomList.addAll(App.data.roomList)
        for (i in 0 until roomList.size) {
            if (TextUtils.isEmpty(roomList.get(i).RoomName)) {
                roomList[i].RoomName = getString(R.string.all_dev)
            }
        }
        App.data.roomList?.let {
            for (i in 0 until it.size) {
                if (App.data.roomList.isSelect(i)) {
                    roomsAdapter?.selectPos = i
                }
            }
        }
        roomsAdapter?.notifyDataSetChanged()

        loadCurrentWeather()
    }

    // 显示设备列表
    override fun showDeviceList(deviceSize: Int, roomId: String, deviceListEnd: Boolean, shareDeviceListEnd: Boolean) {
        if (deviceListEnd) {
            devList.clear()
            devList.addAll(presenter.getIModel(this).deviceList)
        }

        if (shareDeviceListEnd) {
            shareDevList.clear()
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
        requestCameraPermission = false
        requestRecordAudioPermission = false
        requestStoragePermission = false

        if (deviceListEnd) {
            this.deviceListEnd = true
        }
        if (shareDeviceListEnd) {
            this.shareDeviceListEnd = true
        }
        if (this.deviceListEnd && this.shareDeviceListEnd) {
            requestPermission()
        }
    }

    private fun requestPermission() {

        if ((shareDevList.size > 0 || devList.size > 0) && permissionDialog == null) {
            if (!checkPermissions(Manifest.permission.CAMERA) && !requestCameraPermission) {
                // 查看请求camera权限的时间是否大于48小时
                var cameraJsonString = Utils.getStringValueFromXml(T.getContext(), CommonField.PERMISSION_CAMERA, CommonField.PERMISSION_CAMERA)
                var cameraJson: JSONObject? = JSONObject.parse(cameraJsonString) as JSONObject?
                val lasttime = cameraJson?.getLong(CommonField.PERMISSION_CAMERA)
                if (lasttime != null && lasttime > 0 && System.currentTimeMillis() / 1000 - lasttime < 48*60*60) {
                    requestCameraPermission = true
                    T.show(getString(R.string.permission_of_camera_refuse))
                    return
                }
                permissionDialog = PermissionDialog(App.activity, R.mipmap.permission_camera ,getString(R.string.permission_camera_lips), getString(R.string.permission_camera_trtc))
                permissionDialog!!.show()
                requestPermissions(arrayOf(Manifest.permission.CAMERA), 103)
                requestCameraPermission = true

                // 记录请求camera权限的时间
                var json = JSONObject()
                json.put(CommonField.PERMISSION_CAMERA, System.currentTimeMillis() / 1000)
                Utils.setXmlStringValue(T.getContext(), CommonField.PERMISSION_CAMERA, CommonField.PERMISSION_CAMERA, json.toJSONString())
                return
            }
            requestCameraPermission = true
            if (!checkPermissions(Manifest.permission.RECORD_AUDIO) && !requestRecordAudioPermission) {
                // 查看请求mic权限的时间是否大于48小时
                var micJsonString = Utils.getStringValueFromXml(T.getContext(), CommonField.PERMISSION_MIC, CommonField.PERMISSION_MIC)
                var micJson: JSONObject? = JSONObject.parse(micJsonString) as JSONObject?
                val lasttime = micJson?.getLong(CommonField.PERMISSION_MIC)
                if (lasttime != null && lasttime > 0 && System.currentTimeMillis() / 1000 - lasttime < 48*60*60) {
                    requestRecordAudioPermission = true
                    T.show(getString(R.string.permission_of_camera_mic_refuse))
                    return
                }
                permissionDialog = PermissionDialog(App.activity, R.mipmap.permission_mic ,getString(R.string.permission_mic_lips), getString(R.string.permission_camera_trtc))
                permissionDialog!!.show()
                requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), 104)
                requestRecordAudioPermission = true

                // 记录请求mic权限的时间
                var json = JSONObject()
                json.put(CommonField.PERMISSION_MIC, System.currentTimeMillis() / 1000)
                Utils.setXmlStringValue(T.getContext(), CommonField.PERMISSION_MIC, CommonField.PERMISSION_MIC, json.toJSONString())
                return
            }
            requestRecordAudioPermission = true
            if ((!checkPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE) || !checkPermissions(Manifest.permission.READ_EXTERNAL_STORAGE)) && !requestStoragePermission) {
                // 查看请求storage权限的时间是否大于48小时
                var storageJsonString = Utils.getStringValueFromXml(T.getContext(), CommonField.PERMISSION_STORAGE, CommonField.PERMISSION_STORAGE)
                var storageJson: JSONObject? = JSONObject.parse(storageJsonString) as JSONObject?
                val lasttime = storageJson?.getLong(CommonField.PERMISSION_STORAGE)
                if (lasttime != null && lasttime > 0 && System.currentTimeMillis() / 1000 - lasttime < 48*60*60) {
                    requestStoragePermission = true
                    T.show(getString(R.string.permission_of_camera_mic_refuse))
                    return
                }
                permissionDialog = PermissionDialog(App.activity, R.mipmap.permission_album ,getString(R.string.permission_storage_lips), getString(R.string.permission_storage))
                permissionDialog!!.show()
                requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE), 105)
                requestStoragePermission = true

                // 记录请求storage权限的时间
                var json = JSONObject()
                json.put(CommonField.PERMISSION_STORAGE, System.currentTimeMillis() / 1000)
                Utils.setXmlStringValue(T.getContext(), CommonField.PERMISSION_STORAGE, CommonField.PERMISSION_STORAGE, json.toJSONString())
                return
            }
            requestStoragePermission = true
        }
    }

    private fun checkPermissions(permission: String): Boolean {
        if (App.activity?.let { ActivityCompat.checkSelfPermission(it, permission) } == PackageManager.PERMISSION_DENIED) {
            L.e(permission + "被拒绝")
            return false
        }
        L.e(permission + "已经申请成功")
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (permissions.contains(Manifest.permission.CAMERA) || permissions.contains(Manifest.permission.RECORD_AUDIO) || permissions.contains(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            permissionDialog?.dismiss()
            permissionDialog = null
            if (!requestCameraPermission || !requestRecordAudioPermission || !requestStoragePermission) {
                requestPermission()
            }
        }
    }

    override fun showDeviceOnline() {
        for (i in 0 until devList.size) {
            if (i >= presenter.getIModel(this).deviceList.size) {
                continue
            }
            devList.get(i).online = presenter.getIModel(this).deviceList.get(i).online
        }
        for (i in 0 until shareDevList.size) {
            if (i >= presenter.getIModel(this).shareDeviceList.size) {
                continue
            }
            shareDevList.get(i).online = presenter.getIModel(this).shareDeviceList.get(i).online
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

    override fun showDeviceShortCut(productConfigs: MutableMap<String, ProductUIDevShortCutConfig>) {
        roomDevAdapter?.shortCuts = productConfigs
        roomShareDevAdapter?.shortCuts = productConfigs
        roomDevAdapter?.notifyDataSetChanged()
        roomShareDevAdapter?.notifyDataSetChanged()
    }

    interface PopupListener {
        fun onPopupListener(familyList: List<FamilyEntity>)
    }

    override fun fail(msg: String?, reqCode: Int) {
        msg?.let { L.e(it) }
        when (reqCode) {
            RequestCode.family_info -> {
                showWeather(false)
            }
        }
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
                        var address: com.tencent.iot.explorer.link.kitlink.entity.Address? = null
                        try {
                            address = JSON.parseObject(Address, com.tencent.iot.explorer.link.kitlink.entity.Address::class.java)
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                        if (address != null) {
                            WeatherUtils.getWeatherInfoByLocation(address.latitude.toDouble(), address.longitude.toDouble(), weatherListener)
                        } else {
                            showWeather(false)
                        }
                    }
                } else {
                    showWeather(false)
                }
            }
        }
    }

    override fun payloadMessage(payload: Payload) {
        val jsonObject = org.json.JSONObject(payload.json)
        val action = jsonObject.getString(MessageConst.MODULE_ACTION)
        if (action == MessageConst.DEVICE_CHANGE) { //设备状态发生改变
            val paramsObject = jsonObject.getJSONObject(MessageConst.PARAM) as org.json.JSONObject
            val subType = paramsObject.getString(MessageConst.SUB_TYPE)
            val deviceId = paramsObject.getString(MessageConst.DEVICE_ID)
            if (subType == MessageConst.ONLINE) {
                presenter.updateDeviceStatus(deviceId, 1)
                handler.post {
                    roomDevAdapter?.notifyDataSetChanged()
                    roomShareDevAdapter?.notifyDataSetChanged()
                }
            } else if (subType == MessageConst.OFFLINE) {
                presenter.updateDeviceStatus(deviceId, 0)
                handler.post {
                    roomDevAdapter?.notifyDataSetChanged()
                    roomShareDevAdapter?.notifyDataSetChanged()
                }
            }
        }
    }
}