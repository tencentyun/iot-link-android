package com.tencent.iot.explorer.link.demo.core.activity

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewbinding.ViewBinding
import com.tencent.iot.explorer.link.core.auth.IoTAuth
import com.tencent.iot.explorer.link.core.auth.callback.MyCallback
import com.tencent.iot.explorer.link.core.auth.entity.DeviceEntity
import com.tencent.iot.explorer.link.core.auth.entity.RoomEntity
import com.tencent.iot.explorer.link.core.auth.response.BaseResponse
import com.tencent.iot.explorer.link.core.auth.response.RoomListResponse
import com.tencent.iot.explorer.link.demo.App
import com.tencent.iot.explorer.link.demo.BaseActivity
import com.tencent.iot.explorer.link.demo.R
import com.tencent.iot.explorer.link.demo.core.adapter.BaseAdapter
import com.tencent.iot.explorer.link.demo.core.holder.BaseHolder
import com.tencent.iot.explorer.link.demo.common.log.L
import com.tencent.iot.explorer.link.demo.databinding.ActivitySelectRoomBinding
import com.tencent.iot.explorer.link.demo.databinding.ItemWeekRepeatBinding

/**
 * 选择房间
 */
class SelectRoomActivity : BaseActivity<ActivitySelectRoomBinding>(),MyCallback {

    private lateinit var selectedRoom: RoomEntity
    private lateinit var device: DeviceEntity
    private val roomList = arrayListOf<RoomEntity>()

    private val adapter = object : BaseAdapter(this, roomList) {
        override fun createHolder(parent: ViewGroup, viewType: Int): BaseHolder<*, ItemWeekRepeatBinding> {
            val holderBinding = ItemWeekRepeatBinding.inflate(layoutInflater)

            return object :
                BaseHolder<RoomEntity, ItemWeekRepeatBinding>(holderBinding) {
                override fun show(holder: BaseHolder<*, *>, position: Int) {
                    data.run {
                        holderBinding.tvWeekRepeatTitle.text = RoomName
                        holderBinding.ivWeekRepeatSelected.setImageResource(
                            if (isSelected(position)) R.mipmap.icon_checked
                            else R.mipmap.icon_unchecked
                        )
                        holderBinding.tvWeekRepeatCommit.visibility =
                            if (position == roomList.size - 1) View.VISIBLE else View.GONE
                    }

                    holderBinding.tvWeekRepeatCommit.setOnClickListener {
                        save()
                    }
                    holderBinding.tvWeekRepeatTitle.setOnClickListener {
                        selected(position)
                    }
                }
            }
        }
    }

    /**
     * 是否被选择
     */
    private fun isSelected(position: Int): Boolean {
        return selectedRoom.RoomId == roomList[position].RoomId
    }

    override fun getViewBinding(): ActivitySelectRoomBinding = ActivitySelectRoomBinding.inflate(layoutInflater)

    override fun initView() {
        with(binding) {
            menuSelectRoom.tvTitle.text = "选择房间"
            selectedRoom = get<RoomEntity>("select_room")!!
            device = get<DeviceEntity>("device")!!
            rvSelectRoom.layoutManager = LinearLayoutManager(this@SelectRoomActivity)
            rvSelectRoom.adapter = adapter
            refreshRoomList()
        }
    }

    override fun setListener() {
        binding.menuSelectRoom.ivBack.setOnClickListener { finish() }
    }

    /**
     *  获取家庭内房间列表
     */
    private fun refreshRoomList() {
        App.data.getCurrentFamily().run {
            IoTAuth.familyImpl.roomList(FamilyId, 0, this@SelectRoomActivity)
        }
    }


    /**
     * 保存
     */
    private fun save() {
        App.data.getCurrentFamily().run {
            IoTAuth.deviceImpl.changeRoom(
                FamilyId, selectedRoom.RoomId, device.ProductId, device.DeviceName,
                object : MyCallback {
                    override fun fail(msg: String?, reqCode: Int) {
                        L.e(msg ?: "")
                    }

                    override fun success(response: BaseResponse, reqCode: Int) {
                        if (response.isSuccess()) {
                            this@SelectRoomActivity.update("select_room", selectedRoom)
                            finish()
                        } else {
                            show(response.msg)
                        }
                    }
                })
        }
    }

    /**
     * 选择
     */
    private fun selected(position: Int) {
        selectedRoom = roomList[position]
        adapter.notifyDataSetChanged()
    }

    private fun showRoomList() {
        runOnUiThread {
            adapter.notifyDataSetChanged()
        }
    }

    override fun fail(msg: String?, reqCode: Int) {
        L.e(msg ?: "")
    }

    override fun success(response: BaseResponse, reqCode: Int) {
        if (response.isSuccess()) {
            roomList.clear()
            response.parse(RoomListResponse::class.java)?.let {
                it.Roomlist.run {
                    roomList.addAll(this)
                    showRoomList()
                }
            }
        }
    }

}
