package com.tenext.demo.fragment

import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.tenext.auth.IoTAuth
import com.tenext.auth.callback.DeviceCallback
import com.tenext.auth.callback.MyCallback
import com.tenext.auth.consts.RequestCode
import com.tenext.auth.entity.Device
import com.tenext.auth.entity.Room
import com.tenext.auth.response.BaseResponse
import com.tenext.auth.response.RoomListResponse
import com.tenext.demo.App
import com.tenext.demo.R
import com.tenext.demo.adapter.DeviceAdapter
import com.tenext.demo.adapter.FamilyAdapter
import com.tenext.demo.adapter.RoomAdapter
import com.tenext.auth.response.FamilyListResponse
import com.tenext.demo.activity.AddDeviceActivity
import com.tenext.demo.activity.ControlPanelActivity
import com.tenext.demo.adapter.OnItemListener
import com.tenext.demo.holder.BaseHolder
import com.tenext.demo.view.MyDivider
import kotlinx.android.synthetic.main.fragment_device.*

class DeviceFragment : BaseFragment(), MyCallback {

    private lateinit var familyAdapter: FamilyAdapter
    private lateinit var roomAdapter: RoomAdapter
    private lateinit var deviceAdapter: DeviceAdapter

    override fun getContentView(): Int {
        return R.layout.fragment_device
    }

    override fun startHere(view: View) {
        familyAdapter = FamilyAdapter(context!!, IoTAuth.familyList)
        rv_family.adapter = familyAdapter
        rv_family.layoutManager =
            LinearLayoutManager(context!!, LinearLayoutManager.HORIZONTAL, false)

        roomAdapter = RoomAdapter(context!!, IoTAuth.roomList)
        rv_room.adapter = roomAdapter
        rv_room.layoutManager =
            LinearLayoutManager(context!!, LinearLayoutManager.HORIZONTAL, false)

        deviceAdapter = DeviceAdapter(context!!, IoTAuth.deviceList)
        rv_device.adapter = deviceAdapter
        rv_device.layoutManager = LinearLayoutManager(context!!)
        val myDivider = MyDivider(dp2px(16), dp2px(16), dp2px(16))
        rv_device.addItemDecoration(myDivider)

        setListener()
    }

    override fun onResume() {
        super.onResume()
        refreshFamilyList()
    }

    private fun setListener() {
        tv_add_device.setOnClickListener {
            jumpActivity(AddDeviceActivity::class.java)
        }
        familyAdapter.setOnItemListener(object : OnItemListener {
            override fun onItemClick(holder: BaseHolder<*>, clickView: View, position: Int) {
                App.data.tabFamily(position)
                showFamily()
                refreshRoomList()
                showRoom()
            }
        })
        roomAdapter.setOnItemListener(object : OnItemListener {
            override fun onItemClick(holder: BaseHolder<*>, clickView: View, position: Int) {
                App.data.tabRoom(position)
                showRoom()
                Log.e("onItemClick", "切换房间更新设备列表")
                refreshDeviceList()
                showDevice()
            }
        })
        deviceAdapter.setOnItemListener(object : OnItemListener {
            override fun onItemClick(holder: BaseHolder<*>, clickView: View, position: Int) {
                put("device", IoTAuth.deviceList[position])
                put("share", App.data.getCurrentFamily().Role != 1)
                jumpActivity(ControlPanelActivity::class.java)
            }
        })
    }

    private fun refreshFamilyList() {
        IoTAuth.familyList.clear()
        IoTAuth.familyImpl.familyList(0, this)
    }

    private fun doNext() {
        //没有家庭时，先创建
        if (IoTAuth.familyList.isEmpty()) {
            IoTAuth.familyImpl.createFamily("我的家", "", this)
        } else {
            refreshRoomList()
        }
    }

    private fun refreshRoomList() {
        //有家庭获取房间列表,第一个房间为所有设备，也是默认房间
        IoTAuth.roomList.clear()
        //先添加一个默认房间
        val room = Room()
        room.RoomName = "所有设备"
        IoTAuth.roomList.add(room)
        IoTAuth.familyImpl.roomList(App.data.getCurrentFamily().FamilyId, 0, this)
    }

    private fun refreshDeviceList() {
        IoTAuth.deviceList.clear()
        App.data.getCurrentFamily().run {
            IoTAuth.deviceImpl.deviceList(
                FamilyId,
                App.data.getCurrentRoom().RoomId,
                0,
                object : DeviceCallback {
                    //获取到设备列表时回调（新增设备无在线状态）
                    override fun success(deviceList: List<Device>) {
                        showDevice()
                    }

                    //内部获取到设备在线状态时回调
                    override fun onlineUpdate() {
                        showDevice()
                    }

                    override fun fail(message: String) {
                        Log.e("deviceList", message)
                    }
                })
        }
    }

    private fun showFamily() {
        activity?.runOnUiThread {
            familyAdapter.notifyDataSetChanged()
        }
    }

    private fun showRoom() {
        activity?.runOnUiThread {
            roomAdapter.notifyDataSetChanged()
        }
    }

    private fun showDevice() {
        activity?.runOnUiThread {
            deviceAdapter.notifyDataSetChanged()
        }
    }


    override fun fail(msg: String?, reqCode: Int) {
    }

    override fun success(response: BaseResponse, reqCode: Int) {
        if (response.isSuccess()) {
            when (reqCode) {
                RequestCode.family_list -> {
                    response.parse(FamilyListResponse::class.java)?.run {
                        IoTAuth.familyList.addAll(FamilyList)
                        showFamily()
                        //根据家庭列表决定下一步
                        doNext()
                    }
                }
                RequestCode.create_family -> {
                    refreshFamilyList()
                }
                RequestCode.room_list -> {
                    response.parse(RoomListResponse::class.java)?.run {
                        IoTAuth.roomList.addAll(Roomlist)
                        showRoom()
                        Log.e("success", "首次更新房间列表，更新设备列表")
                        refreshDeviceList()
                    }
                }
            }
        } else {
            Log.e("DeviceFragment", response.msg)
        }
    }
}