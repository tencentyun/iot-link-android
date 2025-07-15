package com.tencent.iot.explorer.link.demo.video.playback.localPlayback

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.tencent.iot.explorer.link.demo.R
import com.tencent.iot.explorer.link.demo.databinding.ItemPlaybackFileBinding
import java.text.SimpleDateFormat
import java.util.*

class LocalPlaybackListAdapter(context: Context?, list: MutableList<PlaybackFile>) :
    RecyclerView.Adapter<LocalPlaybackListAdapter.ViewHolder>() {
    var list: MutableList<PlaybackFile> = ArrayList()
    var index = -1
    var context: Context? = null

    init {
        this.list = list
        this.context = context
    }

    class ViewHolder(val binding: ItemPlaybackFileBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPlaybackFileBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val sdf = SimpleDateFormat("HH:mm:ss")
        val formatStartTime: String = sdf.format(Date(list[position].start_time * 1000))
        val formatEndTime: String = sdf.format(Date(list[position].end_time * 1000))
        holder.binding.tvStartTime.text = formatStartTime
        holder.binding.tvEndTime.text = formatEndTime
        holder.binding.ivDownload.setOnClickListener {
            onDownloadClicked?.onItemDownloadClicked(position)
        }

        holder.itemView.setOnClickListener {
            onItemClicked?.onItemClicked(position)
        }
    }

    override fun getItemCount(): Int {
        if (list == null) return 0
        return list.size
    }

    interface OnDownloadClickedListener {
        fun onItemDownloadClicked(pos: Int)
    }

    private var onDownloadClicked: OnDownloadClickedListener? = null

    fun setOnDownloadClickedListenter(onItemClicked: OnDownloadClickedListener?) {
        this.onDownloadClicked = onItemClicked
    }

    interface OnItemClickedListener {
        fun onItemClicked(pos: Int)
    }

    private var onItemClicked: OnItemClickedListener? = null

    fun setOnItemClickedListener(onItemClicked: OnItemClickedListener?) {
        this.onItemClicked = onItemClicked
    }
}