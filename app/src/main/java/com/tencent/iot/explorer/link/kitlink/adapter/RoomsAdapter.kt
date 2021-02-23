package com.tencent.iot.explorer.link.kitlink.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.core.auth.entity.RoomEntity
import kotlin.collections.ArrayList

class RoomsAdapter(list: MutableList<RoomEntity>) : RecyclerView.Adapter<RoomsAdapter.ViewHolder>() {
    var list: MutableList<RoomEntity> = ArrayList()
    var selectPos = 0

    init {
        this.list = list
    }

    class ViewHolder(layoutView: View) : RecyclerView.ViewHolder(layoutView) {
        var textSelected: TextView
        var textDefault: TextView
        var status: View

        init {
            textSelected = layoutView.findViewById(R.id.tv_selected_status)
            textDefault = layoutView.findViewById(R.id.tv_defalut_status)
            status = layoutView.findViewById(R.id.v_status)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_room, parent, false)
        val holder = ViewHolder(view)
        view.setOnClickListener {
            val position = holder.adapterPosition
            if (onItemClicked != null) {
                onItemClicked!!.onItemClicked(position, list[position])
            }
        }
        return holder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.textSelected.setText(list.get(position)?.RoomName)
        holder.textDefault.setText(list.get(position)?.RoomName)
        if (position == selectPos) {
            holder.textSelected.visibility = View.VISIBLE
            holder.status.visibility = View.VISIBLE
            holder.textDefault.visibility = View.INVISIBLE
        } else {
            holder.textSelected.visibility = View.INVISIBLE
            holder.status.visibility = View.INVISIBLE
            holder.textDefault.visibility = View.VISIBLE
        }

    }

    override fun getItemCount(): Int {
        if (list == null) {
            return 0
        }
        return list.size
    }

    interface OnItemClicked {
        fun onItemClicked(pos: Int, dev: RoomEntity)
    }

    private var onItemClicked: OnItemClicked? = null

    fun setOnItemClicked(onItemClicked: OnItemClicked?) {
        this.onItemClicked = onItemClicked
    }
}