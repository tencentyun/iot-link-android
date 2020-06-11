package com.tencent.iot.explorer.link.kitlink.holder

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.tencent.iot.explorer.link.kitlink.R
import com.tencent.iot.explorer.link.kitlink.entity.RoomEntity
import com.tencent.iot.explorer.link.kitlink.fragment.BaseFragment
import com.tencent.iot.explorer.link.kitlink.fragment.HomeFragment
import com.view.recyclerview.CRecyclerView
import kotlinx.android.synthetic.main.head_home3.view.*

/**
 * 显示房间
 */
class HomeHeadViewHolder3 : CRecyclerView.HeadViewHolder<List<RoomEntity>>,
    CRecyclerView.RecyclerItemView {

    constructor(context: Context, fragment: HomeFragment, parent: ViewGroup, resId: Int) : super(
        context,
        parent,
        resId
    ) {
        this.fragment = fragment
    }

    private var fragment: HomeFragment

    fun setRoomList(list: List<RoomEntity>) {
        itemView.crv_home_room.layoutManager =
            LinearLayoutManager(itemView.context, LinearLayoutManager.HORIZONTAL, false)
        itemView.crv_home_room.setList(list)
        itemView.crv_home_room.addRecyclerItemView(this)
    }

    override fun doAction(
        viewHolder: CRecyclerView.CViewHolder<*>,
        clickView: View,
        position: Int
    ) {
        itemView.crv_home_room.addSingleSelect(position)
        fragment.showRoomList()
        fragment.tabRoom(position)
    }

    override fun getViewHolder(parent: ViewGroup, viewType: Int): CRecyclerView.CViewHolder<*> {
        return HomeRoomViewHolder(itemView.context, parent, R.layout.item_home_room)
    }

    override fun getViewType(position: Int): Int {
        return 0
    }

    override fun show() {
        itemView.crv_home_room.notifyDataChanged()
    }

}