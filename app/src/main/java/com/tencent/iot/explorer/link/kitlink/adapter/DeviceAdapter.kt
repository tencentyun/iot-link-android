package com.tencent.iot.explorer.link.kitlink.adapter

import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.core.auth.entity.DeviceEntity
import java.util.*

class DeviceAdapter(list: MutableList<DeviceEntity>) : RecyclerView.Adapter<DeviceAdapter.ViewHolder>() {
    var list: MutableList<DeviceEntity> = LinkedList()

    init {
        this.list = list
    }

    class ViewHolder(layoutView: View) : RecyclerView.ViewHolder(layoutView) {
        var image: ImageView
        var text: TextView

        init {
            image = layoutView.findViewById(R.id.iv_device_icon)
            text = layoutView.findViewById(R.id.tv_device_name)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.device_item, parent, false)
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
        if (TextUtils.isEmpty(list.get(position)?.IconUrl)) {
            Picasso.get().load(R.mipmap.device_placeholder).into(holder.image)
        } else {
            Picasso.get().load(list.get(position)?.IconUrl).into(holder.image)
        }
        holder.text.setText(list.get(position)?.getAlias())
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