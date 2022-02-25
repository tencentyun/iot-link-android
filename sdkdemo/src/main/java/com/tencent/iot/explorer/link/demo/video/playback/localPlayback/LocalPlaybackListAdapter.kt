package com.tencent.iot.explorer.link.demo.video.playback.localPlayback

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.tencent.iot.explorer.link.demo.R
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class LocalPlaybackListAdapter(context: Context?, list: MutableList<PlaybackFile>) :
    RecyclerView.Adapter<LocalPlaybackListAdapter.ViewHolder>() {
    var list: MutableList<PlaybackFile> = ArrayList()
    var index = -1
    var context: Context? = null

    init {
        this.list = list
        this.context = context
    }

    class ViewHolder(layoutView: View) : RecyclerView.ViewHolder(layoutView) {
        var startTime: TextView
        var endTime: TextView
        var download: ImageView

        init {
            startTime = layoutView.findViewById(R.id.tv_start_time)
            endTime = layoutView.findViewById(R.id.tv_end_time)
            download = layoutView.findViewById(R.id.iv_download)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_playback_file, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val sdf = SimpleDateFormat("HH:mm:ss")
        val formatStartTime: String = sdf.format(Date(list[position].start_time * 1000))
        val formatEndTime: String = sdf.format(Date(list[position].end_time * 1000))
        holder.startTime.text = formatStartTime
        holder.endTime.text = formatEndTime
        holder.download.setOnClickListener {
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