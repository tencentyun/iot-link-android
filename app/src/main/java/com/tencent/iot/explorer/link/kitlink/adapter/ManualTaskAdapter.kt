package com.tencent.iot.explorer.link.kitlink.adapter

import android.text.TextUtils
import android.util.Log
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
import kotlinx.android.synthetic.main.activity_timer.*
import java.util.*

class ManualTaskAdapter(list: MutableList<ManualTask>) : RecyclerView.Adapter<ManualTaskAdapter.ViewHolder>() {
    var list: MutableList<ManualTask> = LinkedList()
    var showHeader: Boolean = true
    var indexOpen2Keep = -1

    init {
        this.list = list
    }

    constructor(list: MutableList<ManualTask>, showHeader: Boolean): this(list) {
        this.showHeader = showHeader
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
                indexOpen2Keep = -1
                this@ManualTaskAdapter.notifyDataSetChanged()
            }
        }
        holder.swipeRevealLayout.setSwipeListener(object: SwipeRevealLayout.SwipeListener {
            override fun onOpened(view: SwipeRevealLayout?) {
                var position = holder.adapterPosition
                Log.e("XXX", "position " + position)
                indexOpen2Keep = position
                this@ManualTaskAdapter.notifyDataSetChanged()
            }

            override fun onClosed(view: SwipeRevealLayout?) {}
            override fun onSlide(view: SwipeRevealLayout?, slideOffset: Float) {}
        })
        return holder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (position == 0 && showHeader) {
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
        } else if (list[position].type == 0) {
            if (!TextUtils.isEmpty(list[position].iconUrl)) {
                Picasso.get().load(list[position].iconUrl).into(holder.ivType)
            } else {
                Picasso.get().load(R.drawable.device_placeholder).into(holder.ivType)
            }
            holder.taskDesc.setText(": " + list[position].task)
        } else if (list[position].type == 2) {
            Picasso.get().load(R.mipmap.send_msg_icon).into(holder.ivType)
            holder.taskDesc.setText(list[position].task)
        } else if (list[position].type == 3) {
            Picasso.get().load(R.mipmap.sel_manual_task_icon).into(holder.ivType)
            holder.taskDesc.setText(list[position].task)
        } else if (list[position].type == 4) {
            Picasso.get().load(R.mipmap.automic_timer_icon).into(holder.ivType)
            var str =  String.format("%02d:%02d, ", list[position].hour, list[position].min)
            if (list[position].workDayType == 0) {
                str += T.getContext().getString(R.string.run_one_time)
            } else if (list[position].workDayType == 1) {
                str += T.getContext().getString(R.string.everyday)
            } else if (list[position].workDayType == 2) {
                str += T.getContext().getString(R.string.work_day)
            } else if (list[position].workDayType == 3) {
                str += T.getContext().getString(R.string.weekend)
            } else if (list[position].workDayType == 4) {
                var dayStr = ""
                for (i in 0 .. list[position].workDays.length - 1) {
                    if (list[position].workDays.get(i).toString() == "1") {
                        when(i) {
                            0 -> {
                                dayStr += T.getContext().getString(R.string.sunday) + " "
                            }
                            1 -> {
                                dayStr += T.getContext().getString(R.string.monday) + " "
                            }
                            2 -> {
                                dayStr += T.getContext().getString(R.string.tuesday) + " "
                            }
                            3 -> {
                                dayStr += T.getContext().getString(R.string.wednesday) + " "
                            }
                            4 -> {
                                dayStr += T.getContext().getString(R.string.thursday) + " "
                            }
                            5 -> {
                                dayStr += T.getContext().getString(R.string.friday) + " "
                            }
                            6 -> {
                                dayStr += T.getContext().getString(R.string.saturday) + " "
                            }
                        }
                    }
                }
                str += dayStr
            }
            holder.taskDesc.setText(str.trim())
        } else if (list[position].type == 5) {
            if (!TextUtils.isEmpty(list[position].iconUrl)) {
                Picasso.get().load(list[position].iconUrl).into(holder.ivType)
            } else {
                Picasso.get().load(R.drawable.device_placeholder).into(holder.ivType)
            }
            holder.taskDesc.setText(": " + T.getContext().getString(R.string.equals_str) + " " + list[position].task)
        }

        holder.devName.setText(list[position].devName)
        holder.taskTip.setText(list[position].taskTip)

        if (position != indexOpen2Keep) {
            holder.swipeRevealLayout.close(false)
        } else {
            holder.swipeRevealLayout.open(false)
        }

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

//    override fun notifyDataSetChanged() {
//
//    }
}