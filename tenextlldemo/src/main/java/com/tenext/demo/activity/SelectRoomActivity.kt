package com.tenext.demo.activity

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.tenext.auth.IoTAuth
import com.tenext.auth.callback.MyCallback
import com.tenext.auth.entity.Device
import com.tenext.auth.entity.Room
import com.tenext.auth.response.BaseResponse
import com.tenext.auth.response.RoomListResponse
import com.tenext.demo.App
import com.tenext.demo.R
import com.tenext.demo.adapter.BaseAdapter
import com.tenext.demo.holder.BaseHolder
import com.tenext.demo.log.L
import kotlinx.android.synthetic.main.activity_select_room.*
import kotlinx.android.synthetic.main.item_week_repeat.view.*
import kotlinx.android.synthetic.main.menu_back_layout.*

/**
 * 选择房间
 */
class SelectRoomActivity : BaseActivity(),MyCallback {

    private lateinit var selectedRoom: Room
    private lateinit var device: Device
    private val roomList = arrayListOf<Room>()

    private val adapter = object : BaseAdapter(this, roomList) {
        override fun createHolder(parent: ViewGroup, viewType: Int): BaseHolder<*> {
            return object :
                BaseHolder<Room>(this@SelectRoomActivity, parent, R.layout.item_week_repeat) {
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
        selectedRoom = get<Room>("select_room")!!
        device = get<Device>("device")!!
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
