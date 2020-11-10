package com.tencent.iot.explorer.link.kitlink.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.chauthai.swipereveallayout.SwipeRevealLayout
import com.squareup.picasso.Picasso
import com.tencent.iot.explorer.link.App
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.T
import com.tencent.iot.explorer.link.kitlink.entity.Automation
import com.tencent.iot.explorer.link.kitlink.entity.ManualTask
import kotlinx.android.synthetic.main.activity_complete_task_info.*
import java.util.*
import kotlin.collections.HashSet

class SelectManualTaskAdapter(list: MutableList<Automation>) : RecyclerView.Adapter<SelectManualTaskAdapter.ViewHolder>() {
    var list: MutableList<Automation> = LinkedList()
    var index: MutableSet<Int> = HashSet()

    init {
        this.list = list
    }

    class ViewHolder(layoutView: View) : RecyclerView.ViewHolder(layoutView) {
        var taskName: TextView
        var status: TextView

        init {
            taskName = layoutView.findViewById(R.id.item_tip)
            status = layoutView.findViewById(R.id.tv_status)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_select_manual_task, parent, false)
        val holder = ViewHolder(view)
        view.setOnClickListener {
            val position = holder.adapterPosition
            if (index.contains(position)) {
                index.remove(position)
            } else {
                index.add(position)
            }
            this@SelectManualTaskAdapter.notifyDataSetChanged()

            if (onItemClicked != null) {
                onItemClicked!!.onItemClicked(position, list[position])
            }
        }
        return holder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (index.contains(position)) {
            holder.status.visibility = View.VISIBLE
        } else {
            holder.status.visibility = View.INVISIBLE
        }

        holder.taskName.setText(list.get(position).Name)
    }

    override fun getItemCount(): Int {
        if (list == null) {
            return 0
        }
        return list.size
    }

    interface OnItemClicked {
        fun onItemClicked(pos: Int, url: Automation)
    }

    private var onItemClicked: OnItemClicked? = null

    fun setOnItemClicked(onItemClicked: OnItemClicked) {
        this.onItemClicked = onItemClicked
    }
}