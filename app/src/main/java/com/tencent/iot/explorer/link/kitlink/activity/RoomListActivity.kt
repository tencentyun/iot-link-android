package com.tencent.iot.explorer.link.kitlink.activity

import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.core.auth.entity.FamilyEntity
import com.tencent.iot.explorer.link.core.auth.entity.RoomEntity
import com.tencent.iot.explorer.link.core.auth.response.BaseResponse
import com.tencent.iot.explorer.link.core.auth.response.RoomListResponse
import com.tencent.iot.explorer.link.core.log.L
import com.tencent.iot.explorer.link.kitlink.holder.RoomListFootHolder
import com.tencent.iot.explorer.link.kitlink.holder.RoomListViewHolder
import com.tencent.iot.explorer.link.kitlink.util.HttpRequest
import com.tencent.iot.explorer.link.kitlink.util.MyCallback
import com.tencent.iot.explorer.link.customview.recyclerview.CRecyclerView
import kotlinx.android.synthetic.main.activity_room_list.*
import kotlinx.android.synthetic.main.menu_back_layout.*

/**
 * 房间列表
 */
class RoomListActivity : BaseActivity(), MyCallback, CRecyclerView.RecyclerItemView {

    private var familyEntity: FamilyEntity? = null

    private val roomList = arrayListOf<RoomEntity>()
    private var total = 0
    private lateinit var roomListFootHolder: RoomListFootHolder

    override fun getContentView(): Int {
        return R.layout.activity_room_list
    }

    override fun onResume() {
        super.onResume()
        refreshRoomList()
    }

    override fun initView() {
        familyEntity = get("family")
        iv_back.setColorFilter(resources.getColor(R.color.black_333333))
        tv_title.text = getString(R.string.room_manager)
        crv_room_list.setList(roomList)
        crv_room_list.addRecyclerItemView(this)
        addFooter()
    }

    private fun addFooter() {
        roomListFootHolder = RoomListFootHolder(this, crv_room_list, R.layout.foot_room_list)
        roomListFootHolder.footListener = object : CRecyclerView.FootListener {
            override fun doAction(
                holder: CRecyclerView.FootViewHolder<*>,
                clickView: View,
                position: Int
            ) {
                addRoom()
            }
        }
    }

    override fun setListener() {
        iv_back.setOnClickListener { finish() }
        tv_no_room_add.setOnClickListener {
            addRoom()
        }
    }

    /**
     * 跳转添加房间
     */
    private fun addRoom() {
        familyEntity?.run {
            if (Role == 1) {
                jumpActivity(AddRoomActivity::class.java)
            } else {
                show(getString(R.string.no_add_permission))
            }
        }
    }

    /**
     *  获取家庭内房间列表
     */
    private fun refreshRoomList() {
        roomList.clear()
        loadMoreRoomList()
    }

    /**
     *  获取家庭内房间列表
     */
    private fun loadMoreRoomList() {
        familyEntity?.let {
            if (TextUtils.isEmpty(it.FamilyId)) return
            if (roomList.size > 0 && roomList.size >= total) return
            HttpRequest.instance.roomList(it.FamilyId, roomList.size, this)
        }
    }

    override fun fail(msg: String?, reqCode: Int) {
        L.e(msg ?: "")
    }

    override fun success(response: BaseResponse, reqCode: Int) {
        if (response.isSuccess()) {
            response.parse(RoomListResponse::class.java)?.let {
                total = it.Total
                it.Roomlist?.run {
                    roomList.addAll(this)
                    showRoomList()
                }
            }
        }
    }

    /**
     * 监听列表点击
     */
    override fun doAction(
        viewHolder: CRecyclerView.CViewHolder<*>,
        clickView: View,
        position: Int
    ) {
        familyEntity?.run {
            put("room",roomList[position])
            jumpActivity(RoomActivity::class.java)
        }
    }

    override fun getViewHolder(parent: ViewGroup, viewType: Int): CRecyclerView.CViewHolder<*> {
        return RoomListViewHolder(this, parent, R.layout.item_room_list)
    }

    override fun getViewType(position: Int): Int {
        return 0
    }

    private fun showRoomList() {
        if (roomList.size > 0) {
            //已经作限制，可以重复调用，已经添加时不会再添加
            crv_room_list.addFooter(roomListFootHolder)
            crv_room_list.notifyDataChanged()
            cl_no_room.visibility = View.GONE
            crv_room_list.visibility = View.VISIBLE
        } else {
            cl_no_room.visibility = View.VISIBLE
            crv_room_list.visibility = View.GONE
        }
    }
}
