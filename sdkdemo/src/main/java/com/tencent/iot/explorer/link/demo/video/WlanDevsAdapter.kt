package com.tencent.iot.explorer.link.demo.video

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.tencent.iot.explorer.link.demo.R
import com.tencent.iot.video.link.entity.DeviceServerInfo

class WlanDevsAdapter(context: Context, list: MutableList<DeviceServerInfo>) : RecyclerView.Adapter<WlanDevsAdapter.ViewHolder>() {
    var list: MutableList<DeviceServerInfo> = ArrayList()
    var context: Context? = null

    init {
        this.list = list
        this.context = context
    }

    class ViewHolder(layoutView: View) : RecyclerView.ViewHolder(layoutView) {
        var devName: TextView
        var port: TextView
        var start: TextView

        init {
            devName = layoutView.findViewById(R.id.tv_dev_name)
            port = layoutView.findViewById(R.id.tv_dev_port)
            start = layoutView.findViewById(R.id.start_tv)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_wlan_device, parent, false)
        val holder = ViewHolder(view)
        return holder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.devName.setText(list.get(position)?.deviceName)
        holder.port.setText(list.get(position)?.port.toString())
        holder.start.setOnClickListener {
            onItemClicked?.onItemClicked(position)
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