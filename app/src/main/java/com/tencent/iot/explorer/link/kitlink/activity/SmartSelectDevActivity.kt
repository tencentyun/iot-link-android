package com.tencent.iot.explorer.link.kitlink.activity

import android.text.TextUtils
import android.util.Log
import androidx.fragment.app.Fragment
import com.alibaba.fastjson.JSON
import com.tencent.iot.explorer.link.App
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.T
import com.tencent.iot.explorer.link.core.auth.entity.RoomEntity
import com.tencent.iot.explorer.link.core.auth.response.BaseResponse
import com.tencent.iot.explorer.link.core.auth.response.RoomListResponse
import com.tencent.iot.explorer.link.core.log.L
import com.tencent.iot.explorer.link.kitlink.consts.CommonField
import com.tencent.iot.explorer.link.kitlink.entity.RouteType
import com.tencent.iot.explorer.link.kitlink.fragment.SelDeviceFragment
import com.tencent.iot.explorer.link.kitlink.util.HttpRequest
import com.tencent.iot.explorer.link.kitlink.util.MyCallback
import com.tencent.iot.explorer.link.kitlink.util.RequestCode
import kotlinx.android.synthetic.main.activity_device_category.*
import kotlinx.android.synthetic.main.activity_smart_sel_dev.*
import kotlinx.android.synthetic.main.activity_smart_sel_dev.vtab_device_category
import kotlinx.android.synthetic.main.menu_back_layout.*
import q.rorbin.verticaltablayout.VerticalTabLayout
import q.rorbin.verticaltablayout.widget.TabView


class SmartSelectDevActivity : BaseActivity(), MyCallback, VerticalTabLayout.OnTabSelectedListener {

    private var roomListEnd = false
    @Volatile
    private var roomList = ArrayList<RoomEntity>()
    private var roomTotal = 0
    private var startType = RouteType.MANUAL_TASK_ROUTE

    override fun getContentView(): Int {
        return R.layout.activity_smart_sel_dev
    }

    override fun initView() {
        tv_title.setText(R.string.select_dev)
        startType = intent.getIntExtra(CommonField.EXTRA_ROUTE_TYPE, RouteType.MANUAL_TASK_ROUTE)
        roomList.add(RoomEntity())  // 默认的全部设备
        loadRoomList()
    }

    private fun loadRoomList() {
        if (roomListEnd) return
        HttpRequest.instance.roomList(App.data.getCurrentFamily().FamilyId, 0, this)
    }

    private fun generateFragments() : List<Fragment>{
        val fragmentList = arrayListOf<Fragment>()
        if (roomList != null) {
            for (i in 0 until roomList.size) {
                val fragment = SelDeviceFragment(this, startType, roomList.get(i).RoomId)
                fragmentList.add(fragment)
            }
        }
        return fragmentList
    }

    override fun setListener() {
        iv_back.setOnClickListener { finish() }
        vtab_device_category.addOnTabSelectedListener(this)
    }

    override fun fail(msg: String?, reqCode: Int) {
        T.show(msg)
    }

    override fun success(response: BaseResponse, reqCode: Int) {
        when(reqCode)
        {
            RequestCode.room_list -> {
                if (response.isSuccess()) {
                    response.parse(RoomListResponse::class.java)?.run {
                        if (Roomlist != null) {
                            roomList.addAll(Roomlist!!)
                        }
                        if (Total >= 0) {
                            roomTotal = Total
                        }
                        roomListEnd = roomList.size >= roomTotal
                        L.e("roomList=${JSON.toJSONString(roomList)}")
                        //还有数据
                        if (!roomListEnd) {
                            loadRoomList()
                        } else {        // 没有数据了，开始加载所有房间的列表数据
                            val adapter = DeviceCategoryActivity.MyTabAdapter()
                            for (item in roomList) {
                                if (TextUtils.isEmpty(item.RoomId)) {
                                    adapter.titleList.add(getString(R.string.all_devices))
                                } else {
                                    adapter.titleList.add(item.RoomName)
                                }
                            }
                            vtab_device_category.setupWithFragment(
                                supportFragmentManager,
                                R.id.devce_fragment_container,
                                generateFragments(),
                                adapter
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onTabReselected(tab: TabView?, position: Int) {

    }

    override fun onTabSelected(tab: TabView?, position: Int) {
        for (i in 0 until roomList.size) {
            if (i != position) {
                vtab_device_category.getTabAt(i).setBackgroundColor(resources.getColor(R.color.gray_F5F5F5))
            }
        }
        tab?.setBackground(R.drawable.tab)
    }
}