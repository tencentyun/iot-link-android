package com.kitlink.activity

import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import com.kitlink.App
import com.kitlink.R
import com.kitlink.entity.DeviceEntity
import com.kitlink.entity.RoomEntity
import com.kitlink.holder.WeekRepeatHolder
import com.kitlink.response.BaseResponse
import com.kitlink.util.HttpRequest
import com.kitlink.util.MyCallback
import com.util.L
import com.util.T
import com.view.recyclerview.CRecyclerView
import kotlinx.android.synthetic.main.activity_select_room.*
import kotlinx.android.synthetic.main.item_week_repeat.view.*
import kotlinx.android.synthetic.main.menu_back_layout.*

/**
 * 选择房间
 */
class SelectRoomActivity : BaseActivity(), CRecyclerView.RecyclerItemView {

    private var deviceEntity: DeviceEntity? = null

    private lateinit var selectedRoom: RoomEntity
    private val roomList = arrayListOf<RoomEntity>()

    override fun getContentView(): Int {
        return R.layout.activity_select_room
    }

    override fun initView() {
        tv_title.text = "选择房间"
        deviceEntity = get("device")

        selectedRoom = get<RoomEntity>("select_room")!!
        roomList.addAll(App.data.roomList.subList(1, App.data.roomList.size))
        if (TextUtils.isEmpty(selectedRoom.RoomId) && !TextUtils.isEmpty(deviceEntity!!.RoomId)) {
            roomList.forEach {
                if (it.RoomId == deviceEntity!!.RoomId){
                    selectedRoom = it
                }
            }
        }
        crv_select_room.setList(roomList)
        crv_select_room.addRecyclerItemView(this)
    }

    override fun setListener() {
        iv_back.setOnClickListener { finish() }
    }

    override fun onResume() {
        super.onResume()
        rl_no_room.visibility = if (roomList.isEmpty())
            View.VISIBLE
        else View.GONE
    }

    override fun doAction(
        viewHolder: CRecyclerView.CViewHolder<*>,
        clickView: View,
        position: Int
    ) {
        when (position) {
            -1 -> save()
            else -> selected(position)
        }
    }

    /**
     * 选择
     */
    private fun selected(position: Int) {
        selectedRoom = roomList[position]
        crv_select_room.notifyDataChanged()
    }

    /**
     *  保存
     */
    private fun save() {
        if (deviceEntity == null) return
        App.data.getCurrentFamily().run {
            selectedRoom.let {
                HttpRequest.instance.changeRoom(FamilyId, it.RoomId, deviceEntity!!.ProductId,
                    deviceEntity!!.DeviceName, object : MyCallback {
                        override fun fail(msg: String?, reqCode: Int) {
                            L.e(msg ?: "")
                        }

                        override fun success(response: BaseResponse, reqCode: Int) {
                            if (response.isSuccess()) {
                                deviceEntity?.RoomId = it.RoomId
                                finish()
                            } else {
                                runOnUiThread {
                                    T.show(response.msg)
                                }
                            }
                        }
                    })
            }
        }
    }

    override fun getViewHolder(parent: ViewGroup, viewType: Int): CRecyclerView.CViewHolder<*> {
        WeekRepeatHolder(this, parent, R.layout.item_week_repeat)
        return object :
            CRecyclerView.CViewHolder<RoomEntity>(this, parent, R.layout.item_week_repeat) {
            override fun show(position: Int) {
                entity?.run {
                    itemView.tv_week_repeat_title.text = this.RoomName
                    itemView.iv_week_repeat_selected.setImageResource(
                        if (selectedRoom.RoomId == this.RoomId) R.mipmap.icon_checked
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

    override fun getViewType(position: Int): Int {
        return 0
    }
}
