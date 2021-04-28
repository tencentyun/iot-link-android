package com.tencent.iot.explorer.link.kitlink.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.kitlink.entity.Postion
import kotlin.collections.ArrayList

class PostionsAdapter(list: MutableList<Postion>) : RecyclerView.Adapter<PostionsAdapter.ViewHolder>() {
    var list: MutableList<Postion> = ArrayList()
    var selectPos = 0
    set(value) {
        field = value
        if (list == null || list.size <= 0) return
        selectPostion = list.get(value)
    }
    var selectPostion: Postion? = null

    init {
        this.list = list
    }

    class ViewHolder(layoutView: View) : RecyclerView.ViewHolder(layoutView) {
        var title: TextView
        var address: TextView
        var status: ImageView
        var line: View

        init {
            title = layoutView.findViewById(R.id.tv_pos_title)
            address = layoutView.findViewById(R.id.tv_pos_address)
            status = layoutView.findViewById(R.id.iv_select_status)
            line = layoutView.findViewById(R.id.v_line)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_postion, parent, false)
        val holder = ViewHolder(view)
        view.setOnClickListener {
            val position = holder.adapterPosition
            if (onItemClicked != null) {
                onItemClicked!!.onItemClicked(position)
            }
        }
        return holder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (position == 0) {
            holder.line.visibility = View.GONE
        } else {
            holder.line.visibility = View.VISIBLE
        }

        if (selectPos == position) {
            holder.status.visibility = View.VISIBLE
            selectPostion = list.get(position)
        } else {
            holder.status.visibility = View.INVISIBLE
        }

        holder.title.text = list.get(position).title
        holder.address.text = list.get(position).address
    }

    override fun getItemCount(): Int {
        if (list == null) {
            return 0
        }
        return list.size
    }

    interface OnItemClicked {
        fun onItemClicked(pos: Int)
    }

    private var onItemClicked: OnItemClicked? = null

    fun setOnItemClicked(onItemClicked: OnItemClicked?) {
        this.onItemClicked = onItemClicked
    }
}