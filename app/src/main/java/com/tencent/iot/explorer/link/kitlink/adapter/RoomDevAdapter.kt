package com.tencent.iot.explorer.link.kitlink.adapter

import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.core.auth.entity.DeviceEntity
import kotlin.collections.ArrayList

class RoomDevAdapter(list: MutableList<DeviceEntity>) : RecyclerView.Adapter<RoomDevAdapter.ViewHolder>() {
    var list: MutableList<DeviceEntity> = ArrayList()

    init {
        this.list = list
    }

    class ViewHolder(layoutView: View) : RecyclerView.ViewHolder(layoutView) {
        var devName: TextView
        var devImg: ImageView
        var status: ImageView
        var more: ImageView
        var backgroundLayout: ConstraintLayout

        init {
            devName = layoutView.findViewById(R.id.tv_dev_name)
            devImg = layoutView.findViewById(R.id.iv_dev)
            status = layoutView.findViewById(R.id.iv_dev_status)
            more = layoutView.findViewById(R.id.iv_more)
            backgroundLayout = layoutView.findViewById(R.id.item_layout_background)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_room_dev, parent, false)
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
        holder.devName.setText(list.get(position)?.AliasName)
        if (TextUtils.isEmpty(list.get(position)?.IconUrl)) {
            Picasso.get().load(R.drawable.device_placeholder).into(holder.devImg)
        } else {
            Picasso.get().load(list.get(position)?.IconUrl).into(holder.devImg)
        }
        if (list.get(position)?.online == 1) {
            holder.backgroundLayout.visibility = View.GONE
        } else {
            holder.backgroundLayout.visibility = View.VISIBLE
        }
    }

    override fun getItemCount(): Int {
        if (list == null) {
            return 0
        }
        return list.size
    }

    interface OnItemClicked {
        fun onItemClicked(pos: Int, dev: DeviceEntity)
    }

    private var onItemClicked: OnItemClicked? = null

    fun setOnItemClicked(onItemClicked: OnItemClicked?) {
        this.onItemClicked = onItemClicked
    }
}