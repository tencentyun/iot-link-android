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
import com.tencent.iot.explorer.link.App
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.T
import com.tencent.iot.explorer.link.kitlink.entity.DevModeInfo
import com.tencent.iot.explorer.link.kitlink.entity.ManualTask
import kotlinx.android.synthetic.main.activity_complete_task_info.*
import org.w3c.dom.Text
import java.util.*

class DevModeAdapter(list: MutableList<DevModeInfo>) : RecyclerView.Adapter<DevModeAdapter.ViewHolder>() {
    var list: MutableList<DevModeInfo> = LinkedList()

    init {
        this.list = list
    }

    class ViewHolder(layoutView: View) : RecyclerView.ViewHolder(layoutView) {
        var nameTxt: TextView
        var valueTxt: TextView
        var line: View

        init {
            nameTxt = layoutView.findViewById(R.id.tv_tip_name)
            valueTxt = layoutView.findViewById(R.id.tv_dev_value)
            line = layoutView.findViewById(R.id.v_divide_line)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_dev_mode, parent, false)
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
        holder.nameTxt.setText(list.get(position).name)
        if (TextUtils.isEmpty(list.get(position).value)) {
            holder.valueTxt.setText(R.string.unset)
        } else {
            holder.valueTxt.setText(list.get(position).value)
        }
        if (position == list.lastIndex) {
            holder.line.visibility = View.INVISIBLE
        } else {
            holder.line.visibility = View.VISIBLE
        }
    }

    override fun getItemCount(): Int {
        if (list == null) {
            return 0
        }
        return list.size
    }

    interface OnItemClicked {
        fun onItemClicked(pos: Int, devModeInfo: DevModeInfo)
    }

    private var onItemClicked: OnItemClicked? = null

    fun setOnItemClicked(onItemClicked: OnItemClicked?) {
        this.onItemClicked = onItemClicked
    }
}