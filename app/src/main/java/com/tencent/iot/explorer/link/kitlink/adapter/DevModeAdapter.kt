package com.tencent.iot.explorer.link.kitlink.adapter

import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.T
import com.tencent.iot.explorer.link.core.auth.entity.DevModeInfo
import com.tencent.iot.explorer.link.core.auth.entity.OpValue
import com.tencent.iot.explorer.link.kitlink.entity.RouteType
import java.util.*

class DevModeAdapter(list: MutableList<DevModeInfo>, type: Int) : RecyclerView.Adapter<DevModeAdapter.ViewHolder>() {
    var list: MutableList<DevModeInfo> = LinkedList()
    var type = RouteType.MANUAL_TASK_ROUTE

    init {
        this.list = list
        this.type = type
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
        } else if (this.type == RouteType.AUTOMIC_CONDITION_ROUTE || this.type == RouteType.EDIT_AUTOMIC_CONDITION_ROUTE ||
                this.type == RouteType.EDIT_AUTOMIC_CONDITION_DETAIL_ROUTE || this.type == RouteType.ADD_AUTOMIC_CONDITION_DETAIL_ROUTE) {

            var prefix = ""
            if (list.get(position).op.equals(OpValue.OP_GR)) {
                prefix = T.getContext().getString(R.string.tag_gr)
            } else if (list.get(position).op.equals(OpValue.OP_LT)) {
                prefix = T.getContext().getString(R.string.tag_lt)
            } else {
                prefix = T.getContext().getString(R.string.tag_eq)
            }
            holder.valueTxt.setText(prefix + " " + list.get(position).value + list.get(position).unit)
        } else {
            holder.valueTxt.setText(list.get(position).value + list.get(position).unit)
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