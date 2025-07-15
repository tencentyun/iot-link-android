package com.tencent.iot.explorer.link.demo.core.activity

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.tencent.iot.explorer.link.core.auth.IoTAuth
import com.tencent.iot.explorer.link.core.auth.callback.MyCallback
import com.tencent.iot.explorer.link.core.auth.entity.FamilyEntity
import com.tencent.iot.explorer.link.core.auth.entity.RoomEntity
import com.tencent.iot.explorer.link.core.auth.response.BaseResponse
import com.tencent.iot.explorer.link.core.auth.response.RoomListResponse
import com.tencent.iot.explorer.link.demo.BaseActivity
import com.tencent.iot.explorer.link.demo.R
import com.tencent.iot.explorer.link.demo.core.adapter.OnItemListener
import com.tencent.iot.explorer.link.demo.core.adapter.RoomListAdapter
import com.tencent.iot.explorer.link.demo.core.holder.BaseHolder
import com.tencent.iot.explorer.link.demo.common.log.L
import com.tencent.iot.explorer.link.demo.databinding.ActivityRoomListBinding

/**
 * 房间管理
 */
class RoomListActivity : BaseActivity<ActivityRoomListBinding>(), MyCallback {

    private lateinit var adapter: RoomListAdapter
    private val roomList = arrayListOf<RoomEntity>()

    override fun onResume() {
        refreshRoomList()
        super.onResume()
    }

    override fun getViewBinding(): ActivityRoomListBinding = ActivityRoomListBinding.inflate(layoutInflater)

    override fun initView() {
        with(binding) {
            roomListMenu.tvTitle.text = getString(R.string.room_manager)
            rvRoomList.layoutManager = LinearLayoutManager(this@RoomListActivity)
            adapter = RoomListAdapter(this@RoomListActivity, roomList)
            rvRoomList.adapter = adapter
        }
    }

    override fun setListener() {
        binding.roomListMenu.ivBack.setOnClickListener { finish() }
        binding.tvAddRoom.setOnClickListener {
            jumpActivity(AddRoomActivity::class.java)
        }
        adapter.setOnItemListener(object : OnItemListener {
            override fun onItemClick(holder: BaseHolder<*, *>, clickView: View, position: Int) {
                put("room", roomList[position])
                jumpActivity(RoomActivity::class.java)
            }
        })
    }

    /**
     *  获取家庭内房间列表
     */
    private fun refreshRoomList() {
        get<FamilyEntity>("family")?.run {
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
