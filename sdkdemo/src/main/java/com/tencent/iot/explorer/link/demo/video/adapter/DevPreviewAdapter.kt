package com.tencent.iot.explorer.link.demo.video.adapter

import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.tencent.iot.explorer.link.demo.R
import com.tencent.iot.explorer.link.demo.video.entity.DevUrl2Preview

class DevPreviewAdapter(context: Context, list: MutableList<DevUrl2Preview>) : RecyclerView.Adapter<DevPreviewAdapter.ViewHolder>() {
    var list: MutableList<DevUrl2Preview> = ArrayList()
    var context: Context? = null

    init {
        this.list = list
        this.context = context
    }

    class ViewHolder(layoutView: View) : RecyclerView.ViewHolder(layoutView) {
        var devName: TextView
        var videoView: TextureView
        var offlineTip: TextView

        init {
            devName = layoutView.findViewById(R.id.tv_dev_name)
            videoView = layoutView.findViewById(R.id.preview_dev)
            offlineTip = layoutView.findViewById(R.id.tv_offline)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_dev_preview, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var str2Show = list.get(position).devName
        if(!TextUtils.isEmpty(list.get(position).channel2DevName)) {
            str2Show += "_" + list.get(position).channel2DevName
        }
        holder.devName.text = str2Show
        if (list.get(position).Status == 1) {
            holder.offlineTip.visibility = View.GONE
        } else {
            holder.offlineTip.visibility = View.VISIBLE
        }
        holder.videoView.surfaceTextureListener = list.get(position).surfaceTextureListener
    }

    override fun getItemCount(): Int {
        if (list == null) return 0
        return list.size
    }

}