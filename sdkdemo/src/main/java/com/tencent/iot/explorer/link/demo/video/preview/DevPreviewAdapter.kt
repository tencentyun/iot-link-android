package com.tencent.iot.explorer.link.demo.video.preview

import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tencent.iot.explorer.link.demo.databinding.ItemDevPreviewBinding

class DevPreviewAdapter(context: Context, list: MutableList<DevUrl2Preview>) : RecyclerView.Adapter<DevPreviewAdapter.ViewHolder>() {
    var list: MutableList<DevUrl2Preview> = ArrayList()
    var context: Context? = null

    init {
        this.list = list
        this.context = context
    }

    class ViewHolder(val binding: ItemDevPreviewBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDevPreviewBinding.inflate(LayoutInflater.from(parent.context))
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var str2Show = list.get(position).devName
        if(!TextUtils.isEmpty(list.get(position).channel2DevName)) {
            str2Show += "_" + list.get(position).channel2DevName
        }
        holder.binding.tvDevName.text = str2Show
        if (list.get(position).Status == 1) {
            holder.binding.tvOffline.visibility = View.GONE
        } else {
            holder.binding.tvOffline.visibility = View.VISIBLE
        }
        holder.binding.previewDev.surfaceTextureListener = list.get(position).surfaceTextureListener
    }

    override fun getItemCount(): Int {
        if (list == null) return 0
        return list.size
    }

}