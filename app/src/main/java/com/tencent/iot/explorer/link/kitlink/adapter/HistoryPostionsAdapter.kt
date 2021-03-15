package com.tencent.iot.explorer.link.kitlink.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.kitlink.entity.Address
import com.tencent.iot.explorer.link.kitlink.entity.Postion
import kotlin.collections.ArrayList

class HistoryPostionsAdapter(list: MutableList<Address>) : RecyclerView.Adapter<HistoryPostionsAdapter.ViewHolder>() {
    var list: MutableList<Address> = ArrayList()

    init {
        this.list = list
    }

    class ViewHolder(layoutView: View) : RecyclerView.ViewHolder(layoutView) {
        var title: TextView
        var address: TextView
        var status: ImageView
        var line: View
        var bottomLine: View
        var clear: TextView

        init {
            title = layoutView.findViewById(R.id.tv_pos_title)
            address = layoutView.findViewById(R.id.tv_pos_address)
            status = layoutView.findViewById(R.id.iv_select_status)
            line = layoutView.findViewById(R.id.v_line)
            bottomLine = layoutView.findViewById(R.id.v_line_bottom)
            clear = layoutView.findViewById(R.id.tv_clear_history)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_history_postion, parent, false)
        val holder = ViewHolder(view)
        view.setOnClickListener {
            val position = holder.adapterPosition
            onItemClicked?.onItemClicked(position)

        }
        return holder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.line.visibility = View.GONE
        holder.title.text = list.get(position).name
        holder.status.visibility = View.INVISIBLE
        holder.address.text = list.get(position).address
        if (position == list.size - 1) {
            holder.clear.visibility = View.VISIBLE
            holder.clear.setOnClickListener {
                onItemClicked?.onClearAllHistory()
            }
        } else {
            holder.clear.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int {
        if (list == null) {
            return 0
        }
        return list.size
    }

    interface OnItemClicked {
        fun onItemClicked(pos: Int)
        fun onClearAllHistory()
    }

    private var onItemClicked: OnItemClicked? = null

    fun setOnItemClicked(onItemClicked: OnItemClicked?) {
        this.onItemClicked = onItemClicked
    }
}