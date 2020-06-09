package com.tenext.demo.activity

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.tenext.auth.IoTAuth
import com.tenext.auth.callback.MyCallback
import com.tenext.auth.entity.Family
import com.tenext.auth.entity.Room
import com.tenext.auth.response.BaseResponse
import com.tenext.auth.response.RoomListResponse
import com.tenext.demo.R
import com.tenext.demo.adapter.OnItemListener
import com.tenext.demo.adapter.RoomListAdapter
import com.tenext.demo.holder.BaseHolder
import com.tenext.demo.log.L
import kotlinx.android.synthetic.main.activity_room_list.*
import kotlinx.android.synthetic.main.menu_back_layout.*

/**
 * 房间管理
 */
class RoomListActivity : BaseActivity(), MyCallback {

    private lateinit var adapter: RoomListAdapter
    private val roomList = arrayListOf<Room>()

    override fun onResume() {
        refreshRoomList()
        super.onResume()
    }

    override fun getContentView(): Int {
        return R.layout.activity_room_list
    }

    override fun initView() {
        tv_title.text = getString(R.string.room_manager)
        rv_room_list.layoutManager = LinearLayoutManager(this)
        adapter = RoomListAdapter(this, roomList)
        rv_room_list.adapter = adapter
    }

    override fun setListener() {
        iv_back.setOnClickListener { finish() }
        tv_add_room.setOnClickListener {
            jumpActivity(AddRoomActivity::class.java)
        }
        adapter.setOnItemListener(object : OnItemListener {
            override fun onItemClick(holder: BaseHolder<*>, clickView: View, position: Int) {
                put("room", roomList[position])
                jumpActivity(RoomActivity::class.java)
            }
        })
    }

    /**
     *  获取家庭内房间列表
     */
    private fun refreshRoomList() {
        get<Family>("family")?.run {
            IoTAuth.familyImpl.roomList(FamilyId, 0, this@RoomListActivity)
        }
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
