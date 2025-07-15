package com.tencent.iot.explorer.link.demo.video

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tencent.iot.explorer.link.demo.databinding.ItemWlanDeviceBinding
import com.tencent.iot.video.link.entity.DeviceServerInfo

class WlanDevsAdapter(context: Context, list: MutableList<DeviceServerInfo>) : RecyclerView.Adapter<WlanDevsAdapter.ViewHolder>() {
    var list: MutableList<DeviceServerInfo> = ArrayList()
    var context: Context? = null

    init {
        this.list = list
        this.context = context
    }

    class ViewHolder(val binding: ItemWlanDeviceBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemWlanDeviceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val holder = ViewHolder(binding)
        return holder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder.binding) {
            tvDevName.setText(list.get(position)?.deviceName)
            tvDevPort.setText(list.get(position)?.port.toString())
            startTv.setOnClickListener {
                onItemClicked?.onItemClicked(position)
            }
        }
    }

    override fun getItemCount(): Int {
        if (list == null) return 0
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