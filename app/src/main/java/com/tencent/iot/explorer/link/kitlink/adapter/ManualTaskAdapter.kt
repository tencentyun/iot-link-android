package com.tencent.iot.explorer.link.kitlink.adapter

import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.chauthai.swipereveallayout.SwipeRevealLayout
import com.squareup.picasso.Picasso
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.T
import com.tencent.iot.explorer.link.kitlink.entity.ManualTask
import java.util.*

class ManualTaskAdapter(list: MutableList<ManualTask>) : RecyclerView.Adapter<ManualTaskAdapter.ViewHolder>() {
    var list: MutableList<ManualTask> = LinkedList()

    init {
        this.list = list
    }

    class ViewHolder(layoutView: View) : RecyclerView.ViewHolder(layoutView) {
        var devName: TextView
        var taskTip: TextView
        var go: ImageView
        var ivType: ImageView
        var taskDesc: TextView
        var firstItem: ConstraintLayout
        var deleteItem: ConstraintLayout
        var addTask: ImageView
        var itemLayout: ConstraintLayout
        var swipeRevealLayout: SwipeRevealLayout

        init {
            devName = layoutView.findViewById(R.id.tv_dev_name)
            taskTip = layoutView.findViewById(R.id.tv_task_tip)
            go = layoutView.findViewById(R.id.iv_go)
            ivType = layoutView.findViewById(R.id.iv_type)
            taskDesc = layoutView.findViewById(R.id.tv_task_desc)
            firstItem = layoutView.findViewById(R.id.layout_manual_task)
            addTask = layoutView.findViewById(R.id.iv_add_task)
            itemLayout = layoutView.findViewById(R.id.item_layout)
            deleteItem = layoutView.findViewById(R.id.layout_delete_btn)
            swipeRevealLayout = layoutView.findViewById(R.id.swipeRevealLayout)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_manual_task, parent, false)
        val holder = ViewHolder(view)
        holder.itemLayout.setOnClickListener {
            val position = holder.adapterPosition
            if (onItemClicked != null) {
                onItemClicked!!.onItemClicked(position, list[position])
            }
        }
        holder.deleteItem.setOnClickListener {
            val position = holder.adapterPosition
            if (onItemClicked != null) {
                onItemClicked!!.onDeletedClicked(position)
                holder.swipeRevealLayout.close(false)
            }
        }
        return holder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (position == 0) {
            holder.firstItem.visibility = View.VISIBLE
            holder.addTask.setOnClickListener{
                if (onItemClicked != null) {
                    onItemClicked!!.onAddTaskClicked()
                }}
        } else {
            holder.firstItem.visibility = View.GONE
        }

        if (list[position].type == 1) {
            Picasso.get().load(R.mipmap.delay_time_icon).into(holder.ivType)
            holder.taskDesc.setText("" + list[position].hour + T.getContext().getString(R.string.unit_h)
            + list[position].min + T.getContext().getString(R.string.unit_m))
        } else {
            if (!TextUtils.isEmpty(list[position].iconUrl)) {
                Picasso.get().load(list[position].iconUrl).into(holder.ivType)
            } else {
                Picasso.get().load(R.drawable.device_placeholder).into(holder.ivType)
            }
            holder.taskDesc.setText(": " + list[position].task)
        }

        holder.devName.setText(list[position].devName)
        holder.taskTip.setText(list[position].taskTip)
    }

    override fun getItemCount(): Int {
        if (list == null) {
            return 0
        }
        return list.size
    }

    interface OnItemClicked {
        fun onItemClicked(pos: Int, manualTask: ManualTask?)
        fun onAddTaskClicked()
        fun onDeletedClicked(pos: Int)
    }

    private var onItemClicked: OnItemClicked? = null

    fun setOnItemClicked(onItemClicked: OnItemClicked?) {
        this.onItemClicked = onItemClicked
    }
}