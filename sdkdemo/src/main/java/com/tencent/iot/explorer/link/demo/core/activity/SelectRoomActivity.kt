package com.tencent.iot.explorer.link.demo.core.activity

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.tencent.iot.explorer.link.core.auth.IoTAuth
import com.tencent.iot.explorer.link.core.auth.callback.MyCallback
import com.tencent.iot.explorer.link.core.auth.entity.DeviceEntity
import com.tencent.iot.explorer.link.core.auth.entity.RoomEntity
import com.tencent.iot.explorer.link.core.auth.response.BaseResponse
import com.tencent.iot.explorer.link.core.auth.response.RoomListResponse
import com.tencent.iot.explorer.link.demo.App
import com.tencent.iot.explorer.link.core.demo.R
import com.tencent.iot.explorer.link.demo.core.adapter.BaseAdapter
import com.tencent.iot.explorer.link.demo.core.holder.BaseHolder
import com.tencent.iot.explorer.link.demo.common.log.L
import kotlinx.android.synthetic.main.activity_select_room.*
import kotlinx.android.synthetic.main.item_week_repeat.view.*
import kotlinx.android.synthetic.main.menu_back_layout.*

/**
 * 选择房间
 */
class SelectRoomActivity : BaseActivity(),MyCallback {

    private lateinit var selectedRoom: RoomEntity
    private lateinit var device: DeviceEntity
    private val roomList = arrayListOf<RoomEntity>()

    private val adapter = object : BaseAdapter(this, roomList) {
        override fun createHolder(parent: ViewGroup, viewType: Int): BaseHolder<*> {
            return object :
                BaseHolder<RoomEntity>(this@SelectRoomActivity, parent, R.layout.item_week_repeat) {
                override fun show(holder: BaseHolder<*>, position: Int) {
                    data.run {
                        itemView.tv_week_repeat_title.text = RoomName
                        itemView.iv_week_repeat_selected.setImageResource(
                            if (isSelected(position)) R.mipmap.icon_checked
                            else R.mipmap.icon_unchecked
                        )
                        itemView.tv_week_repeat_commit.visibility =
                            if (position == roomList.size - 1) View.VISIBLE else View.GONE
                    }
                    itemView.tv_week_repeat_commit.setOnClickListener {
                        save()
                    }
                    itemView.tv_week_repeat_title.setOnClickListener {
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

    override fun getContentView(): Int {
        return R.layout.activity_select_room
    }

    override fun initView() {
        tv_title.text = "选择房间"
        selectedRoom = get<RoomEntity>("select_room")!!
        device = get<DeviceEntity>("device")!!
        rv_select_room.layoutManager = LinearLayoutManager(this)
        rv_select_room.adapter = adapter
        refreshRoomList()
    }

    override fun setListener() {
        iv_back.setOnClickListener { finish() }
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
