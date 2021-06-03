package com.tencent.iot.explorer.link.core.demo.video.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.tencent.iot.explorer.link.core.demo.R
import com.tencent.iot.explorer.link.core.demo.video.entity.DevInfo

class DevsAdapter(context: Context, list: MutableList<DevInfo>) : RecyclerView.Adapter<DevsAdapter.ViewHolder>() {
    var list: MutableList<DevInfo> = ArrayList()
    var context: Context? = null
    var showCheck = false
    var checkedIds : MutableList<Int> = ArrayList()
    var maxNum = -1 // 选择子项目的上限， <= 0 没有上限， > 0 上限生效

    init {
        this.list = list
        this.context = context
    }

    class ViewHolder(layoutView: View) : RecyclerView.ViewHolder(layoutView) {
        var devName: TextView
        var statusTv: TextView
        var devImg: ImageView
        var more: ImageView
        var checked: ImageView
        var backgroundLayout: ConstraintLayout

        init {
            devName = layoutView.findViewById(R.id.tv_dev_name)
            devImg = layoutView.findViewById(R.id.iv_dev)
            statusTv = layoutView.findViewById(R.id.tv_dev_status)
            more = layoutView.findViewById(R.id.iv_more)
            checked = layoutView.findViewById(R.id.iv_select)
            backgroundLayout = layoutView.findViewById(R.id.item_layout_background)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_video_list_dev, parent, false)
        val holder = ViewHolder(view)
        view.setOnClickListener {
            if (showCheck) return@setOnClickListener
            val position = holder.adapterPosition
            if (position < list.size && position >= 0) {
                onItemClicked?.onItemClicked(position, list[position])
            }
        }
        return holder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.devName.setText(list.get(position)?.deviceName)
        Picasso.get().load(R.mipmap.ipc).into(holder.devImg)

        holder.checked.visibility = View.GONE
        if (showCheck) {
            holder.checked.visibility = View.VISIBLE
        }

        if (list.get(position)?.Status != 0) {  // 原为等于 1 是在线
            holder.backgroundLayout.visibility = View.GONE
            holder.statusTv.setText(R.string.online)
            context?.let {
                holder.statusTv.setTextColor(it.resources.getColor(R.color.green_29CC85))
                holder.devName.setTextColor(it.resources.getColor(R.color.black_15161A))
            }

        } else {
            holder.backgroundLayout.visibility = View.VISIBLE
            holder.statusTv.setText(R.string.offline)
            context?.let {
                holder.statusTv.setTextColor(it.resources.getColor(R.color.gray_C2C5CC))
                holder.devName.setTextColor(it.resources.getColor(R.color.gray_C2C5CC))
            }
        }

        if (checkedIds.contains(position)) {
            holder.checked.setImageResource(R.mipmap.selected)
        } else {
            holder.checked.setImageResource(R.mipmap.unchecked)
        }

        holder.more.visibility = View.VISIBLE
        holder.checked.setOnClickListener {
            if (checkedIds.contains(position)) {
                checkedIds.remove(position)
                onItemClicked?.onItemCheckedClicked(position, false)
            } else {
                if (maxNum > 0 && checkedIds.size >= maxNum) {
                    onItemClicked?.onItemCheckedLimited()
                    return@setOnClickListener
                }
                checkedIds.add(position)
                onItemClicked?.onItemCheckedClicked(position, true)
            }
            this.notifyDataSetChanged()
        }

        if (list.get(position)?.deviceName.endsWith("20237_7")) {
            holder.devName.setTextColor(context!!.resources.getColor(R.color.red_e54545))
        }
    }

    override fun getItemCount(): Int {
        if (list == null) {
            return 0
        }
        return list.size
    }

    interface OnItemClicked {
        fun onItemClicked(pos: Int, dev: DevInfo)
        fun onItemMoreClicked(pos: Int, dev: DevInfo)
        fun onItemCheckedClicked(pos: Int, checked : Boolean)
        fun onItemCheckedLimited()
    }

    @Volatile
    private var onItemClicked: OnItemClicked? = null

    fun setOnItemClicked(onItemClicked: OnItemClicked?) {
        this.onItemClicked = onItemClicked
    }
}